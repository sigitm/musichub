package it.musichub.server.upnp;

import java.util.List;

import it.musichub.server.runner.IMusicHubService;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceService;

public interface DiscoveryService extends IMusicHubService {

	/*
	 * nuova proposta
	 * 
	 * metodi per ottenere la lista dei device, settare il selectedDevice, 
	 * (POTREI FARE UN'INTERFACCIA INTERNAL PER L'USO INTERNO....SOLO DA ALTRI SERVIZI)
	 */
	public List<Device> getDevices();
	public Device getDevice(String udn) throws DeviceNotFoundException;
	public Device getDeviceByCustomName(String customName) throws DeviceNotFoundException;
	public List<Device> getOnlineDevices();
	public boolean isDeviceOnline(Device device); //TODO nascondere??
	public boolean isDeviceOnline(String udn) throws DeviceNotFoundException;
	public void setDeviceCustomName(String udn, String customName) throws DeviceNotFoundException;

	public Device getSelectedDevice();
	public void setSelectedDevice(Device device); //TODO nascondere??
	public void setSelectedDevice(String udn) throws DeviceNotFoundException;
	public void clearSelectedDevice();
	public boolean isSelectedDeviceOnline() throws NoSelectedDeviceException;
	public DeviceService getSelectedDeviceService(String serviceType) throws NoSelectedDeviceException;
}
