package it.musichub.server.upnp;

import org.apache.log4j.Logger;
import org.fourthline.cling.model.meta.RemoteService;

import it.musichub.server.config.Constants;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceService;
import it.musichub.server.upnp.model.DeviceServiceFactory;

public class ControllerServiceImpl extends MusicHubServiceImpl implements ControllerService {

	/**
	 * 
	 * ...
	 * 
	 */
	
	private final static Logger logger = Logger.getLogger(ControllerServiceImpl.class);
	
	private static DiscoveryService getDiscoveryService(){
		return (DiscoveryService) ServiceFactory.getServiceInstance(Service.upnpdiscovery);
	}
	

	public static RemoteService getRenderingControlService() {
		
		DiscoveryService ds = getDiscoveryService();
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX migliorare e togliere il try/catch
		try {
			if (!ds.isDeviceSelected() || !ds.isSelectedDeviceOnline())
				return null;
			
			Device device = ds.getSelectedDevice();
			DeviceService service = ds.getSelectedDeviceService(Constants.UPNP_SERVICE_TYPE_RENDERINGCONTROL);
			
			return DeviceServiceFactory.toClingDeviceService(device, service, ds.getUpnpService());
		} catch (NoSelectedDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static RemoteService getAVTransportService() {
		DiscoveryService ds = getDiscoveryService();
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX migliorare e togliere il try/catch
		try {
			if (!ds.isDeviceSelected() || !ds.isSelectedDeviceOnline())
				return null;
			
			Device device = ds.getSelectedDevice();
			DeviceService service = ds.getSelectedDeviceService(Constants.UPNP_SERVICE_TYPE_AVTRANSPORT);
			
			return DeviceServiceFactory.toClingDeviceService(device, service, ds.getUpnpService());
		} catch (NoSelectedDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	

	
	@Override
	public void init() {
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	}

	@Override
	public void start() {
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	}

	@Override
	public void stop() {
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	}

	@Override
	public void destroy() {
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	}
	

}
