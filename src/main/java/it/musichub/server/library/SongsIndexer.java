package it.musichub.server.library;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.FolderFactory;
import it.musichub.server.library.model.Song;
import it.musichub.server.library.model.SongFactory;
import it.musichub.server.library.utils.FileUtils;
import it.musichub.server.persistence.PersistenceService;
import it.musichub.server.persistence.ex.FileNotFoundException;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceFactory.Service;

public class SongsIndexer implements IndexerService {
	
	/*
	 * EVOLUZIONI:
	 * - vedere se si può usare una pila al posto della mappa
	 * - controllo timerizzato?
	 * - listener di aggiornamento modifica file e cartelle da filesystem?
	 * - sistema di configurazione? (per attivare timer e a quale intervallo, listener di aggiornamento, folder di salvataggio...)
	 * - supporto a più cartelle e più file di configurazione?
	 */

	private boolean init = false;
	
	private String startingDir;
	private Folder startingFolder;
	
	private static final String LIBRARY_FILE_NAME = "library.xml";
	
	//options TODO
	private static boolean verbose = false;
	
	private final static Logger logger = Logger.getLogger(SongsIndexer.class);
	
	public SongsIndexer(String startingDir) {
		super();
		this.startingDir = startingDir;
	}
	
	@Override
	public Folder getStartingFolder() {
		return startingFolder;
	}
	
	private PersistenceService getPersistenceService(){
		return (PersistenceService) ServiceFactory.getServiceInstance(Service.persistence);
	}

	@Override
	public void init(){
		if (startingDir == null)
			throw new IllegalArgumentException("startingDir cannot be null");
		
		//init parsing
		Path startingDirPath = Paths.get(startingDir);
		try {
			boolean notExists = Files.notExists(startingDirPath);
			if (notExists)
				throw new IllegalArgumentException();
		}catch(Exception e){
			logger.error("Error opening directory "+startingDir, e);
			return;
		}
		//normalizzo il percorso
		startingDir = Paths.get(startingDirPath.toUri()).normalize().toString();
		
		loadFromDisk();
		if (startingFolder == null)
			startingFolder = FolderFactory.fromFilePath(startingDir, startingDir, null);
		
		init = true;
	}
	
	@Override
	public void start(){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		
		parse(startingFolder);
		
		//se ci mettiamo lo scanning automatico, va attivato il servizio...
	}
	
	@Override
	public void refresh(){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		//rescan forzato
		parse(startingFolder);
	}
	
	@Override
	public void refresh(String subFolderPath){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		//rescan forzato (cartella specifica)
		Folder subFolder = FolderFactory.fromFilePath(subFolderPath, startingDir, null);
		parse(startingFolder, subFolder);
	}
	
	@Override
	public void refresh(String subFolderPath, boolean parseSubFolders){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		//rescan forzato (cartella specifica)
		Folder subFolder = FolderFactory.fromFilePath(subFolderPath, startingDir, null);
		parse(startingFolder, subFolder, parseSubFolders);
	}
	
	@Override
	public void stop(){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		saveToDisk();
		
		//se ci mettiamo lo scanning automatico, va disattivato il servizio...
	}
	
	@Override
	public void destroy(){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		

		startingDir = null;
		startingFolder = null;
	}
	
	private void parse(Folder startingFolder){
		parse(startingFolder, startingFolder);
	}
	
	private void parse(Folder startingFolder, Folder folderToParse){
		parse(startingFolder, folderToParse, true);
	}
	
	private void parse(Folder startingFolder, Folder folderToParse, boolean parseSubFolders){
		FileParser fp = new FileParser(startingFolder);
		fp.parse(folderToParse, parseSubFolders);
	}
	
//	public List<Song> search(String query){
//		return search(startingFolder, query);
//	}
//	
//	public List<Song> search(Folder folder, String query){
//		SongsSearch search = new SongsSearch(folder, query);
//		List<Song> results = search.execute(); //TODO clonare!!
//		return results;
//	}
	
	
	
