package it.musichub.server.rest.impl;

import java.io.Serializable;

import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.upnp.UpnpControllerService;
import spark.Request;
import spark.Route;

public abstract class AbstractRoute implements Route, Serializable {

	protected UpnpControllerService getUpnpControllerService(){
		return (UpnpControllerService) ServiceFactory.getServiceInstance(Service.upnpcontroller);
	}
	
	protected static Integer[] getPaginationParams(Request request){
		String paramLimit = request.queryParams("limit");
		Integer limit = null;
		if (paramLimit != null){
			try {
				limit = Integer.valueOf(paramLimit);
			} catch (NumberFormatException e){
				//Nothing to do
			}
		}
		String paramStart = request.queryParams("start");
		Integer start = null;
		if (paramStart != null){
			try {
				start = Integer.valueOf(paramStart);
			} catch (NumberFormatException e){
				//Nothing to do
			}
		}
		return new Integer[]{limit, start};
	}
	
	/**
	 * Spark route init order
	 */
	public int getOrder(){
		return 0;
	}

}
