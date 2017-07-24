package it.musichub.server.ex;

public class MusicHubException extends Exception {

	public MusicHubException() {
		super();
	}

	public MusicHubException(String message) {
		super(message);
	}

	public MusicHubException(Throwable cause) {
		super(cause);
	}

	public MusicHubException(String message, Throwable cause) {
		super(message, cause);
	}

}
