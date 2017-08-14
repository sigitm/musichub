package it.musichub.server.runner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

import it.musichub.server.config.Configuration;
import it.musichub.server.config.Constants;
import it.musichub.server.persistence.PersistenceEngine;
import it.musichub.server.persistence.PersistenceService;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.runner.ServiceRegistry.ServiceDefinition;

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
	
	public static void main(String[] args) {
		//TODO PROVVISORIO mettere in un test
		String startingDirStr = "D:\\users\\msigismondi.INT\\Desktop";
		String startingDirStr2 = "D:\\users\\msigismondi.INT\\Desktop";
		try {
			if ("SIGIQC".equals(InetAddress.getLocalHost().getHostName())){
				startingDirStr = "N:\\incoming\\##mp3 new";
				startingDirStr2 = "N:\\incoming\\##mp3 new\\Zucchero TODO\\Zucchero - Greatest Hits (1996)NLT-Release";
			}else if ("SARANB".equals(InetAddress.getLocalHost().getHostName())){
				startingDirStr = "C:\\Users\\Sara\\Desktop";
				startingDirStr2 = "C:\\Users\\Sara\\Desktop";
			} 
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ServiceFactory sf = ServiceFactory.getInstance();
		sf.init();
		sf.getConfiguration().setContentDir(startingDirStr);
		sf.start();
	}
	
	public Configuration getConfiguration(){
		return config;
	}
	
	public void init(){
		//using persistence service in unmanaged mode
		configPers.init();
		configPers.start();
		try {
			config = configPers.loadFromDisk(Configuration.class, Constants.CONFIG_FILE_NAME);
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (config == null)
			config = new Configuration();
	}
	
	public void start(){		
		initServices();
		startServices();
		final Timer timer = addTimer();
		addHook(timer);
		logger.info("MusicHub Server 0.1 started.");
		
		/**
		 * esperimenti shutdown:
		 * - "exit"
		 * - ctrl-c
		 * - 1 minuto (per prova)
		 */
		Scanner sc = new Scanner(System.in);

		while (true) {
			String command = sc.nextLine();
			if ("stop".equalsIgnoreCase(command) || "exit".equalsIgnoreCase(command)) {
				logger.info("Terminating program by stop request... ");
				sc.close();
				timer.cancel();
				newShutdown();
				break;
			}
		}
	}
	
	public void newShutdown(){
		stopServices();
		destroyServices();
		
		try {
			configPers.saveToDisk(config, Constants.CONFIG_FILE_NAME);
		} catch (SaveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		configPers.stop();
		configPers.destroy();
		
//		System.exit(0);
	}
	
	public Timer addTimer() {
		//esperimento timer
		final int time = 600;
		final Timer timer = new Timer();
        timer.schedule (new TimerTask() {

            @Override
            public void run() {
            	logger.fatal("sono passati "+time+" sec");
            	newShutdown();
            }
        }, TimeUnit.SECONDS.toMillis(time));
		
		return timer;
	}
	
	public void addHook(final Timer timer) {
        //shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Terminating program by shutdown hook... ");
				timer.cancel();
				newShutdown();
			}
		});
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
