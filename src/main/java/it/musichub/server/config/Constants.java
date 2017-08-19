package it.musichub.server.config;

import java.io.Serializable;

public class Constants implements Serializable {

	/*
	 * GENERIC
	 */
	public static boolean verbose = false;
	public static Integer AUTOSLEEP_TIME = 300; //TOO decidere se metterlo nella config
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
	 * upnpdiscovery
	 */
	public static final String REGISTRY_FILE_NAME = "devices.xml";
	
	
}
