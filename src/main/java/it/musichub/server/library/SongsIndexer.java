package it.musichub.server.library;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.thoughtworks.xstream.XStream;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.FolderFactory;
import it.musichub.server.library.model.Song;
import it.musichub.server.library.model.SongFactory;
import it.musichub.server.library.utils.FileUtils;

public class SongsIndexer {
	
	/*
	 * TODO: decidere se alla fine preparare mappe di pre-parsing
	 * (...oppure le si potrebbero preparare alla prima richiesta?)
	 * per cartella?
	 * per genere?
	 * per rating?
	 * ecc...
	 */

	private boolean init = false;
	
	private String startingDir;
	private Folder startingFolder;
	
	private XStream xstream;
	
	private static final String LIBRARY_PATH_NAME = System.getProperty("java.io.tmpdir");
	private static final String LIBRARY_FOLDER_NAME = ".musichub";
	private static final String LIBRARY_FILE_NAME = "library.xml";
	
	public SongsIndexer(String startingDir) {
		super();
		this.startingDir = startingDir;
	}
	
	public Folder getStartingFolder() {
		return startingFolder;
	}


	public void init(){
		if (startingDir == null)
			throw new IllegalArgumentException("startingDir cannot be null");
		
		//init xstream
		xstream = new XStream();
		XStream.setupDefaultSecurity(xstream);
		xstream.allowTypeHierarchy(Folder.class);
		xstream.allowTypeHierarchy(Song.class);
//		xstream.addPermission(AnyTypePermission.ANY);
		xstream.alias("folder", Folder.class);
		xstream.alias("song", Song.class);
		
		//init parsing
		Path startingDirPath = Paths.get(startingDir);
		try {
			boolean notExists = Files.notExists(startingDirPath);
			if (notExists)
				throw new IllegalArgumentException();
		}catch(Exception e){
			System.err.println("Error opening directory "+startingDir+" - exception: "+e);
			return;
		}
		//normalizzo il percorso
		startingDir = Paths.get(startingDirPath.toUri()).normalize().toString();
		
		loadFromDisk();
		if (startingFolder == null)
			startingFolder = FolderFactory.fromFilePath(startingDir, startingDir, null);
		
		init = true;
	}
	
	public void start(){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		
		parse(startingFolder);
		
		//se ci mettiamo lo scanning automatico, va attivato il servizio...
	}
	
	public void refresh(){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		//rescan forzato
		parse(startingFolder);
	}
	
	public void refresh(String subFolderPath){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		//rescan forzato (cartella specifica)
		Folder subFolder = FolderFactory.fromFilePath(subFolderPath, startingDir, null);
		parse(startingFolder, subFolder);
	}
	
	public void refresh(String subFolderPath, boolean parseSubFolders){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		//rescan forzato (cartella specifica)
		Folder subFolder = FolderFactory.fromFilePath(subFolderPath, startingDir, null);
		parse(startingFolder, subFolder, parseSubFolders);
	}
	
	public void stop(){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		//se ci mettiamo lo scanning automatico, va disattivato il servizio...
	}
	
	public void destroy(){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
		saveToDisk();
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
	
	public List<Song> search(String query){
		return search(startingFolder, query);
	}
	
	public List<Song> search(Folder folder, String query){
		SongsSearch search = new SongsSearch(folder, query);
		List<Song> results = search.execute(); //TODO clonare!!
		return results;
	}
	
	
	
	private static String getLibraryPath(){
		return LIBRARY_PATH_NAME + File.separator + LIBRARY_FOLDER_NAME + File.separator + LIBRARY_FILE_NAME;
	}
	
	private void loadFromDisk(){
		String path = getLibraryPath();
		File file = new File(path);
		
		if (file.exists()){
			System.out.println("Loading library from file...");

			Folder loadedStartingFolder = null;
			try {
				loadedStartingFolder = (Folder)xstream.fromXML(file);
			} catch(Exception e) {
				System.err.println("Error loading library file");
			    e.printStackTrace(); // this obviously needs to be refined.
			    return;
			}
			
			if (loadedStartingFolder != null){
				if (startingDir.equals(loadedStartingFolder.getPath())){
					startingFolder = loadedStartingFolder;
					System.out.println("... library loaded.");					
				}else{
					System.out.println("Loaded library file is from a different path. Ignored.");
				}
			}
		}
	}
	
	private void saveToDisk(){
		/**
		 * TODO
		 * 
		 * verificare se ha senso la compressione della libreria.
		 * 
		 * anche gli alias andrebbero cambiati?? "f" e "s"
		 * 
		 * http://xstream.10960.n7.nabble.com/XStream-data-compression-td3467.html
		 * https://docs.oracle.com/javase/7/docs/api/java/util/zip/GZIPInputStream.html
		 * https://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStream.html
		 * 
		 */
		String xml = xstream.toXML(startingFolder);
		
		String path = getLibraryPath();
		File file = new File(path);
	
		try {
			file.getParentFile().mkdirs(); //crea la cartella se non esiste già
			file.createNewFile(); //crea il file se non esiste già
		} catch (IOException e) {
			System.err.println("Error opening library file for save");
			e.printStackTrace(); // this obviously needs to be refined.
			return;
		} 
		
		FileOutputStream fos = null;
		try {
			
		    fos = new FileOutputStream(file, false);
		    fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(StandardCharsets.UTF_8)); //write XML header, as XStream doesn't do that for us
		    byte[] bytes = xml.getBytes("UTF-8");
		    fos.write(bytes);

		} catch(Exception e) {
			System.err.println("Error saving library file");
		    e.printStackTrace(); // this obviously needs to be refined.
		} finally {
		    if(fos!=null) {
		        try{ 
		            fos.close();
		        } catch (IOException e) {
		        	System.err.println("Error closing library file after saving");
		            e.printStackTrace(); // this obviously needs to be refined.
		        }
		    }
		}
	}
	
	
	
