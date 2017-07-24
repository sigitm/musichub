package it.musichub.server.runner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import it.musichub.server.runner.ServiceRegistry.ServiceDefinition;

public class ServiceFactory implements MusicHubService {

	/*
	 * EVOLUZIONI:
	 * v. saveToDisk
	 * v. init
	 */	
	public ServiceFactory() {
		super();
	}

	private boolean init = false;//TODO sistemare; decidere se mantenere uno status
	
	private final static Logger logger = Logger.getLogger(ServiceFactory.class);
	
	
	private static ServiceFactory instance = null;
	
	private Map<String,Object> params = new HashMap<>();
	
	public static enum Service {persistence, indexer, search, upnp, ws}
	
	public void addParam(String key, Object value){
		params.put(key, value);
	}
	
	public synchronized static ServiceFactory getInstance() {
		if (instance == null)
			instance = new ServiceFactory(); 
			
		return instance;
	}
	
	public static MusicHubService getServiceInstance(Service service){
		return instance.getServiceMap().get(service).getInstance();
	}
	
	public Map<Service, ServiceDefinition> getServiceMap() {
		return ServiceRegistry.serviceMap;
	}
	
	private List<Service> getServiceList(boolean reverse) {
		List<Service> result = new ArrayList<Service>(getServiceMap().keySet());
		
		if (reverse)
			Collections.reverse(result);
		
		return result;
	}

	private void generateServices(){
		logger.debug("Generating services...");
		for (Service service : getServiceList(false)){
			logger.debug("Generating service "+service.name()+"...");
			ServiceDefinition svcDefinition = getServiceMap().get(service);
			Class<?> svcClass = svcDefinition.getServiceClass();
			MusicHubService svcInstance = null;
			try {
				List<Class<?>> argClasses = new ArrayList<>();
				List<Object> argValues = new ArrayList<>();
				for (String name : svcDefinition.getArgs().keySet()){
					Class<?> clazz = svcDefinition.getArgs().get(name);
					argClasses.add(clazz);
					argValues.add(params.get(name));
				}
				Constructor constructor = svcClass.getDeclaredConstructor(argClasses.toArray(new Class<?>[]{}));
				svcInstance = (MusicHubService) constructor.newInstance(argValues.toArray(new Object[]{}));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				logger.error("Error generating service "+service.name(), e);
				return;
			}
			svcDefinition.setInstance(svcInstance);
			
			logger.debug("Service "+service.name()+" generated");
		}
	}
	
	@Override
	public void init(){
		generateServices();
		
		logger.debug("Initializing services...");
		for (Service service : getServiceList(false)){
			logger.debug("Initializing service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			MusicHubService svc = serviceDefinition.getInstance();
			svc.init();
			logger.debug("Service "+service.name()+" initialized");
		}

		logger.debug("Services initialized");
	}

	@Override
	public void start(){
		logger.debug("Starting services...");
		for (Service service : getServiceList(false)){
			logger.debug("Starting service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			MusicHubService svc = serviceDefinition.getInstance();
			svc.init();
			logger.debug("Service "+service.name()+" started");
		}

		logger.debug("Services started");
	}
	
	@Override
	public void stop(){
		logger.debug("Stopping services...");
		for (Service service : getServiceList(true)){
			logger.debug("Stopping service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			MusicHubService svc = serviceDefinition.getInstance();
			svc.init();
			logger.debug("Service "+service.name()+" stopped");
		}

		logger.debug("Services stopped");
	}
	
	@Override
	public void destroy(){
		logger.debug("Destroying services...");
		for (Service service : getServiceList(true)){
			logger.debug("Destroying service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			MusicHubService svc = serviceDefinition.getInstance();
			svc.init();
			logger.debug("Service "+service.name()+" destroyed");
		}

		logger.debug("Services destroyed");
	}
}
