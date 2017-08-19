package it.musichub.server.upnp;

import it.musichub.server.library.model.Folder;
import it.musichub.server.runner.IMusicHubService;
import it.musichub.server.upnp.model.Device;

public interface DiscoveryService extends IMusicHubService {

	public Device getSelectedDevice();
	public void setSelectedDevice(Device device);
	public void clearSelectedDevice();
	
}
