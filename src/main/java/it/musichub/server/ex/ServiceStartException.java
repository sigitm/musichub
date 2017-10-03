package it.musichub.server.ex;

public class ServiceStartException extends MusicHubException {

	public ServiceStartException() {
		super();
	}

	public ServiceStartException(String message) {
		super(message);
	}

	public ServiceStartException(Throwable cause) {
		super(cause);
	}

	public ServiceStartException(String message, Throwable cause) {
		super(message, cause);
	}

}
