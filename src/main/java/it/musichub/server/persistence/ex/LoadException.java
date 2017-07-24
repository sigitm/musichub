package it.musichub.server.persistence.ex;

/**
 * Loading exception of PersistenceEngine
 * 
 * @author sigitm
 */
public class LoadException extends PersistenceException {

	public LoadException() {
		super();
	}

	public LoadException(String message) {
		super(message);
	}

	public LoadException(Throwable cause) {
		super(cause);
	}

	public LoadException(String message, Throwable cause) {
		super(message, cause);
	}

}