	private void loadFromDisk(){
		logger.info("Loading library from file...");
		
		Folder loadedStartingFolder = null;
		try {
			loadedStartingFolder = getPersistenceService().loadFromDisk(Folder.class, LIBRARY_FILE_NAME);
		} catch(FileNotFoundException e) {
			logger.warn("Library file not found. May be first launch.", e);
		    return;
		} catch(LoadException e) {
			logger.error("Error loading library file", e);
		    return;
		}
		
		if (loadedStartingFolder != null){
			if (startingDir.equals(loadedStartingFolder.getPath())){
				startingFolder = loadedStartingFolder;
				logger.info("... library loaded.");
			}else{
				logger.warn("Loaded library file is from a different path. Ignored.");
			}
		}
	}
	
	private void saveToDisk(){
		logger.info("Saving library to file...");
		
		try {
			getPersistenceService().saveToDisk(startingFolder, LIBRARY_FILE_NAME);
		} catch (SaveException e) {
			logger.error("Error saving library", e);
			return;
		}
		
		logger.info("... library saved.");
	}
	
	
	
	private static class FileParser extends SimpleFileVisitor<Path> {

		
		private Folder startingFolder = null;
		private Folder folderToParse = null;
		private boolean parseSubFolders = true;
		private Stack<CurrentFolderData> currentFoldersStack = null;
		
		private static class CurrentFolderData{
			public Folder currentFolder = null;
			public boolean parseFiles = true;
			public List<String> parsedSongs;
			
			public CurrentFolderData(Folder currentFolder) {
				super();
				this.currentFolder = currentFolder;
			}
		}
		
		
		
		public FileParser(Folder startingFolder) {
			super();
			this.startingFolder = startingFolder;
		}
		
		public void parse(Folder folderToParse, boolean parseSubFolders){
			long startTime = System.currentTimeMillis();
			
			this.folderToParse = folderToParse; 
			this.parseSubFolders = parseSubFolders;
			
			String relPath = FileUtils.getRelativePath(folderToParse.getFile(), startingFolder.getFile());
			if (relPath == null)
				throw new IllegalArgumentException("folderToParse must be a subFolder of startingFolder");
			
			currentFoldersStack = new Stack<>();
			
			try {
				Files.walkFileTree(startingFolder.getFile().toPath(), this);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Error parsing directory "+folderToParse.getPath(), e);
				return;
			}
			
			currentFoldersStack = null;
			
			long endTime = System.currentTimeMillis();
			
			logger.info("Parsing took " + (endTime - startTime) + " milliseconds");
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			logger.info(String.format("Indexing folder: %s", dir));
			
			
			String dirPathStr = Paths.get(dir.toUri()).normalize().toString();
			
			if (startingFolder.getPath().equals(dirPathStr)){
				//caso root
				if (verbose)
					logger.debug("Folder "+dirPathStr+" is root");
				currentFoldersStack.push(new CurrentFolderData(startingFolder));
			}else{
				//caso interno
				if (startingFolder == null)
					throw new IllegalStateException("startingFolder cannot be null");
				
				if (verbose)
					logger.debug("Folder "+dirPathStr+" is not root");
				String parentFolderPath = Paths.get(dir.getParent().toUri()).normalize().toString();
				Folder parentFolderTemplate = FolderFactory.fromFilePath(parentFolderPath, startingFolder.getPath(), null);
				Folder parentFolder = startingFolder.getFolderRecursive(parentFolderTemplate);
				
				Folder folder = FolderFactory.fromFilePath(dirPathStr, startingFolder.getPath(), parentFolder);
				
				Folder folderInCurrentFolder = parentFolder.getFolder(folder);
				if (folderInCurrentFolder != null){
					logger.info("Folder "+dirPathStr+" is already parsed");
					folder = folderInCurrentFolder;
				}else{		
					parentFolder.addFolder(folder);
				}
				currentFoldersStack.push(new CurrentFolderData(folder));
			}
			CurrentFolderData currentFolderData = currentFoldersStack.peek();
			
			//escludo il parsing dei file nei rami scorrelati
			String relPath = FileUtils.getRelativePath(currentFolderData.currentFolder.getFile(), folderToParse.getFile());
			if (relPath == null){ //ramo scorrelato
				currentFolderData.parseFiles = false;
				return CONTINUE;
			}
			if (!relPath.isEmpty() && !parseSubFolders){ //subFolder
				currentFolderData.parseFiles = false;
				return CONTINUE;
			}
			currentFolderData.parseFiles = true;
			
			currentFolderData.parsedSongs = new ArrayList<>();
			
			return CONTINUE;
		}
		
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
//			String dirPathStr = Paths.get(file.getParent().toUri()).normalize().toString();
			CurrentFolderData currentFolderData = currentFoldersStack.peek();
			
