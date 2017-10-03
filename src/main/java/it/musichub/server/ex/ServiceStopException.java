package it.musichub.server.ex;

public class ServiceStopException extends MusicHubException {

	public ServiceStopException() {
		super();
	}

	public ServiceStopException(String message) {
		super(message);
	}

	public ServiceStopException(Throwable cause) {
		super(cause);
	}

	public ServiceStopException(String message, Throwable cause) {
		super(message, cause);
	}

}
