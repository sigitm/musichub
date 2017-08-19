package it.musichub.server.upnp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import it.musichub.server.library.IndexerService;
import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.FolderFactory;
import it.musichub.server.library.model.Song;
import it.musichub.server.library.utils.FileUtils;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
public class HttpServerServiceImpl extends MusicHubServiceImpl implements HttpServerService {

	/**
	 * TODO decidere per la bufferizzazione (v.commento nel MusicHandler)
	 * 
	 * - vedere su minimserver anche come si comporta in caso di 404
	 */
	
	private Integer port = null;
	
	private HttpServer server;
	
	private final static Logger logger = Logger.getLogger(HttpServerServiceImpl.class);
	
	public HttpServerServiceImpl() {
		super();
		this.port = getConfiguration().getMediaHttpPort();
	}
	
	private IndexerService getIndexerService(){
		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
	}
	
	@Override
	public void init() {
		//init http server
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			logger.error("Error creating Upnp http server on port "+port, e);
			return;
			//TODO xxxxxxxxxxxxxxxxxxx INIT FALLITA
		}
		server.createContext("/", new MusicHandler());
		server.setExecutor(null);
	}

	@Override
	public void start() {
		server.start();
		logger.info("Upnp http server started on port "+port);
	}

	@Override
	public void stop() {
		server.stop(0);
		logger.info("Upnp http server stopped");
	}

	@Override
	public void destroy() {
		server = null;
	}
	
	private class MusicHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {

			String path = t.getRequestURI().getPath();
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
				
				// add the required response header for a MP3 file
				Headers h = t.getResponseHeaders();
				h.add("Content-Type", "audio/mpeg");
				
				File file = song.getFile();
				byte[] bytearray = new byte[(int) file.length()];
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				bis.read(bytearray, 0, bytearray.length);

				// ok, we are ready to send the response.
				t.sendResponseHeaders(HttpURLConnection.HTTP_OK, file.length());
				OutputStream os = t.getResponseBody();
				os.write(bytearray, 0, bytearray.length);
				os.close();
				
				
				//TODO la versione sotto bufferizza il file, ma non fornisce la durata totale su firefox... confrontare con minimserver
				
//				//Reading the whole of each file into memory would not be scalable, so it is better to perform the copying in chunks.
//				
//				// Object exists and is a file: accept with response code 200.
//				t.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
//				OutputStream os = t.getResponseBody();
//				FileInputStream fs = new FileInputStream(file);
//				final byte[] buffer = new byte[0x10000];
//				int count = 0;
//				while ((count = fs.read(buffer)) >= 0) {
//					os.write(buffer, 0, count);
//				}
//				fs.close();
//				os.close();
			}else{
				//song not found
				String hostName = t.getLocalAddress().getHostName();
				String response = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">"
				+"<html><head>"
				+"<title>404 Not Found</title>"
				+"</head><body>"
				+"<h1>Not Found</h1>"
				+"<p>The requested URL "+path+" was not found on this server.</p>"
				+"<hr>"
				+"<address>MusicHub Server at "+hostName+" Port "+port+"</address>"
				+"</body></html>";
				t.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
			}
			
			t.close();
		}
	}	
	

}
