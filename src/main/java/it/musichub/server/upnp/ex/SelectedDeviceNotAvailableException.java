package it.musichub.server.upnp.ex;

/**
 * Selected device not available exception of Upnp components
 * 
 * @author sigitm
 */
public class SelectedDeviceNotAvailableException extends UpnpException {

	public SelectedDeviceNotAvailableException() {
		super();
	}

	public SelectedDeviceNotAvailableException(String message) {
		super(message);
	}

	public SelectedDeviceNotAvailableException(Throwable cause) {
		super(cause);
	}

	public SelectedDeviceNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