	private static class FileParser extends SimpleFileVisitor<Path> {

		
		private Folder startingFolder = null;
		private Folder folderToParse = null;
		private boolean parseSubFolders = true;
		private Map<String, CurrentFolderData> currentFoldersMap = new HashMap<>();
		
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
			
			currentFoldersMap = new HashMap<>();
			
			try {
				Files.walkFileTree(startingFolder.getFile().toPath(), this);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error parsing directory "+folderToParse.getPath());
				return;
			}
			
			currentFoldersMap = null;
			
			long endTime = System.currentTimeMillis();
			
			System.out.println("Parsing took " + (endTime - startTime) + " milliseconds");
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			System.out.format("Pre-Directory: %s%n", dir);
			
			
			String dirPathStr = Paths.get(dir.toUri()).normalize().toString();
			
			if (startingFolder.getPath().equals(dirPathStr)){
				//caso root
				System.out.println("Folder "+dirPathStr+" is root");
//				currentFolder = startingFolder;
				currentFoldersMap.put(dirPathStr, new CurrentFolderData(startingFolder));
			}else{
				//caso interno
				if (startingFolder == null)
					throw new IllegalStateException("startingFolder cannot be null");
				
				System.out.println("Folder "+dirPathStr+" is not root");
				String parentFolderPath = Paths.get(dir.getParent().toUri()).normalize().toString();
				Folder parentFolderTemplate = FolderFactory.fromFilePath(parentFolderPath, startingFolder.getPath(), null);
				Folder parentFolder = startingFolder.getFolderRecursive(parentFolderTemplate);
				
				Folder folder = FolderFactory.fromFilePath(dirPathStr, startingFolder.getPath(), parentFolder);
				
				Folder folderInCurrentFolder = parentFolder.getFolder(folder);
				if (folderInCurrentFolder != null){
					System.out.println("Folder "+dirPathStr+" is already parsed");
					folder = folderInCurrentFolder;
				}else{		
					parentFolder.addFolder(folder);
				}
				currentFoldersMap.put(dirPathStr, new CurrentFolderData(folder));
			}
			CurrentFolderData currentFolderData = currentFoldersMap.get(dirPathStr);
			
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
			String dirPathStr = Paths.get(file.getParent().toUri()).normalize().toString();
			CurrentFolderData currentFolderData = currentFoldersMap.get(dirPathStr);
			
			if (!currentFolderData.parseFiles)
				return CONTINUE;
			
			
			if (attr.isSymbolicLink()) {
				System.out.format("Symbolic link: %s ", file);
			} else if (attr.isRegularFile()) {
				String path = Paths.get(file.toUri()).normalize().toString();
				if (".mp3".equalsIgnoreCase(FileUtils.extractExtension(path))){
					System.out.format("Song file: %s ", file);
					currentFolderData.parsedSongs.add(path);
					try {
						Song songInCurrentFolder = currentFolderData.currentFolder.getSong(file);
						if (songInCurrentFolder != null){ //canzone già presente
							if (songInCurrentFolder.isSongUpdated()){
								//la canzone è già stata parsata ed è aggiornata
								System.out.println("Song is already parsed.");
								return CONTINUE;
							}
							//la canzone era già stata parsata ma è stata aggiornata: la cancello
							currentFolderData.currentFolder.getSongs().remove(songInCurrentFolder);
							System.out.println("Song was already parsed but is now updated. Removing old version.");
						}
						
						Song song = SongFactory.fromFilePath(path, startingFolder.getPath());
						currentFolderData.currentFolder.addSong(song);
						song.setFolder(currentFolderData.currentFolder);
						
					} catch (UnsupportedTagException | InvalidDataException | IOException e) {
						e.printStackTrace();
						System.err.println("Error parsing "+path);
						return CONTINUE;
					}
				}else{
					System.out.format("Ignoring unknown file: %s ", file);
				}
				
			} else {
				System.out.format("Other: %s ", file);
			}
			System.out.println("(" + attr.size() + "bytes)");
			return CONTINUE;
		}

		// Print each directory visited.
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
			String dirPathStr = Paths.get(dir.toUri()).normalize().toString();
			CurrentFolderData currentFolderData = currentFoldersMap.get(dirPathStr);
			
			if (!currentFolderData.parseFiles)
				return CONTINUE;
			
			System.out.format("Post-Directory: %s%n", dir);

			//verifico le song presenti per scartare quelle non rilevate in questa esplorazione
			List<Song> songsToRemove = new ArrayList<>();
			List<Song> songs = currentFolderData.currentFolder.getSongs();
			if (songs != null){
				for (Song song : songs){
					String path = song.getPath();
					if (!currentFolderData.parsedSongs.contains(path)){
						System.out.println("Removing old song "+song.getFilename());
						songsToRemove.add(song);
					}
				}
				currentFolderData.currentFolder.getSongs().removeAll(songsToRemove);
			}
			
			currentFoldersMap.remove(dirPathStr);
			
			
			return CONTINUE;
		}

		// If there is some error accessing
		// the file, let the user know.
		// If you don't override this method
		// and an error occurs, an IOException
		// is thrown.
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return CONTINUE;
		}
		
	}
}
