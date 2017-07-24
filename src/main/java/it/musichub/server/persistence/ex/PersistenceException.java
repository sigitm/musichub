package it.musichub.server.persistence.ex;

import it.musichub.server.ex.MusicHubException;

/**
 * Generic exception of PersistenceEngine
 * 
 * @author sigitm
 */
public class PersistenceException extends MusicHubException {

	public PersistenceException() {
		super();
	}

	public PersistenceException(String message) {
		super(message);
	}

	public PersistenceException(Throwable cause) {
		super(cause);
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
