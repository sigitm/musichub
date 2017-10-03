package it.musichub.server.ex;

public class ServiceDestroyException extends MusicHubException {

	public ServiceDestroyException() {
		super();
	}

	public ServiceDestroyException(String message) {
		super(message);
	}

	public ServiceDestroyException(Throwable cause) {
		super(cause);
	}

	public ServiceDestroyException(String message, Throwable cause) {
		super(message, cause);
	}

}
