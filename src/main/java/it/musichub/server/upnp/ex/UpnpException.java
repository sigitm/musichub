package it.musichub.server.upnp.ex;

import it.musichub.server.ex.MusicHubException;

/**
 * Generic exception of Upnp components
 * 
 * @author sigitm
 */
public class UpnpException extends MusicHubException {

	public UpnpException() {
		super();
	}

	public UpnpException(String message) {
		super(message);
	}

	public UpnpException(Throwable cause) {
		super(cause);
	}

	public UpnpException(String message, Throwable cause) {
		super(message, cause);
	}

}
