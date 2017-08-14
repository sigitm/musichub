package it.musichub.server.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Configuration implements Serializable {

	/*
	 * GENERIC
	 */
	/**
	 * Activates the verbose mode.
	 */
	private static boolean verboseMode = false;	
	
	/*
	 * persistence
	 */
	
	/*
	 * indexer
	 */
	@Required
	private String contentDir;
	
	private Integer delayStart; //use this to delay MusicHub startup by a specified number of seconds before it starts reading the content directories. This can be useful when a content directory is on an external or networked disk which isn't yet mounted when MusicHub is started
	
	@Required
	private String startupScan = "true"; //If set to true (the default), MusicHub does a complete library scan when it is started. If set to full, this scan ignores any existing cache files. If set to false, no library scan is done when MusicHub is started. See the Reading audio files section for details. 

	
	
	/*
	 * GETTERS & SETTERS
	 */
	public static boolean isVerboseMode() {
		return verboseMode;
	}

	public static void setVerboseMode(boolean verboseMode) {
		Configuration.verboseMode = verboseMode;
	}

	public String getContentDir() {
		return contentDir;
	}

	public void setContentDir(String contentDir) {
		this.contentDir = contentDir;
	}

	public Integer getDelayStart() {
		return delayStart;
	}

	public void setDelayStart(Integer delayStart) {
		this.delayStart = delayStart;
	}

	public String getStartupScan() {
		return startupScan;
	}

	public void setStartupScan(String startupScan) {
		this.startupScan = startupScan;
	}
		
}
