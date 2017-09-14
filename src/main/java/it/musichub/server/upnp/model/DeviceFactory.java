package it.musichub.server.upnp.model;

import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.UDN;

public class DeviceFactory {
	
	public static Device fromClingDevice(RemoteDevice clingDevice){
		Device device = new Device();

		device.setUdn(clingDevice.getIdentity().getUdn().toString());
		device.setDeviceType(clingDevice.getType().toString());
		device.setFriendlyName(clingDevice.getDisplayString());
		device.setManifacturer(clingDevice.getDetails().getManufacturerDetails().getManufacturer());
		device.setModelName(clingDevice.getDetails().getModelDetails().getModelName());
		if (clingDevice.getIcons() != null && clingDevice.getIcons().length > 0){
			List<DeviceIcon> icons = new ArrayList<>();
			for (Icon clingIcon : clingDevice.getIcons()){
				DeviceIcon icon = new DeviceIcon(clingIcon.getMimeType().toString(), clingIcon.getWidth(),
						clingIcon.getHeight(), clingIcon.getDepth(), clingIcon.getUri(), clingIcon.getData());
				icons.add(icon);
			}
			device.setIcons(icons.toArray(new DeviceIcon[]{}));
		}
		RemoteService[] clingServices = clingDevice.findServices();
		if (clingServices != null && clingServices.length > 0){
			List<DeviceService> services = new ArrayList<>();
			for (RemoteService clingService : clingServices){
				DeviceService service = DeviceServiceFactory.fromClingDeviceService(clingService);
				services.add(service);
			}
			device.setServices(services.toArray(new DeviceService[]{}));
		}
		
		return device;
	}
	
	public static RemoteDevice toClingDevice(Device device, UpnpService upnpService){
		return upnpService.getRegistry().getRemoteDevice(UDN.valueOf(device.getUdn()), true);
	}
	
	public static void mergeFromDevice(Device oldDevice, Device newDevice){
		oldDevice.setUdn(newDevice.getUdn());
		oldDevice.setDeviceType(newDevice.getDeviceType());
		oldDevice.setFriendlyName(newDevice.getFriendlyName());
		oldDevice.setManifacturer(newDevice.getManifacturer());
		oldDevice.setModelName(newDevice.getModelName());
		oldDevice.setIcons(newDevice.getIcons());
	}
	
}
