package it.musichub.server.upnp;

import java.util.List;

import org.apache.commons.logging.Log;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import it.musichub.server.runner.IMusicHubService;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.ex.UpnpException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceService;

public interface ControllerService extends IMusicHubService {

	/*
	 * metodi per controllare il selecteddevice
	 * 
	 */
	public void commandPlay() throws UpnpException;
	public void commandStop() throws UpnpException;
	public void commandPause() throws UpnpException;
	public void commandSeek(String relativeTimeTarget) throws UpnpException;
	public void setVolume(final int volume) throws UpnpException;
	public void setMute(final boolean mute) throws UpnpException;
}
