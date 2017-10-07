package it.musichub.server.upnp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import fi.iki.elonen.NanoHTTPD;
import it.musichub.server.config.Constants;
import it.musichub.server.ex.ServiceDestroyException;
import it.musichub.server.ex.ServiceInitException;
import it.musichub.server.ex.ServiceStartException;
import it.musichub.server.ex.ServiceStopException;
import it.musichub.server.library.IndexerService;
import it.musichub.server.library.model.Folder;
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
import it.musichub.server.upnp.model.IPlaylistState;
import it.musichub.server.upnp.model.UpnpFactory;
import it.musichub.server.upnp.renderer.IRendererCommand;
import it.musichub.server.upnp.renderer.IRendererState;

public class UpnpControllerServiceImpl extends MusicHubServiceImpl implements UpnpControllerService {

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
	private IRendererState rendererState = null;
	private IRendererCommand rendererCommand = null;
	private MediaServer mediaServer = null;
	
	private final static Logger logger = Logger.getLogger(UpnpControllerServiceImpl.class);

	@Override
	public UpnpService getUpnpService() {
		return upnpService;
	}
	
	@Override
	public DeviceRegistry getDeviceRegistry() {
		return deviceRegistry;
	}
	
	@Override
	public IRendererState getRendererState(){ //TODO provvisorio
		return rendererState;
	}
	
	@Override
	public IRendererCommand getRendererCommand(){ //TODO provvisorio
		return rendererCommand;
	}
	
	public MediaServer getMediaServer(){ //TODO provvisorio???
		return mediaServer;
	}
	
	private static boolean isValidDevice(RemoteDevice device){
		return new UDADeviceType(Constants.UPNP_DEVICE_TYPE).equals(device.getType());
	}
	
	private PersistenceService getPersistenceService(){
		return (PersistenceService) ServiceFactory.getServiceInstance(Service.persistence);
	}
	
	private IndexerService getIndexerService(){
		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
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
	public void init() throws ServiceInitException {
		//init registry
		loadFromDisk();
		if (deviceRegistry == null)
			deviceRegistry = new DeviceRegistry();
		deviceRegistry.resetOnlines();
		
		//creating UPnP discovery with a callback
		RegistryListener listener = new MediaRenderersListener();
		upnpService = new UpnpServiceImpl(listener);
		
		//creating renderer state and command
		rendererState = UpnpFactory.createRendererState();
		rendererCommand = UpnpFactory.createRendererCommand(upnpService.getControlPoint(), rendererState);
		
		//creating http server
		mediaServer = new MediaServer(getConfiguration().getMediaHttpPort());
	}

	@Override
	public void start() throws ServiceStartException {
		//init http server
        try {
        	mediaServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
   			throw new ServiceStartException("Could not start media server on port "+getConfiguration().getMediaHttpPort(), e);
        }
        
		// Send a search message to all devices and services, they should respond soon
//		for (UDADeviceType udaType : getDeviceTypes())
			upnpService.getControlPoint().search(new UDADeviceTypeHeader(/*udaType*/new UDADeviceType(Constants.UPNP_DEVICE_TYPE)));

        

        
        //EXPERIMENT ---------------------------------------------
//        try {
//			TimeUnit.SECONDS.sleep(2);
//		} catch (Exception e) {}
//        
//        
//        Collection<Device> devices = deviceRegistry.values();
//        for (Device device : devices){
//        	if (isDeviceOnline(device)){
//        		setSelectedDevice(device);
//        		logger.fatal("Selected device "+device.getFriendlyName());
//        	}
//        }
//        if (!isDeviceSelected()){
//	        try {
//	        	logger.fatal("Manually selecting device...");
//				setSelectedDevice("uuid:5f9ec1b3-ed59-1900-4530-00a0deb52729");
//			} catch (DeviceNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//        }

//        rendererCommand.resume();
        try {
			TimeUnit.SECONDS.sleep(4);
		} catch (Exception e) {}
        
        IndexerService is = getIndexerService();
        Folder root = is.getStartingFolder();
//      Song song0 = root.getSongs().get(0);
        
        IPlaylistState playlist = rendererState.getPlaylist();
        playlist.addFolder(root, false);
        

        
	}

	@Override
	public void stop() throws ServiceStopException {
		upnpService.shutdown();
		
		mediaServer.stop();
		
		saveToDisk();
	}

	@Override
	public void destroy() throws ServiceDestroyException {
		upnpService = null;
		rendererState = null;
		rendererCommand = null;
		
		mediaServer = null;
		
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
	public boolean isDeviceSelected(){
		return deviceRegistry.getSelectedDevice() != null;
	}
	
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
		rendererState.reset();
		rendererCommand.resumeUpdates();
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
		rendererState.reset();
		rendererCommand.pauseUpdates();
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