			if (!currentFolderData.parseFiles)
				return CONTINUE;
			
			
			if (attr.isSymbolicLink()) {
				logger.debug(String.format("Symbolic link (ignored): %s ", file));
			} else if (attr.isRegularFile()) {
				String path = Paths.get(file.toUri()).normalize().toString();
				if (".mp3".equalsIgnoreCase(FileUtils.extractExtension(path))){
					logger.info(String.format("Song file: %s (" + attr.size() + " bytes)", file));
					currentFolderData.parsedSongs.add(path);
					try {
						Song songInCurrentFolder = currentFolderData.currentFolder.getSong(file);
						if (songInCurrentFolder != null){ //canzone già presente
							if (songInCurrentFolder.isSongUpdated()){
								//la canzone è già stata parsata ed è aggiornata
								logger.info("Song is already parsed.");
								return CONTINUE;
							}
							//la canzone era già stata parsata ma è stata aggiornata: la cancello
							currentFolderData.currentFolder.getSongs().remove(songInCurrentFolder);
							logger.info("Song was already parsed but is now updated.");
						}
						
						Song song = SongFactory.fromFilePath(path, startingFolder.getPath());
						currentFolderData.currentFolder.addSong(song);
						song.setFolder(currentFolderData.currentFolder);
						
					} catch (UnsupportedTagException | InvalidDataException | IOException e) {
						logger.error("Error parsing "+path, e);
						return CONTINUE;
					}
				}else{
					if (verbose)
						logger.debug(String.format("Ignoring unknown file: %s ", file));
				}
				
			} else {
				logger.debug(String.format("Other: %s ", file));
			}
//			logger.debug("(" + attr.size() + "bytes)");
			return CONTINUE;
		}

		// Print each directory visited.
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
//			String dirPathStr = Paths.get(dir.toUri()).normalize().toString();
			CurrentFolderData currentFolderData = currentFoldersStack.peek();
			
			if (!currentFolderData.parseFiles)
				return CONTINUE;
			
			logger.debug(String.format("Ended indexing folder: %s%n", dir));

			//verifico le song presenti per scartare quelle non rilevate in questa esplorazione
			List<Song> songsToRemove = new ArrayList<>();
			List<Song> songs = currentFolderData.currentFolder.getSongs();
			if (songs != null){
				for (Song song : songs){
					String path = song.getPath();
					if (!currentFolderData.parsedSongs.contains(path)){
						logger.debug("Removing old song "+song.getFilename());
						songsToRemove.add(song);
					}
				}
				currentFolderData.currentFolder.getSongs().removeAll(songsToRemove);
			}
			
			currentFoldersStack.pop();
			
			
			return CONTINUE;
		}

		// If there is some error accessing
		// the file, let the user know.
		// If you don't override this method
		// and an error occurs, an IOException
		// is thrown.
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			logger.error("Visit failed", exc);
			return CONTINUE;
		}
		
	}
}
