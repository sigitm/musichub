package it.musichub.server.upnp.model;

import java.io.Serializable;
import java.util.Date;

import org.fourthline.cling.model.meta.Service;

public class DeviceService implements Serializable {

	private String namespace;
	private String serviceId;
	private String serviceType;
	
	public DeviceService() {
		super();
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	
}
