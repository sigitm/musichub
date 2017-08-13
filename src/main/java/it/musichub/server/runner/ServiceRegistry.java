package it.musichub.server.runner;

import java.util.LinkedHashMap;
import java.util.Map;

import it.musichub.server.library.SongsIndexer;
import it.musichub.server.library.SongsSearch;
import it.musichub.server.persistence.PersistenceEngine;
import it.musichub.server.upnp.DiscoveryServiceImpl;
import it.musichub.server.upnp.HttpServerServiceImpl;

public class ServiceRegistry {

	public static enum Service {persistence, indexer, search, upnpdiscovery, upnphttpserver, upnpcontroller, ws}
	
	protected static Map<Service,ServiceDefinition> serviceMap = new LinkedHashMap<Service,ServiceDefinition>(){{
		/**
		 * Dependencies:
		 * 
		 * persistence: none
		 * indexer: persistence
		 * search: indexer
		 * upnpdiscovery: persistence
		 * upnphttpserver: indexer XXXXXXXXXXXXXXXXX
		 * upnpcontroller: upnpdiscovery XXXXXXXXXXXXXXXX
		 */
		put(Service.persistence, new ServiceDefinition(PersistenceEngine.class));
		put(Service.indexer, new ServiceDefinition(SongsIndexer.class){{
			addArg("startingDir", String.class);
		}});
//		put(Service.search, new ServiceDefinition(SongsSearch.class));
//		put(Service.upnpdiscovery, new ServiceDefinition(DiscoveryServiceImpl.class));
		put(Service.upnphttpserver, new ServiceDefinition(HttpServerServiceImpl.class));
	}};
	
	protected static class ServiceDefinition {
		private Class<? extends MusicHubService> serviceClass;
		private MusicHubService instance;
		private Map<String,Class<?>> args = new LinkedHashMap<>();
		
		protected ServiceDefinition(Class<? extends MusicHubService> serviceClass) {
			super();
			this.serviceClass = serviceClass;
		}
		
		protected Class<? extends MusicHubService> getServiceClass() {
			return serviceClass;
		}
//		public void setServiceClass(Class<? extends MusicHubService> serviceClass) {
//			this.serviceClass = serviceClass;
//		}
		protected MusicHubService getInstance() {
			return instance;
		}
		protected void setInstance(MusicHubService instance) {
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
