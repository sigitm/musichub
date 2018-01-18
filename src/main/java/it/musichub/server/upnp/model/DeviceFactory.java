package it.musichub.server.upnp.model;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.UDN;

import it.musichub.server.library.IndexerServiceImpl;

public class DeviceFactory {
	
	private static Map<URL, byte[]> iconsCache = new HashMap<>();
	
	private final static Logger logger = Logger.getLogger(DeviceFactory.class);
	
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
				byte[] data = clingIcon.getData();
				String filename = null;
				if (clingIcon.getUri() != null && data == null){
					//retrieving icon
					URL iconURL = clingDevice.normalizeURI(clingIcon.getUri());
					filename = iconURL.getPath() != null ? iconURL.getPath().substring(iconURL.getPath().lastIndexOf('/')+1) : null;
					if (iconsCache.containsKey(iconURL))
						data = iconsCache.get(iconURL);
					else{
						data = downloadBinaryFile(iconURL);
						iconsCache.put(iconURL, data);
					}
				}
				DeviceIcon icon = new DeviceIcon(filename, clingIcon.getMimeType().toString(), clingIcon.getWidth(),
						clingIcon.getHeight(), clingIcon.getDepth(), clingIcon.getUri(), data);
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
	
	private static byte[] downloadBinaryFile(URL u){
		byte[] data = null;
		
		try{
			URLConnection uc = u.openConnection();
		    String contentType = uc.getContentType();
		    int contentLength = uc.getContentLength();
		    if (contentType.startsWith("text/") || contentLength == -1) {
		      throw new IOException("This is not a binary file.");
		    }
		    InputStream raw = uc.getInputStream();
		    InputStream in = new BufferedInputStream(raw);
		    
		    data = new byte[contentLength];
		    int bytesRead = 0;
		    int offset = 0;
		    while (offset < contentLength) {
		      bytesRead = in.read(data, offset, data.length - offset);
		      if (bytesRead == -1)
		        break;
		      offset += bytesRead;
		    }
		    in.close();
	
		    if (offset != contentLength) {
		      throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
		    }
		} catch (Exception e){
			logger.warn("Exception downloading binary file at url "+u, e);
			return null;
		}
	    
	    return data;
	}
	
}
