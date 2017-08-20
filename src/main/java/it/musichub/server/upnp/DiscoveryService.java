package it.musichub.server.upnp;

import it.musichub.server.runner.IMusicHubService;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.model.Device;

public interface DiscoveryService extends IMusicHubService {

	public Device getDevice(String udn) throws DeviceNotFoundException;
	public Device getDeviceByCustomName(String customName) throws DeviceNotFoundException;
	public boolean isDeviceOnline(Device device); //TODO nascondere??
	public boolean isDeviceOnline(String udn) throws DeviceNotFoundException;
	public void setDeviceCustomName(String udn, String customName) throws DeviceNotFoundException;

	public Device getSelectedDevice();
	public void setSelectedDevice(Device device); //TODO nascondere??
	public void setSelectedDevice(String udn) throws DeviceNotFoundException;
	public boolean isSelectedDeviceOnline() throws NoSelectedDeviceException;
	public void clearSelectedDevice();
}
