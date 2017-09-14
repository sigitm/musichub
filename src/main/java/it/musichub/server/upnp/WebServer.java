package it.musichub.server.upnp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.seamless.util.MimeType;

import fi.iki.elonen.NanoHTTPD;
import it.musichub.server.config.Configuration;
import it.musichub.server.library.IndexerService;
import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.FolderFactory;
import it.musichub.server.library.model.Song;
import it.musichub.server.library.utils.FileUtils;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;

public class WebServer extends NanoHTTPD {

	private Integer port = getConfiguration().getMediaHttpPort();
	
	private final static Logger logger = Logger.getLogger(HttpServerServiceImpl.class);
	
	public WebServer(int port) {
		super(port);
	}
//	public WebServer(String hostname, int port) {
//		super(hostname, port);
//		// TODO Auto-generated constructor stub
//	}
	
	public static final String CONTEXT_MUSIC = "music";
	public static final String CONTEXT_ALBUM_ART = "art";
	
	//TODO xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx farli richiamare dal UpnpService passato in input?
	public static Configuration getConfiguration(){
		return ServiceFactory.getInstance().getConfiguration();
	}
	private IndexerService getIndexerService(){
		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
	}
	
	@Override
	public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
		
		String context = uri.substring(1, uri.indexOf("/", uri.indexOf("/")+1));
		String path = uri.substring(uri.indexOf("/", uri.indexOf("/")+1)+1);
		String folderPath = FileUtils.extractPath(path);
		String filename = FileUtils.extractFilename(path);
		
		Folder root = getIndexerService().getStartingFolder();
		Song song = null;
		
		Folder folderTemplate = FolderFactory.fromRelativePath(folderPath, root);
		Folder folder = root.getFolderRecursive(folderTemplate);
		if (folder != null){
			song = folder.getSong(filename);
		}
		
		if (song != null){
			//song found; preparing right response
			if (CONTEXT_MUSIC.equals(context))
				return serveSong(song);
			if (CONTEXT_ALBUM_ART.equals(context))
				return serveAlbumArt(song);
			
			return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Forbidden: unknown context /"+context);
		}else{
			//song not found
			String hostName = null;
			try {
				hostName = InetAddress.getLocalHost().toString();
			} catch (UnknownHostException e) {
				logger.warn("Cannot determine hostname", e);
			}
			String response = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">"
			+"<html><head>"
			+"<title>404 Not Found</title>"
			+"</head><body>"
			+"<h1>Not Found</h1>"
			+"<p>The requested URL "+path+" was not found on this server.</p>"
			+"<hr>"
			+"<address>MusicHub Server at "+hostName+" Port "+port+"</address>"
			+"</body></html>";
			return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_HTML, response);
		}
	}
	
	private Response serveSong(Song song) {
		if (song.getFile().exists()){
			try {
				File file = song.getFile();
				byte[] bytearray = new byte[(int) file.length()];
				FileInputStream fis = new FileInputStream(file);
				
				BufferedInputStream bis = new BufferedInputStream(fis);
				bis.read(bytearray, 0, bytearray.length);
				
				String mimeType = new MimeType("audio", "mpeg").toString();
				
				return newFixedLengthResponse(Response.Status.OK, mimeType, bis, bytearray.length);
			} catch (IOException e) {
				logger.error("serveSong: IOException", e);
				return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal error: IOException while loading song");
			}
		}
		return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal error: song file not found");
	}
	
	private Response serveAlbumArt(Song song) {
		byte[] albumArt = song.getAlbumImage();
		if (albumArt != null){
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(albumArt);
				
				String mimeType = MimeType.valueOf(song.getAlbumImageMimeType()).toString();
				
				return newFixedLengthResponse(Response.Status.OK, mimeType, bais, albumArt.length);
			} catch (Exception e) {
				logger.error("serveAlbumArt: Exception", e);
				return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal error: Exception while loading album art");
			}
		}
		return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal error: song album art not found");
	}
	
	private String getSongUrl(String context, Song song){
		try {
			String host = InetAddress.getLocalHost().toString();
			String path = context+"/"+song.getPath()+"/"+song.getFilename();
			return new URL("http", host, port, path).toExternalForm();
		} catch (UnknownHostException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String getSongFileUrl(Song song){
		return getSongUrl(CONTEXT_MUSIC, song);
	}
	
	public String getSongAlbumArtUrl(Song song){
		return getSongUrl(CONTEXT_ALBUM_ART, song);
	}

}
