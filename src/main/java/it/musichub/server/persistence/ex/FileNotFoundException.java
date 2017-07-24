package it.musichub.server.persistence.ex;

import it.musichub.server.ex.MusicHubException;

/**
 * File not found exception during loading in PersistenceEngine
 * 
 * @author sigitm
 */
public class FileNotFoundException extends LoadException {

	public FileNotFoundException() {
		super();
	}

	public FileNotFoundException(String message) {
		super(message);
	}

	public FileNotFoundException(Throwable cause) {
		super(cause);
	}

	public FileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
