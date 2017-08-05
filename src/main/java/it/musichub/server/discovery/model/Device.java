package it.musichub.server.discovery.model;

import java.io.Serializable;
import java.util.Date;

public class Device implements Serializable {

	private String udn;
	private String deviceType;
	private String friendlyName;
	private String manifacturer;
	private String modelName;
	private	DeviceIcon[] icons;
	
	private boolean online = false;
	private Date lastSeenOnline = null;
	private Date lastUpdate = null;
	
	public String getUdn() {
		return udn;
	}
	public void setUdn(String udn) {
		this.udn = udn;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getFriendlyName() {
		return friendlyName;
	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	public String getManifacturer() {
		return manifacturer;
	}
	public void setManifacturer(String manifacturer) {
		this.manifacturer = manifacturer;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public DeviceIcon[] getIcons() {
		return icons;
	}
	public void setIcons(DeviceIcon[] icons) {
		this.icons = icons;
	}
	public boolean isOnline() {
		return online;
	}
	public void setOnline(boolean online) {
		this.online = online;
	}
	public Date getLastSeenOnline() {
		return lastSeenOnline;
	}
	public void setLastSeenOnline(Date lastSeenOnline) {
		this.lastSeenOnline = lastSeenOnline;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	
	public void registerOnline(){
		setOnline(true);
		setLastSeenOnline(new Date());
		setLastUpdate(new Date());
	}
	
	public void registerOffline(){
		setOnline(false);
		setLastUpdate(new Date());
	}
}
