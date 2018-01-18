package it.musichub.server.config;

import java.io.Serializable;

public class Constants implements Serializable {

	/*
	 * GENERIC
	 */
	public static String VERSION = "0.1";
	public static boolean verbose = false;
	public static final String CONFIG_FILE_NAME = "config.xml";
	
	/*
	 * persistence
	 */
	public static final String PATH_NAME = System.getProperty("java.io.tmpdir");//TODO mettere la home?o appdata?
	public static final String FOLDER_NAME = ".musichub";
	
	/*
	 * indexer
	 */
	public static final String LIBRARY_FILE_NAME = "library.xml";

	/*
	 * upnpcontroller
	 */
	public static final String UPNP_DEVICE_TYPE = "MediaRenderer";
	public static final String UPNP_SERVICE_TYPE_AVTRANSPORT = "AVTransport";
	public static final String UPNP_SERVICE_TYPE_RENDERINGCONTROL = "RenderingControl";
	public static final String REGISTRY_FILE_NAME = "devices.xml";
	
	/*
	 * rest
	 */
	public static final int DEFAULT_JSON_PAGINATION_LIMIT = 5;
}
