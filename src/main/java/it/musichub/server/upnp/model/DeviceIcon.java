package it.musichub.server.upnp.model;

import java.io.Serializable;
import java.net.URI;

public class DeviceIcon implements Serializable {

	private String mimeType;
	private int width;
	private int height;
	private int depth;
	private URI uri;
	private byte[] data;
	
	public DeviceIcon(String mimeType, int width, int height, int depth, URI uri, byte[] data) {
		super();
		this.mimeType = mimeType;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.uri = uri;
		this.data = data;
	}

	public String getMimeType() {
		return mimeType;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getDepth() {
		return depth;
	}

	public URI getUri() {
		return uri;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return "DeviceIcon [mimeType=" + mimeType + ", width=" + width + ", height=" + height + ", depth=" + depth
				+ "]";
	}
	
}
