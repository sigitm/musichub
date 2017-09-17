package it.musichub.server.upnp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
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

	private final static Logger logger = Logger.getLogger(HttpServerServiceImpl.class);
	
	private String hostname = null;
	private int port = -1;
	
	public WebServer(int port) {
		super(port);
		this.hostname = getHostAddress();
		this.port = port;
	}

	public WebServer(String hostname, int port) {
		super(hostname, port);
		this.hostname = hostname;
		this.port = port;
	}
	
	private String getHostAddress(){
		String host = null;
		try {
			DatagramSocket socket = new DatagramSocket();
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			host = socket.getLocalAddress().getHostAddress();
			socket.close();
		} catch (SocketException | UnknownHostException e) {
			logger.warn("Error retrieving host name by choosing default route address", e);
		}
		if (host == null){
			try {
				host = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				logger.warn("Error retrieving host name by looking for localhost", e);
			}
		}

		return host;
	}
	
	
	public static final String CONTEXT_MUSIC = "/music";
	public static final String CONTEXT_ALBUM_ART = "/art";
	
	private IndexerService getIndexerService(){
		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
	}
	
	private static String getAlbumArtFileName(){
		return "picture.jpg";
	}
	
	@Override
	public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
		
		String context = uri.substring(0, uri.indexOf("/", uri.indexOf("/")+1));
		String originalPath = uri.substring(uri.indexOf("/", uri.indexOf("/")+1)+1);
		String path = originalPath;
		
		//ALBUM-ART
		if (CONTEXT_ALBUM_ART.equals(context)){
			String imageUrlPart = "/"+getAlbumArtFileName();
			if (path.indexOf(imageUrlPart) < 0)
				return getNotFoundResponse(originalPath);
			path = path.substring(0, path.indexOf(imageUrlPart));
		}
		
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
				return serveSong(headers, song);
			if (CONTEXT_ALBUM_ART.equals(context))
				return serveAlbumArt(song);
			
			return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Forbidden: unknown context /"+context);
		}else{
			//song not found
			return getNotFoundResponse(originalPath);
		}
	}
	
	private Response serveSong(Map<String, String> headers, Song song) {
		if (song.getFile().exists()){
			try {
				File file = song.getFile();
//				byte[] bytearray = new byte[(int) file.length()];
//				FileInputStream fis = new FileInputStream(file);
//				
//				BufferedInputStream bis = new BufferedInputStream(fis);
//				bis.read(bytearray, 0, bytearray.length);
				
				String mimeType = new MimeType("audio", "mpeg").toString();
				
//				return newFixedLengthResponse(Response.Status.OK, mimeType, bis, bytearray.length);
				return serveFile(headers, file, mimeType);
			} catch (Exception e) {//TODO XXX catch inutile!!!!!!!!
				logger.error("serveSong: IOException", e);
				return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal error: IOException while loading song");
			}
		}
		return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal error: song file not found");
	}
	
	Response serveFileSimple(Map<String, String> header, File file, String mime) {
		Response res;
		try {
			InputStream stream = new FileInputStream(file);
			res = newFixedLengthResponse(Response.Status.OK, mime, stream, file.length());
		} catch (FileNotFoundException e) {
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
					"Internal error: file not found");
		}
		return res;
	}
	
    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI,
     * ignores all headers and HTTP parameters.
     */
    Response serveFile(Map<String, String> header, File file, String mime) {
		Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && (ifNoneMatch.equals("*") || ifNoneMatch.equals(etag));

            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();

            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    fis.skip(startFrom);

                    res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes */" + fileLen);
                    res.addHeader("ETag", etag);
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified

                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    // supply the file
                    res = newFixedFileResponse(file, mime);
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse("Reading file failed.");
        }

        return res;
    }

    private Response newFixedFileResponse(File file, String mime) throws FileNotFoundException {
        Response res;
        res = newFixedLengthResponse(Response.Status.OK, mime, new FileInputStream(file), (int) file.length());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
    
    protected Response getNotFoundResponse(String path){
		String response = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">"
		+"<html><head>"
		+"<title>404 Not Found</title>"
		+"</head><body>"
		+"<h1>Not Found</h1>"
		+"<p>The requested URL "+path+" was not found on this server.</p>"
		+"<hr>"
		+"<address>MusicHub Server at "+hostname+" Port "+port+"</address>"
		+"</body></html>";
		return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_HTML, response);
    }
    
    protected Response getForbiddenResponse(String s) {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
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
	
	private String getSongUrl(String context, String suffix, Song song){
		try {
			String path = context+"/"+song.getFolder().getRelativePath()+song.getFilename();
			if (suffix != null)
				path += suffix;
			URI uri = new URI("http", null, hostname, port, path, null, null);
			String songUrl = uri.toASCIIString();
			return songUrl;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String getSongFileUrl(Song song){
		return getSongUrl(CONTEXT_MUSIC, null, song);
	}
	
	public String getSongAlbumArtUrl(Song song){
		if (song.getAlbumImage() == null)
			return "";
		
		return getSongUrl(CONTEXT_ALBUM_ART, "/"+getAlbumArtFileName(), song);
	}

}
