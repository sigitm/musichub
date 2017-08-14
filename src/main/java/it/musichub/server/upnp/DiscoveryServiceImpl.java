package it.musichub.server.upnp;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.FolderFactory;
import it.musichub.server.persistence.PersistenceService;
import it.musichub.server.persistence.ex.FileNotFoundException;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceFactory;
import it.musichub.server.upnp.model.DeviceRegistry;

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
	
	private static final String REGISTRY_FILE_NAME = "devices.xml";
	
	private final static Logger logger = Logger.getLogger(DiscoveryServiceImpl.class);
	
	private static List<UDADeviceType> getDeviceTypes(){
		return Arrays.asList(new UDADeviceType[]{new UDADeviceType("MediaRenderer")});
	}
	
	private PersistenceService getPersistenceService(){
		return (PersistenceService) ServiceFactory.getServiceInstance(Service.persistence);
	}
	
	private void loadFromDisk(){
		logger.info("Loading registry from file...");
		
		try {
			deviceRegistry = getPersistenceService().loadFromDisk(DeviceRegistry.class, REGISTRY_FILE_NAME);
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
			getPersistenceService().saveToDisk(deviceRegistry, REGISTRY_FILE_NAME);
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
		for (UDADeviceType udaType : getDeviceTypes())
			upnpService.getControlPoint().search(new UDADeviceTypeHeader(udaType));
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

		private boolean isValidDevice(RemoteDevice device){
			return getDeviceTypes().contains(device.getType());
		}
		
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

}
