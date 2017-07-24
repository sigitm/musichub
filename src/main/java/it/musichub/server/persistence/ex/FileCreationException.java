package it.musichub.server.persistence.ex;

import it.musichub.server.ex.MusicHubException;

/**
 * File creation exception during saving in PersistenceEngine
 * 
 * @author sigitm
 */
public class FileCreationException extends SaveException {

	public FileCreationException() {
		super();
	}

	public FileCreationException(String message) {
		super(message);
	}

	public FileCreationException(Throwable cause) {
		super(cause);
	}

	public FileCreationException(String message, Throwable cause) {
		super(message, cause);
	}

}
