package it.musichub.server.rest;

import org.apache.log4j.Logger;

import it.musichub.server.ex.ServiceDestroyException;
import it.musichub.server.ex.ServiceInitException;
import it.musichub.server.ex.ServiceStartException;
import it.musichub.server.ex.ServiceStopException;
import it.musichub.server.rest.impl.RestApp;
import it.musichub.server.runner.MusicHubServiceImpl;

public class RestServiceImpl extends MusicHubServiceImpl implements RestService {

	/**
	 * requirements di spark da verificare:
	 * - 
	 * - basic auth https://github.com/qmetric/spark-authentication OPPURE https://github.com/pac4j/spark-pac4j
	 * - pagination
	 * - swagger support https://serol.ro/posts/2016/swagger_sparkjava/
	 */
	/*
	 * TODO
	 * 
	 */
	private RestApp restApp = null;
	
	private final static Logger logger = Logger.getLogger(RestServiceImpl.class);
	

//	private IndexerService getIndexerService(){
//		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
//	}
	
	@Override
	public void init() throws ServiceInitException {
		restApp = new RestApp(getConfiguration().getRestHttpPort());
		try {
			restApp.init();
		} catch (RuntimeException e){
			throw new ServiceInitException("Error initializing REST service", e);
		}
	}

	@Override
	public void start() throws ServiceStartException {
		try {
			restApp.start();
		} catch (RuntimeException e){
			throw new ServiceStartException("Error starting REST service", e);
		}
	}
	
	@Override
	public void stop() throws ServiceStopException {
		try {
			restApp.stop();
		} catch (RuntimeException e){
			throw new ServiceStopException("Error stopping REST service", e);
		}
	}

	@Override
	public void destroy() throws ServiceDestroyException {
		restApp = null;
	}

}
