package it.musichub.server.discovery.model;

import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;

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
		
		return device;
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
