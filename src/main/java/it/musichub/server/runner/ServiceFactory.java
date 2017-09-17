package it.musichub.server.runner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import it.musichub.server.config.ConfigUtils;
import it.musichub.server.config.ConfigUtils.ValidateResult;
import it.musichub.server.config.Configuration;
import it.musichub.server.config.Constants;
import it.musichub.server.persistence.PersistenceEngine;
import it.musichub.server.persistence.PersistenceService;
import it.musichub.server.persistence.ex.FileNotFoundException;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.runner.ServiceRegistry.ServiceDefinition;
import it.musichub.server.upnp.DiscoveryService;

public class ServiceFactory {

	/*
	 * EVOLUZIONI:
	 * - NEW sistemare la questione init/start/newStart/ecc.... e i vari hook.. e lo stato del runner!
	 * v. saveToDisk
	 * v. init
	 */	
	public ServiceFactory() {
		super();
	}

	private boolean init = false;//TODO sistemare; decidere se mantenere uno status
	
	private final static Logger logger = Logger.getLogger(ServiceFactory.class);
	
	
	private static ServiceFactory instance = null;
	
	private Configuration config;
	private PersistenceService configPers = new PersistenceEngine();
	
	private Map<String,Object> params = new HashMap<>();
	
	public void addParam(String key, Object value){
		params.put(key, value);
	}
	
	public synchronized static ServiceFactory getInstance() {
		if (instance == null)
			instance = new ServiceFactory(); 
			
		return instance;
	}
	
	public static IMusicHubService getServiceInstance(Service service){
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
			IMusicHubService svcInstance = null;
			try {
				List<Class<?>> argClasses = new ArrayList<>();
				List<Object> argValues = new ArrayList<>();
				for (String name : svcDefinition.getArgs().keySet()){
					Class<?> clazz = svcDefinition.getArgs().get(name);
					argClasses.add(clazz);
					argValues.add(params.get(name));
				}
				Constructor constructor = svcClass.getDeclaredConstructor(argClasses.toArray(new Class<?>[]{}));
				svcInstance = (IMusicHubService) constructor.newInstance(argValues.toArray(new Object[]{}));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				logger.error("Error generating service "+service.name(), e);
				return;
			}
			svcDefinition.setInstance(svcInstance);
			
			logger.debug("Service "+service.name()+" generated");
		}
	}
	
	public Configuration getConfiguration(){
		return config;
	}
	
	public void setConfiguration(Configuration config){
		this.config = config;
	}
	
	public synchronized void init(){
		//using persistence service in unmanaged mode
		configPers.init();
		configPers.start();
		
		if (config == null){
			try {
				config = configPers.loadFromDisk(Configuration.class, Constants.CONFIG_FILE_NAME);
			} catch(FileNotFoundException e) {
				logger.warn("Config file not found. May be first launch.", e);
			} catch(LoadException e) {
				logger.error("Error loading config file", e);
			}
			if (config == null)
				config = new Configuration();
		}
	}
	
	private List<ShutdownHook> hooks = new ArrayList<ShutdownHook>();
	
	private synchronized void enableHook(ShutdownHook hook){
		hook.enable();
		hooks.add(hook);
	}
	
	private synchronized void disableHooks(){
		for (ShutdownHook hook : hooks)
			hook.disable();
		hooks.clear();
	}
	
	public synchronized void start(){
		//check configuration
		ValidateResult configValidation = ConfigUtils.validate(config);
		if (!configValidation.isOk()){
			logger.error("Error validating configuration.");
			for (String msg : configValidation.getErrors())
				logger.error(msg);
			return;
		}
		
		//starting services
		initServices();
		startServices();
		logger.info("MusicHub Server 0.1 started.");
		
		/*
		 * shutdown hooks:
		 * - "stop"/"exit" from console
		 * - jvm interruption (es. ctrl-c)
		 * - autosleep timer
		 */
		
		enableHook(new TimerHook());
		enableHook(new SystemHook());
		
		//console
		Scanner sc = new Scanner(System.in);
		while (true) {
			String command = sc.nextLine();
			if ("pause".equalsIgnoreCase(command)) {
				getDiscoveryService().getRendererCommand().commandPause();
			}else if ("play".equalsIgnoreCase(command)) {
				getDiscoveryService().getRendererCommand().commandPlay();
			}else if ("mute".equalsIgnoreCase(command)) {
				getDiscoveryService().getRendererCommand().toggleMute();
			}else if ("stop".equalsIgnoreCase(command)) {
				getDiscoveryService().getRendererCommand().commandStop();
			}else if ("exit".equalsIgnoreCase(command)) {
				logger.info("Terminating program by exit request... ");
				sc.close();
				shutdown();
				break;
			}
		}
	}
	
	private DiscoveryService getDiscoveryService(){
		return (DiscoveryService) ServiceFactory.getServiceInstance(Service.upnpdiscovery);
	}
	
	private static interface ShutdownHook{
		public abstract void enable();
		public abstract void disable();
	}
	
	private class TimerHook implements ShutdownHook{

		private Timer timer;
		
		@Override
		public void enable() {
			final Integer time = Constants.AUTOSLEEP_TIME;
			if (time != null){
				timer = new Timer();
		        timer.schedule (new TimerTask() {

		            @Override
		            public void run() {
		            	logger.fatal("sono passati "+time+" sec");
		            	shutdown();
		            }
		        }, TimeUnit.SECONDS.toMillis(time));	
			}
		}

		@Override
		public void disable() {
			if (timer != null)
				timer.cancel();
		}
	}

	private class SystemHook implements ShutdownHook{
		
		private Thread thread = new Thread() {
			public void run() {
				logger.info("Terminating program by shutdown hook... ");
				shutdown();
			}
		};
		
		@Override
		public void enable() {
			Runtime.getRuntime().addShutdownHook(thread);
		}

		@Override
		public void disable() {
			Runtime.getRuntime().removeShutdownHook(thread);
		}
	}
	
	public synchronized void shutdown(){
		//disable hooks
		disableHooks();
		
		stopServices();
		destroyServices();
		
		try {
			configPers.saveToDisk(config, Constants.CONFIG_FILE_NAME);
		} catch (SaveException e) {
			logger.error("Error saving config file", e);
		    return;
		}
		configPers.stop();
		configPers.destroy();
		
		System.exit(0);
	}
	
	private void initServices(){
		generateServices();
		
		logger.debug("Initializing services...");
		for (Service service : getServiceList(false)){
			logger.debug("Initializing service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			IMusicHubService svc = serviceDefinition.getInstance();
			svc.init();
			logger.debug("Service "+service.name()+" initialized");
		}

		logger.debug("Services initialized");
	}

	private void startServices(){
		logger.debug("Starting services...");
		for (Service service : getServiceList(false)){
			logger.debug("Starting service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			IMusicHubService svc = serviceDefinition.getInstance();
			svc.start();
			logger.debug("Service "+service.name()+" started");
		}

		logger.debug("Services started");
	}
	
	private void stopServices(){
		logger.debug("Stopping services...");
		for (Service service : getServiceList(true)){
			logger.debug("Stopping service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			IMusicHubService svc = serviceDefinition.getInstance();
			svc.stop();
			logger.debug("Service "+service.name()+" stopped");
		}

		logger.debug("Services stopped");
	}
	
	private void destroyServices(){
		logger.debug("Destroying services...");
		for (Service service : getServiceList(true)){
			logger.debug("Destroying service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			IMusicHubService svc = serviceDefinition.getInstance();
			svc.destroy();
			logger.debug("Service "+service.name()+" destroyed");
		}

		logger.debug("Services destroyed");
	}
}
