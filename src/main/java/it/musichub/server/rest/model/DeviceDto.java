package it.musichub.server.rest.model;

import java.io.Serializable;
import java.util.Date;

public class DeviceDto implements Serializable {

	private String id;
	private String deviceType;
	private String customName;
	private String friendlyName;
	private String manifacturer;
	private String modelName;
	private	DeviceIconDto[] icons;
	
	private Boolean online;
	private Date lastSeenOnline;
	private Date lastUpdate;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getCustomName() {
		return customName;
	}
	public void setCustomName(String customName) {
		this.customName = customName;
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
	public DeviceIconDto[] getIcons() {
		return icons;
	}
	public void setIcons(DeviceIconDto[] icons) {
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
	
	@Override
	public String toString() {
		return "DeviceDto [id=" + id + ", deviceType=" + deviceType + ", customName=" + customName + ", friendlyName="
				+ friendlyName + ", manifacturer=" + manifacturer + ", modelName=" + modelName + ", online=" + online
				+ ", lastSeenOnline=" + lastSeenOnline + ", lastUpdate=" + lastUpdate + "]";
	}
	
}
