package example.binarylight;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

/**
 * Hello world!
 *
 */
public class Main {
	
	final static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		
		
		
		// UPnP discovery is asynchronous, we need a callback
		RegistryListener listener = new RegistryListener() {

			public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
				logger.info("Discovery started: " + device.getDisplayString());
			}

			public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
				logger.info("Discovery failed: " + device.getDisplayString() + " => " + ex);
			}

			public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
				logger.info("Remote device available: " + device.getDisplayString());
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

		// This will create necessary network resources for UPnP right away
		logger.info("Starting Cling...");
		UpnpService upnpService = new UpnpServiceImpl(listener);

		// Send a search message to all devices and services, they should
		// respond soon
		upnpService.getControlPoint().search(new STAllHeader());

		// Let's wait 20 seconds for them to respond
		logger.info("Waiting 20 seconds before shutting down...");
		Thread.sleep(20000);

		// Release all resources and advertise BYEBYE to other UPnP devices
		logger.info("Stopping Cling...");
		upnpService.shutdown();
	}

}
