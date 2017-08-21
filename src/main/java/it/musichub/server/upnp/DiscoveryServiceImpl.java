package it.musichub.server.upnp;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import it.musichub.server.config.Constants;
import it.musichub.server.persistence.PersistenceService;
import it.musichub.server.persistence.ex.FileNotFoundException;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceFactory;
import it.musichub.server.upnp.model.DeviceRegistry;
import it.musichub.server.upnp.model.DeviceService;

public class DiscoveryServiceImpl extends MusicHubServiceImpl implements DiscoveryService {

	/**
	 * Tengo i device in un mio deviceregistry e li aggiorno con il listener
	 * 
	 * deviceregistry mappa con l'uid come chiave? lo salvo tramite persistence?
	 * 
	 * li tengo tutti, anche i vecchi? 	 ci sarà anche un attributo "online" e "lastSeenOnline"
	 * 
	 * ...
	 * 
	 */
	private DeviceRegistry deviceRegistry = null;
	private UpnpService upnpService = null;
	
	private final static String DEVICE_TYPE = "MediaRenderer";
	private final static String SERVICE_TYPE = "AVTransport";
	
	private final static Logger logger = Logger.getLogger(DiscoveryServiceImpl.class);
	
	private static boolean isValidDevice(RemoteDevice device){
		return new UDADeviceType(DEVICE_TYPE).equals(device.getType()) && device.findService(new UDAServiceType(SERVICE_TYPE)) != null;
	}
	
	private PersistenceService getPersistenceService(){
		return (PersistenceService) ServiceFactory.getServiceInstance(Service.persistence);
	}
	
	private void loadFromDisk(){
		logger.info("Loading registry from file...");
		
		try {
			deviceRegistry = getPersistenceService().loadFromDisk(DeviceRegistry.class, Constants.REGISTRY_FILE_NAME);
		} catch(FileNotFoundException e) {
			logger.warn("Registry file not found. May be first launch.", e);
		    return;
		} catch(LoadException e) {
			logger.error("Error loading registry file", e);
		    return;
		}
	}
	
	private void saveToDisk(){
		logger.info("Saving registry to file...");
		
		try {
			getPersistenceService().saveToDisk(deviceRegistry, Constants.REGISTRY_FILE_NAME);
		} catch (SaveException e) {
			logger.error("Error saving registry", e);
			return;
		}
		
		logger.info("... registry saved.");
	}
	
	@Override
	public void init() {
		//init registry
		loadFromDisk();
		if (deviceRegistry == null)
			deviceRegistry = new DeviceRegistry();

		
		// UPnP discovery is asynchronous, we need a callback
		RegistryListener listener = new MediaRenderersListener();

		upnpService = new UpnpServiceImpl(listener);
	}

	@Override
	public void start() {
		// Send a search message to all devices and services, they should
		// respond soon
//		for (UDADeviceType udaType : getDeviceTypes())
			upnpService.getControlPoint().search(new UDADeviceTypeHeader(/*udaType*/new UDADeviceType(DEVICE_TYPE)));
	}

	@Override
	public void stop() {
		upnpService.shutdown();
		
		saveToDisk();
	}

	@Override
	public void destroy() {
		upnpService = null;
		deviceRegistry = null;
	}
	
	
	private class MediaRenderersListener implements RegistryListener {
		
		private void registerOnline(RemoteDevice device){
			Device dev = DeviceFactory.fromClingDevice(device);
			dev = deviceRegistry.mergeDevice(dev);
			dev.registerOnline();
		}
		
		private void registerOffline(RemoteDevice device){
			Device dev = DeviceFactory.fromClingDevice(device);
			dev = deviceRegistry.mergeDevice(dev);
			dev.registerOffline();
		}
		
		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
//			logger.info("Discovery started: " + device.getDisplayString());
		}

		public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
//			logger.info("Discovery failed: " + device.getDisplayString() + " => " + ex);
		}

		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
//			logger.debug("Remote device available: " + device.getDisplayString());
			if (isValidDevice(device)){
				registerOnline(device);
			}
		}

		public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
//			logger.debug("Remote device updated: " + device.getDisplayString());
			if (isValidDevice(device)){
				registerOnline(device);
			}
		}

		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
//			logger.debug("Remote device removed: " + device.getDisplayString());
			if (isValidDevice(device)){
				registerOffline(device);
			}
		}

		public void localDeviceAdded(Registry registry, LocalDevice device) {
//			logger.info("Local device added: " + device.getDisplayString());
		}

		public void localDeviceRemoved(Registry registry, LocalDevice device) {
//			logger.info("Local device removed: " + device.getDisplayString());
		}

		public void beforeShutdown(Registry registry) {
//			logger.info("Before shutdown, the registry has devices: " + registry.getDevices().size());
		}

		public void afterShutdown() {
//			logger.info("Shutdown of registry complete!");
		}
	};
	

	
	@Override
	public Device getSelectedDevice(){
		return deviceRegistry.getSelectedDevice();
	}
	
	@Override
	public void setSelectedDevice(String udn) throws DeviceNotFoundException{
		setSelectedDevice(getDevice(udn));
	}
	
	@Override
	public void setSelectedDevice(Device device){
		deviceRegistry.setSelectedDevice(device);
	}
	
	@Override
	public boolean isSelectedDeviceOnline() throws NoSelectedDeviceException{
		Device device = getSelectedDevice();
		if (device == null)
			throw new NoSelectedDeviceException();
		return isDeviceOnline(device);
	}
	
	@Override
	public void clearSelectedDevice(){
		deviceRegistry.clearSelectedDevice();
	}

	@Override
	public List<Device> getDevices(){
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXX usare gli streams o fare refactoring del registry
		return new ArrayList<Device>(deviceRegistry.values());
	}
	
	@Override
	public Device getDevice(String udn) throws DeviceNotFoundException {
		Device device = deviceRegistry.get(udn);
		if (device == null)
			throw new DeviceNotFoundException("Device "+udn+" not found");
		return device;
	}

	@Override
	public Device getDeviceByCustomName(String customName) throws DeviceNotFoundException {
		// TODO Auto-generated method stub
		return null;
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	}

	@Override
	public List<Device> getOnlineDevices(){
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXX usare gli streams o fare refactoring del registry
		List<Device> d = new ArrayList<Device>();
		for (Device device : deviceRegistry.values())
			d.add(device);
		return d;
	}
	
	@Override
	public boolean isDeviceOnline(Device device){
		return device.isOnline();
	}
	
	@Override
	public boolean isDeviceOnline(String udn) throws DeviceNotFoundException {
		return isDeviceOnline(getDevice(udn));
	}

	@Override
	public void setDeviceCustomName(String udn, String customName) throws DeviceNotFoundException {
		Device device = getDevice(udn);
		device.setCustomName(customName);
	}

	@Override
	public DeviceService getSelectedDeviceService(String serviceType) throws NoSelectedDeviceException {
		Device device = getSelectedDevice();
		DeviceService[] services = device.getServices();
		for (DeviceService service : services){
			if (service.getServiceType().equals(serviceType))
				return service;
		}
		return null;
	}
	
	

}
