package it.musichub.server.rest.model;

import java.io.Serializable;
import java.net.URI;

public class DeviceIconDto implements Serializable {

	private String filename;
	private String mimeType;
	private int width;
	private int height;
	private int depth;
	private String data;
	
	public DeviceIconDto(){
		super();
	}
	
	public DeviceIconDto(String filename, String mimeType, int width, int height, int depth, String data) {
		this();
		this.filename = filename;
		this.mimeType = mimeType;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.data = data;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "DeviceIconDto [mimeType=" + mimeType + ", width=" + width + ", height=" + height + ", depth=" + depth
				+ "]";
	}
	
}
