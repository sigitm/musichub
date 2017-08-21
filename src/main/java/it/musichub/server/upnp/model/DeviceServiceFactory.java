package it.musichub.server.upnp.model;

import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.Service;

public class DeviceServiceFactory {
	
	public static DeviceService fromClingDeviceService(Service clingService){
		DeviceService service = new DeviceService();
		
		service.setServiceId(clingService.getServiceId().getId());
		service.setServiceType(clingService.getServiceType().getType());
		
		return service;
	}
	
}
