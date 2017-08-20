package it.musichub.server.upnp.ex;

/**
 * Device not found exception of Upnp components
 * 
 * @author sigitm
 */
public class DeviceNotFoundException extends UpnpException {

	public DeviceNotFoundException() {
		super();
	}

	public DeviceNotFoundException(String message) {
		super(message);
	}

	public DeviceNotFoundException(Throwable cause) {
		super(cause);
	}

	public DeviceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
