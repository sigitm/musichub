package it.musichub.server.discovery;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

public class DiscoveryServiceImpl implements DiscoveryService {

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
	private UpnpService upnpService = null;
	
	private final static Logger logger = Logger.getLogger(DiscoveryServiceImpl.class);
	
	public static void main(String[] args) {
		DiscoveryServiceImpl d = new DiscoveryServiceImpl();
		
		logger.info("*** init ***");
		d.init();
		
		logger.info("*** start ***");
		d.start();
		
		// Let's wait 30 seconds for them to respond
		logger.info("Waiting 30 seconds before shutting down...");
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("*** stop ***");
		d.stop();
		
		logger.info("*** destroy ***");
		d.destroy();
		
	}
	
	
	@Override
	public void init() {
		// UPnP discovery is asynchronous, we need a callback
		RegistryListener listener = new MediaRenderersListener();

		upnpService = new UpnpServiceImpl(listener);
	}

	@Override
	public void start() {
		// Send a search message to all devices and services, they should
		// respond soon
		UDADeviceType udaType = new UDADeviceType("MediaRenderer");
		upnpService.getControlPoint().search(new UDADeviceTypeHeader(udaType));
	}

	@Override
	public void stop() {
		upnpService.shutdown();
	}

	@Override
	public void destroy() {
		upnpService = null;
	}
	
	
	private class MediaRenderersListener implements RegistryListener {

		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
			logger.info("Discovery started: " + device.getDisplayString());
			logger.fatal("remoteDeviceDiscoveryStarted"+device.getDisplayString()+" is "+device.getType().equals(new UDADeviceType("MediaRenderer")));
		}

		public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
			logger.info("Discovery failed: " + device.getDisplayString() + " => " + ex);
		}

		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
			logger.info("Remote device available: " + device.getDisplayString());
			logger.fatal("remoteDeviceAdded"+device.getDisplayString()+" is "+device.getType().equals(new UDADeviceType("MediaRenderer")));
		}

		public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
			logger.info("Remote device updated: " + device.getDisplayString());
		}

		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			logger.info("Remote device removed: " + device.getDisplayString());
		}

		public void localDeviceAdded(Registry registry, LocalDevice device) {
			logger.info("Local device added: " + device.getDisplayString());
		}

		public void localDeviceRemoved(Registry registry, LocalDevice device) {
			logger.info("Local device removed: " + device.getDisplayString());
		}

		public void beforeShutdown(Registry registry) {
			logger.info("Before shutdown, the registry has devices: " + registry.getDevices().size());
		}

		public void afterShutdown() {
			logger.info("Shutdown of registry complete!");

		}
	};

}
