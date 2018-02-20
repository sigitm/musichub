package it.musichub.server.rest.impl;

import java.io.Serializable;

import it.musichub.server.library.model.Song;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.search.SearchService;
import it.musichub.server.search.model.Clause;
import it.musichub.server.search.model.QbeClause;
import it.musichub.server.upnp.UpnpControllerService;
import spark.Request;
import spark.Route;

public abstract class AbstractRoute implements Route, Serializable {

	protected SearchService getSearchService(){
		return (SearchService) ServiceFactory.getServiceInstance(Service.search);
	}
	
	protected UpnpControllerService getUpnpControllerService(){
		return (UpnpControllerService) ServiceFactory.getServiceInstance(Service.upnpcontroller);
	}
	
	protected static String getUrl(Request request){
		return request.url()+"?"+request.queryString();
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
	
	protected static Clause encodeSongParams(Request request){
		Song qbe = new Song();
		qbe.setTitle(request.queryParams("title"));
		qbe.setArtist(request.queryParams("artist"));
		qbe.setAlbumTitle(request.queryParams("albumTitle"));
		qbe.setYear(stringToInt(request.queryParams("year")));
		qbe.setGenre(request.queryParams("genre"));
		qbe.setRating(stringToInt(request.queryParams("rating")));
		
		return new QbeClause(qbe); 
	}
	
	private static Integer stringToInt(String s){
		Integer i = null;
		try {
			i = Integer.valueOf(s);
		} catch (NumberFormatException e){
			//Nothing to do
		}
		return i;
	}
	
	/**
	 * Spark route init order
	 */
	public int getOrder(){
		return 0;
	}

}
