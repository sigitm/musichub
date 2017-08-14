package it.musichub.server.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import it.musichub.server.config.Constants;
import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;
import it.musichub.server.persistence.ex.FileCreationException;
import it.musichub.server.persistence.ex.FileNotFoundException;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceIcon;
import it.musichub.server.upnp.model.DeviceRegistry;

public class PersistenceEngine extends MusicHubServiceImpl implements PersistenceService {
	
	/*
	 * EVOLUZIONI:
	 * v. saveToDisk
	 */
	private boolean init = false;
	
	private XStream xstream;
	
	private final static Logger logger = Logger.getLogger(PersistenceEngine.class);
	
	public PersistenceEngine() {
		super();
	}
	
	@Override
	public void init(){
		//init xstream
		xstream = new XStream();
		XStream.setupDefaultSecurity(xstream);
		xstream.allowTypeHierarchy(Folder.class);
		xstream.allowTypeHierarchy(Song.class);
		xstream.allowTypeHierarchy(DeviceRegistry.class);
		xstream.allowTypeHierarchy(Device.class);
		xstream.allowTypeHierarchy(DeviceIcon.class);
//		xstream.addPermission(AnyTypePermission.ANY);
		xstream.alias("folder", Folder.class);
		xstream.alias("song", Song.class);
		
		init = true;
	}
	
	@Override
	public void start(){
	}
	
	@Override
	public void stop(){
		if (!init)
			throw new IllegalStateException("init phase not executed");

	}
	
	@Override
	public void destroy(){
		if (!init)
			throw new IllegalStateException("init phase not executed");
		
	}
	
	private static String getLibraryPath(String fileName){
		return Constants.PATH_NAME + File.separator + Constants.FOLDER_NAME + File.separator + fileName;
	}
	
	public <T> T loadFromDisk(Class<T> clazz, String fileName) throws LoadException{
		String path = getLibraryPath(fileName);
		File file = new File(path);
		
		T loadedObject = null;
		if (file.exists()){
			logger.debug("Loading object of class "+clazz.getSimpleName()+" from file "+fileName+"...");

			try {
				loadedObject = (T)xstream.fromXML(file);
			} catch(Exception e) {
				throw new LoadException("Error loading object of class "+clazz.getSimpleName()+" from file "+fileName, e);
			}
			
		}else{
			throw new FileNotFoundException("Error loading object of class "+clazz.getSimpleName()+" from file "+fileName+": file not found");
		}
		
		return loadedObject;
	}
	
	public <T> void saveToDisk(T object, String fileName) throws SaveException{
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
		String xml = xstream.toXML(object);
		
		String path = getLibraryPath(fileName);
		File file = new File(path);
	
		try {
			file.getParentFile().mkdirs(); //crea la cartella se non esiste già
			file.createNewFile(); //crea il file se non esiste già
		} catch (IOException e) {
			throw new FileCreationException("Error opening file "+fileName+" for save", e);
		} 
		
		FileOutputStream fos = null;
		try {
			
		    fos = new FileOutputStream(file, false);
		    fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(StandardCharsets.UTF_8)); //write XML header, as XStream doesn't do that for us
		    byte[] bytes = xml.getBytes("UTF-8");
		    fos.write(bytes);

		} catch(Exception e) {
			throw new SaveException("Error saving file "+fileName, e);
		} finally {
		    if(fos!=null) {
		        try{ 
		            fos.close();
		        } catch (IOException e) {
		        	logger.warn("Error closing library file after saving", e);
		        }
		    }
		}
	}
}
	
