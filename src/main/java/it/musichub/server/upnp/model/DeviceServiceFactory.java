package it.musichub.server.upnp.model;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceId;

public class DeviceServiceFactory {
	
	public static DeviceService fromClingDeviceService(RemoteService clingService){
		DeviceService service = new DeviceService();
		
		service.setNamespace(clingService.getServiceId().getNamespace());
		service.setServiceId(clingService.getServiceId().getId());
		service.setServiceType(clingService.getServiceType().getType());
		
		return service;
	}
	
	public static RemoteService toClingDeviceService(Device device, DeviceService service, UpnpService upnpService){
		RemoteDevice clingDevice = DeviceFactory.toClingDevice(device, upnpService);
		return clingDevice.findService(new ServiceId(service.getNamespace(), service.getServiceId()));
	}
}
