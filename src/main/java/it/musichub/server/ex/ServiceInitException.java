package it.musichub.server.ex;

public class ServiceInitException extends MusicHubException {

	public ServiceInitException() {
		super();
	}

	public ServiceInitException(String message) {
		super(message);
	}

	public ServiceInitException(Throwable cause) {
		super(cause);
	}

	public ServiceInitException(String message, Throwable cause) {
		super(message, cause);
	}

}
