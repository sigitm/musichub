package it.musichub.server.rest.model;

public class HealthDto {
	
	private String version;
	private String status;
	
	public HealthDto(String version, String status) {
		super();
		this.version = version;
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
