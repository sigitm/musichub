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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
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
import it.musichub.server.upnp.UpnpControllerService;
import it.musichub.server.upnp.model.IPlaylistState.RepeatMode;
import it.musichub.server.upnp.renderer.RendererCommand;

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
		 * - "exit" from console
		 * - jvm interruption (es. ctrl-c)
		 * - autosleep timer
		 */
		
		enableHook(new TimerHook());
		enableHook(new SystemHook());
		
		//console
		Scanner sc = new Scanner(System.in);
		while (true) {
			String line = sc.nextLine();
			if (line.isEmpty())
				continue;
			
			String[] lineSplit = line.split(" ");
			String command = lineSplit.length>0 ? lineSplit[0] : null;
			String p1 = lineSplit.length>1 ? lineSplit[1] : null;
			String p2 = lineSplit.length>2 ? lineSplit[2] : null;
			
			/*
			 * potrei cambiare da console il logging level!!
			 * 
			 * 
			 * list devices
			 * list devices online
			 * get selected device
			 */
			logger.info("Parsing shell command ["+line+"]");
			try{
				if ("pause".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().pause();
				}else if ("uti".equalsIgnoreCase(command)) { //XXXXXX PROVVISORIO
					((RendererCommand)getUpnpControllerService().getRendererCommand()).updateTransportInfo(true);
				}else if ("cplay".equalsIgnoreCase(command)) { //XXXXXX PROVVISORIO
						getUpnpControllerService().getRendererCommand().commandPlay(true);
				}else if ("seek".equalsIgnoreCase(command)) { //XXXXXX PROVVISORIO
					if (p1 != null)
						getUpnpControllerService().getRendererCommand().commandSeek(p1, true);
				}else if ("play".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().play();
				}else if ("stop".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().stop();
				}else if ("first".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().first();
				}else if ("previous".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().previous();
				}else if ("next".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().next();
				}else if ("last".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().last();
				}else if ("playlist".equalsIgnoreCase(command) || "pl".equalsIgnoreCase(command)) {
					logger.info("\n"+getUpnpControllerService().getRendererState().getPlaylist().prettyPrint());
				}else if ("shuffle".equalsIgnoreCase(command)) {
					if (p1 == null)
						logger.info("shuffle = "+getUpnpControllerService().getRendererState().getPlaylist().getShuffle());
					else if ("on".equalsIgnoreCase(p1))
						getUpnpControllerService().getRendererState().getPlaylist().setShuffle(true);
					else if ("off".equalsIgnoreCase(p1))
						getUpnpControllerService().getRendererState().getPlaylist().setShuffle(false);
				}else if ("repeat".equalsIgnoreCase(command)) {
					if (p1 == null)
						logger.info("repeat = "+getUpnpControllerService().getRendererState().getPlaylist().getRepeat());
					else if ("all".equalsIgnoreCase(p1))
						getUpnpControllerService().getRendererState().getPlaylist().setRepeat(RepeatMode.ALL);
					else if ("off".equalsIgnoreCase(p1))
						getUpnpControllerService().getRendererState().getPlaylist().setRepeat(RepeatMode.OFF);
					else if ("track".equalsIgnoreCase(p1))
						getUpnpControllerService().getRendererState().getPlaylist().setRepeat(RepeatMode.TRACK);
				}else if ("mute".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().toggleMute(false);
				}else if ("vol".equalsIgnoreCase(command)) {
					int vol = getUpnpControllerService().getRendererState().getVolume();
					if (p1 == null)
						logger.info("Volume = "+getUpnpControllerService().getRendererState().getVolume());
					else{
						int bias = 5;
						try {
							bias = Integer.parseInt(p2);
						} catch (Exception e) {
							//nothing to do
						}
						if ("+".equalsIgnoreCase(p1))
							getUpnpControllerService().getRendererCommand().setVolume(vol+bias, false);
						else if ("-".equalsIgnoreCase(p1))
							getUpnpControllerService().getRendererCommand().setVolume(vol-bias, false);
					}
				}else if ("logger".equalsIgnoreCase(command)) {
					if (p1 == null)
						logger.info("logger level = "+LogManager.getRootLogger().getLevel());
					else if ("OFF".equalsIgnoreCase(p1))
						LogManager.getRootLogger().setLevel(Level.OFF);
					else if ("FATAL".equalsIgnoreCase(p1))
						LogManager.getRootLogger().setLevel(Level.FATAL);
					else if ("ERROR".equalsIgnoreCase(p1))
						LogManager.getRootLogger().setLevel(Level.ERROR);
					else if ("WARN".equalsIgnoreCase(p1))
						LogManager.getRootLogger().setLevel(Level.WARN);
					else if ("INFO".equalsIgnoreCase(p1))
						LogManager.getRootLogger().setLevel(Level.INFO);
					else if ("DEBUG".equalsIgnoreCase(p1))
						LogManager.getRootLogger().setLevel(Level.DEBUG);
					else if ("TRACE".equalsIgnoreCase(p1))
						LogManager.getRootLogger().setLevel(Level.TRACE);
				}else if ("exit".equalsIgnoreCase(command)) {
					logger.info("Terminating program by exit request... ");
					sc.close();
					shutdown();
					break;
				}else{
					logger.warn("Unknown shell command ["+line+"]");
				}
				logger.info("Parsing of command ["+line+"] completed");
			}catch (Exception e){
				logger.warn("Exception thrown executing command ["+line+"]", e);
			}
		}
	}
	
	private UpnpControllerService getUpnpControllerService(){
		return (UpnpControllerService) ServiceFactory.getServiceInstance(Service.upnpcontroller);
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
