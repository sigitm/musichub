package it.musichub.server.runner;

import java.util.LinkedHashMap;
import java.util.Map;

import it.musichub.server.library.IndexerServiceImpl;
import it.musichub.server.persistence.PersistenceServiceImpl;
import it.musichub.server.rest.RestServiceImpl;
import it.musichub.server.search.SearchServiceImpl;
import it.musichub.server.upnp.UpnpControllerServiceImpl;

public class ServiceRegistry {

	public static enum Service {persistence, indexer, search, upnpcontroller, rest}
	
	protected static Map<Service,ServiceDefinition> serviceMap = new LinkedHashMap<Service,ServiceDefinition>(){{
		/**
		 * Dependencies:
		 * 
		 * persistence: none
		 * indexer: persistence
		 * search: indexer
		 * upnpcontroller: persistence, indexer
		 * rest: ????????????????????????????????????
		 */
		put(Service.persistence, new ServiceDefinition(PersistenceServiceImpl.class));
		put(Service.indexer, new ServiceDefinition(IndexerServiceImpl.class){{
//			addArg("startingDir", String.class);
		}});
		put(Service.search, new ServiceDefinition(SearchServiceImpl.class));
		put(Service.upnpcontroller, new ServiceDefinition(UpnpControllerServiceImpl.class));
		put(Service.rest, new ServiceDefinition(RestServiceImpl.class));
	}};
	
	protected static class ServiceDefinition {
		private Class<? extends IMusicHubService> serviceClass;
		private IMusicHubService instance;
		private Map<String,Class<?>> args = new LinkedHashMap<>();
		
		protected ServiceDefinition(Class<? extends IMusicHubService> serviceClass) {
			super();
			this.serviceClass = serviceClass;
		}
		
		protected Class<? extends IMusicHubService> getServiceClass() {
			return serviceClass;
		}
//		public void setServiceClass(Class<? extends MusicHubService> serviceClass) {
//			this.serviceClass = serviceClass;
//		}
		protected IMusicHubService getInstance() {
			return instance;
		}
		protected void setInstance(IMusicHubService instance) {
			this.instance = instance;
		}
		protected void addArg(String name, Class<?> clazz) {
			args.put(name, clazz);
		}
		protected Map<String, Class<?>> getArgs() {
			return args;
		}
	}
	
}
