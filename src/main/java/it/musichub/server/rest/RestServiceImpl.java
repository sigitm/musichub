package it.musichub.server.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import fi.iki.elonen.NanoHTTPD;
import it.musichub.server.config.Constants;
import it.musichub.server.ex.ServiceDestroyException;
import it.musichub.server.ex.ServiceInitException;
import it.musichub.server.ex.ServiceStartException;
import it.musichub.server.ex.ServiceStopException;
import it.musichub.server.library.IndexerService;
import it.musichub.server.library.model.Folder;
import it.musichub.server.persistence.PersistenceService;
import it.musichub.server.persistence.ex.FileNotFoundException;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceFactory;
import it.musichub.server.upnp.model.DeviceRegistry;
import it.musichub.server.upnp.model.DeviceService;
import it.musichub.server.upnp.model.IPlaylistState;
import it.musichub.server.upnp.model.UpnpFactory;
import it.musichub.server.upnp.renderer.IRendererCommand;
import it.musichub.server.upnp.renderer.IRendererState;

public class RestServiceImpl extends MusicHubServiceImpl implements RestService {

	/**
	 * TODO
	 * 
	 */
	private final static Logger logger = Logger.getLogger(RestServiceImpl.class);

	private IndexerService getIndexerService(){
		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
	}
	
	@Override
	public void init() throws ServiceInitException {
	}

	@Override
	public void start() throws ServiceStartException {
	}

	@Override
	public void stop() throws ServiceStopException {
	}

	@Override
	public void destroy() throws ServiceDestroyException {
	}

}
