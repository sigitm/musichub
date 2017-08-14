package it.musichub.server.upnp.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import it.musichub.server.upnp.DiscoveryServiceImpl;

public class DeviceRegistry extends HashMap<String, Device> {

	private final static Logger logger = Logger.getLogger(DeviceRegistry.class);
	
//	public Device getDevice(String udn){
//		for (Device device : this){
//			if (udn != null && udn.equals(device.getUdn()))
//				return device;
//		}
//		return null;
//	}
//	
//	public boolean containsDevice(String udn){
//		return getDevice(udn) != null;
//	}
	
	public Device mergeDevice(Device device){
		String key = device.getUdn();
		if (!this.containsKey(key)){
			logger.info("Adding new device "+device.getFriendlyName()+" {"+device.getUdn()+"}");
			this.put(key, device);
		}else{
			logger.info("Updating device "+device.getFriendlyName()+" {"+device.getUdn()+"}");
			Device oldDevice = this.get(key);
			DeviceFactory.mergeFromDevice(oldDevice, device);
		}
		return this.get(key);
	}
	
}
