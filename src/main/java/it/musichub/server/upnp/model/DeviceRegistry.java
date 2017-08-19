package it.musichub.server.upnp.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import it.musichub.server.upnp.DiscoveryServiceImpl;

public class DeviceRegistry extends HashMap<String, Device> {

	private String selectedDevice;
	
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
	
	public Device getSelectedDevice(){
		if (selectedDevice == null)
			return null;
		
		return get(selectedDevice);
	}
	
	public void setSelectedDevice(Device device){
		if (device == null || device.getUdn() == null || get(device) == null)
			throw new IllegalArgumentException("Invalid device "+device);
		
		this.selectedDevice = device.getUdn();
	}
	
	public void clearSelectedDevice(){
		this.selectedDevice = null;
	}
	
	@Override
	public Device remove(Object key) {
		Device result = super.remove(key);
		
		if (result != null && result.getUdn() != null && result.getUdn().equals(selectedDevice))
			clearSelectedDevice();
		
		return result;
	}
	
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
