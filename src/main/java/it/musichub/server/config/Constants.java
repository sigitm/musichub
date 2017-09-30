package it.musichub.server.config;

import java.io.Serializable;

public class Constants implements Serializable {

	/*
	 * GENERIC
	 */
	public static boolean verbose = false;
	public static Integer AUTOSLEEP_TIME = 600; //TOO decidere se metterlo nella config
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
	 * upnp generic
	 */

	public final static String UPNP_DEVICE_TYPE = "MediaRenderer";
	public final static String UPNP_SERVICE_TYPE_AVTRANSPORT = "AVTransport";
	public final static String UPNP_SERVICE_TYPE_RENDERINGCONTROL = "RenderingControl";
	
	/*
	 * upnpdiscovery
	 */
	public static final String REGISTRY_FILE_NAME = "devices.xml";
	
	
}
