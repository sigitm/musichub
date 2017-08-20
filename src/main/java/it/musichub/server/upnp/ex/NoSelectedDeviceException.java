package it.musichub.server.upnp.ex;

/**
 * No device selected exception of Upnp components
 * 
 * @author sigitm
 */
public class NoSelectedDeviceException extends UpnpException {

	public NoSelectedDeviceException() {
		super();
	}

	public NoSelectedDeviceException(String message) {
		super(message);
	}

	public NoSelectedDeviceException(Throwable cause) {
		super(cause);
	}

	public NoSelectedDeviceException(String message, Throwable cause) {
		super(message, cause);
	}

}
