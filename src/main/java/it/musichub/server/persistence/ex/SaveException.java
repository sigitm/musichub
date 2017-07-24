package it.musichub.server.persistence.ex;

import it.musichub.server.ex.MusicHubException;

/**
 * Saving exception of PersistenceEngine
 * 
 * @author sigitm
 */
public class SaveException extends PersistenceException {

	public SaveException() {
		super();
	}

	public SaveException(String message) {
		super(message);
	}

	public SaveException(Throwable cause) {
		super(cause);
	}

	public SaveException(String message, Throwable cause) {
		super(message, cause);
	}

}
