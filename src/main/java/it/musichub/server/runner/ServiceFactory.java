package it.musichub.server.runner;

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
import it.musichub.server.ex.MusicHubException;
import it.musichub.server.ex.ServiceDestroyException;
import it.musichub.server.ex.ServiceInitException;
import it.musichub.server.ex.ServiceStartException;
import it.musichub.server.ex.ServiceStopException;
import it.musichub.server.persistence.PersistenceService;
import it.musichub.server.persistence.PersistenceServiceImpl;
import it.musichub.server.persistence.ex.FileNotFoundException;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.IMusicHubService.ServiceState;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.runner.ServiceRegistry.ServiceDefinition;
import it.musichub.server.upnp.UpnpControllerService;
import it.musichub.server.upnp.model.Device;
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

	public enum ServiceFactoryState {initing, inited, starting, started, stopping, stopped, destroying, destroyed}
	
	private ServiceFactoryState state = null;

	private final static Logger logger = Logger.getLogger(ServiceFactory.class);
	
	
	private static ServiceFactory instance = null;
	
	private Configuration config;
	private PersistenceService configPers = new PersistenceServiceImpl();
	
	private Map<String,Object> params = new HashMap<>();
	
	public void addParam(String key, Object value){
		params.put(key, value);
	}
	
	public synchronized static ServiceFactory getInstance() {
		if (instance == null)
			instance = new ServiceFactory(); 
			
		return instance;
	}
	
	public String getVersion(){
		return Constants.VERSION;	
	}
	
	public ServiceFactoryState getState() {
		return state;
	}

	private void setState(ServiceFactoryState state) {
		this.state = state;
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
				svcInstance = (IMusicHubService) svcClass.newInstance();
//				List<Class<?>> argClasses = new ArrayList<>();
//				List<Object> argValues = new ArrayList<>();
//				for (String name : svcDefinition.getArgs().keySet()){
//					Class<?> clazz = svcDefinition.getArgs().get(name);
//					argClasses.add(clazz);
//					argValues.add(params.get(name));
//				}
//				Constructor constructor = svcClass.getDeclaredConstructor(argClasses.toArray(new Class<?>[]{}));
//				svcInstance = (IMusicHubService) constructor.newInstance(argValues.toArray(new Object[]{}));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException /*| InvocationTargetException | NoSuchMethodException*/ | SecurityException e) {
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
	
	public void init(){
		if (config == null){
			try {
				//using persistence service in unmanaged mode
				configPers.init();
				configPers.start();
				
				config = configPers.loadFromDisk(Configuration.class, Constants.CONFIG_FILE_NAME);
			} catch(FileNotFoundException e) {
				logger.warn("Config file not found. May be first launch.", e);
			} catch(LoadException e) {
				logger.error("Error loading config file from disk", e);
			} catch (MusicHubException e) {
				logger.warn("Error starting persistence service in unmanaged mode", e);
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
	
	public void start(){
		//check configuration
		ValidateResult configValidation = ConfigUtils.validate(config);
		if (!configValidation.isOk()){
			logger.error("Error validating configuration.");
			for (String msg : configValidation.getErrors())
				logger.error(msg);
			return;
		}
		
		//starting services
		try {
			initServices();
			startServices();
		} catch (MusicHubException e) {
			logger.fatal("MusicHub Server could not start.", e);
			System.exit(1);
		}
		logger.info("MusicHub Server "+getVersion()+" started.");
		
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
			String p3 = lineSplit.length>3 ? lineSplit[3] : null;
			String p4 = lineSplit.length>4 ? lineSplit[4] : null;
			
			/*
			 * potrei cambiare da console il logging level!!
			 * 
			 * 
			 * list devices
			 * list devices online
			 * get selected device
			 */
			logger.info("Parsing shell command ["+line+"]");
			boolean parsingOk = false;
			try{
				if ("pause".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().pause();
					parsingOk = true;
				}else if ("uti".equalsIgnoreCase(command)) { //XXXXXX PROVVISORIO
					((RendererCommand)getUpnpControllerService().getRendererCommand()).updateTransportInfo(true);
					parsingOk = true;
				}else if ("upi".equalsIgnoreCase(command)) { //XXXXXX PROVVISORIO
					((RendererCommand)getUpnpControllerService().getRendererCommand()).updatePositionInfo(true);
					parsingOk = true;
				}else if ("uv".equalsIgnoreCase(command)) { //XXXXXX PROVVISORIO
					((RendererCommand)getUpnpControllerService().getRendererCommand()).updateVolume(true);
					parsingOk = true;
				}else if ("um".equalsIgnoreCase(command)) { //XXXXXX PROVVISORIO
					((RendererCommand)getUpnpControllerService().getRendererCommand()).updateMute(true);
					parsingOk = true;
				}else if ("cplay".equalsIgnoreCase(command)) { //XXXXXX PROVVISORIO
					getUpnpControllerService().getRendererCommand().commandPlay(true);
					parsingOk = true;
				}else if ("seek".equalsIgnoreCase(command)) { //XXXXXX PROVVISORIO
					if (p1 != null){
						getUpnpControllerService().getRendererCommand().commandSeek(p1, true);
						parsingOk = true;
					}
				}else if ("play".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().play();
					parsingOk = true;
				}else if ("stop".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().stop();
					parsingOk = true;
				}else if ("first".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().first();
					parsingOk = true;
				}else if ("previous".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().previous();
					parsingOk = true;
				}else if ("next".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().next();
					parsingOk = true;
				}else if ("last".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().last();
					parsingOk = true;
				}else if ("list".equalsIgnoreCase(command)) {
					if ("playlist".equalsIgnoreCase(p1)){
						logger.info("\n"+getUpnpControllerService().getRendererState().getPlaylist().prettyPrint());
						parsingOk = true;
					}else if ("devices".equalsIgnoreCase(p1)){
						logger.info("\n"+getUpnpControllerService().getDeviceRegistry().prettyPrint());
						parsingOk = true;
					} 
				}else if ("set".equalsIgnoreCase(command)) {
					if ("device".equalsIgnoreCase(p1)){
						//parsing device udn / customName
						String udn = null;
						if (getUpnpControllerService().getDeviceRegistry().containsKey(p2))
							udn = p2;
						else{
							Device device = getUpnpControllerService().getDeviceByCustomName(p2);
							udn = device.getUdn();
						}
						
						if (udn != null){
							if ("selected".equalsIgnoreCase(p3)){
								getUpnpControllerService().setSelectedDevice(udn);
								parsingOk = true;
							}else if ("name".equalsIgnoreCase(p3)){
								getUpnpControllerService().setDeviceCustomName(udn, p4);
								parsingOk = true;
							}
						}else if ("null".equals(p2) && "selected".equalsIgnoreCase(p3)){
							getUpnpControllerService().clearSelectedDevice();
							parsingOk = true;
						}
					}else if ("devices".equalsIgnoreCase(p1)){
						logger.info("\n"+getUpnpControllerService().getDeviceRegistry().prettyPrint());
						parsingOk = true;
					} 
				}else if ("playlist".equalsIgnoreCase(command) || "pl".equalsIgnoreCase(command)) {
					logger.info("\n"+getUpnpControllerService().getRendererState().getPlaylist().prettyPrint());
					parsingOk = true;
				}else if ("shuffle".equalsIgnoreCase(command)) {
					if (p1 == null){
						logger.info("shuffle = "+getUpnpControllerService().getRendererState().getPlaylist().getShuffle());
						parsingOk = true;
					}else if ("on".equalsIgnoreCase(p1)){
						getUpnpControllerService().getRendererState().getPlaylist().setShuffle(true);
						parsingOk = true;
					}else if ("off".equalsIgnoreCase(p1)){
						getUpnpControllerService().getRendererState().getPlaylist().setShuffle(false);
						parsingOk = true;
					}
				}else if ("repeat".equalsIgnoreCase(command)) {
					if (p1 == null){
						logger.info("repeat = "+getUpnpControllerService().getRendererState().getPlaylist().getRepeat());
						parsingOk = true;
					}else if ("all".equalsIgnoreCase(p1)){
						getUpnpControllerService().getRendererState().getPlaylist().setRepeat(RepeatMode.ALL);
						parsingOk = true;
					}else if ("off".equalsIgnoreCase(p1)){
						getUpnpControllerService().getRendererState().getPlaylist().setRepeat(RepeatMode.OFF);
						parsingOk = true;
					}else if ("track".equalsIgnoreCase(p1)){
						getUpnpControllerService().getRendererState().getPlaylist().setRepeat(RepeatMode.TRACK);
						parsingOk = true;
					}
				}else if ("mute".equalsIgnoreCase(command)) {
					getUpnpControllerService().getRendererCommand().toggleMute(false);
					parsingOk = true;
				}else if ("vol".equalsIgnoreCase(command)) {
					int vol = getUpnpControllerService().getRendererState().getVolume();
					if (p1 == null){
						logger.info("Volume = "+getUpnpControllerService().getRendererState().getVolume());
						parsingOk = true;
					}else{
						if ("+".equals(p1) || "-".equals(p1)){
							int bias = 5;
							try {
								bias = Integer.parseInt(p2);
							} catch (Exception e) {
								//nothing to do
							}
							if ("+".equals(p1)){
								getUpnpControllerService().getRendererCommand().setVolume(vol+bias, false);
								parsingOk = true;
							}else if ("-".equals(p1)){
								getUpnpControllerService().getRendererCommand().setVolume(vol-bias, false);
								parsingOk = true;
							}
						}else{
							Integer newvol = null;
							try {
								newvol = Integer.parseInt(p1);
							} catch (Exception e) {
								//nothing to do
							}
							if (newvol != null){
								getUpnpControllerService().getRendererCommand().setVolume(newvol, false);
								parsingOk = true;
							}
						}
					}
				}else if ("logger".equalsIgnoreCase(command)) {
					parsingOk = true;
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
					else
						parsingOk = false;
				}else if ("exit".equalsIgnoreCase(command)) {
					logger.info("Terminating program by exit request... ");
					sc.close();
					shutdown();
					break;
				}
				if (parsingOk)
					logger.info("Parsing of command ["+line+"] completed");
				else
					logger.warn("Unknown shell command ["+line+"]");
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
			final Integer time = getConfiguration().getAutoSleepTime();
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
	
	public void shutdown(){
		//disable hooks
		disableHooks();
		
		//stopping services
		try {
			stopServices();
			destroyServices();
		} catch (MusicHubException e) {
			logger.error("Error shutting down services", e);
//		    return;
		}
		
		try {
			configPers.saveToDisk(config, Constants.CONFIG_FILE_NAME);
			configPers.stop();
			configPers.destroy();
		} catch (SaveException e) {
			logger.error("Error saving config file to disk", e);
//		    return;
		} catch (MusicHubException e) {
			logger.error("Error shutting down persistence service in unmanaged mode", e);
//		    return;
		}
		
		logger.info("MusicHub Server terminated.");
		
		System.exit(0);
	}
	
	private void initServices() throws ServiceInitException {
		logger.debug("Initializing services...");
		setState(ServiceFactoryState.initing);
		generateServices();
	
		for (Service service : getServiceList(false)){
			logger.debug("Initializing service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			IMusicHubService svc = serviceDefinition.getInstance();
			if (svc.getState() != null)
				throw new ServiceInitException("Invalid service state "+svc.getState());
			svc.init();
			svc.setState(ServiceState.init);
			logger.debug("Service "+service.name()+" initialized");
		}

		setState(ServiceFactoryState.inited);
		logger.debug("Services initialized");
	}

	private void startServices() throws ServiceStartException {
		logger.debug("Starting services...");
		setState(ServiceFactoryState.starting);
		
		for (Service service : getServiceList(false)){
			logger.debug("Starting service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			IMusicHubService svc = serviceDefinition.getInstance();
			if (svc.getState() != ServiceState.init)
				throw new ServiceStartException("Invalid service state "+svc.getState());
			svc.start();
			svc.setState(ServiceState.start);
			logger.debug("Service "+service.name()+" started");
		}

		setState(ServiceFactoryState.started);
		logger.debug("Services started");
	}
	
	private void stopServices() throws ServiceStopException {
		logger.debug("Stopping services...");
		setState(ServiceFactoryState.stopping);
		
		for (Service service : getServiceList(true)){
			logger.debug("Stopping service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			IMusicHubService svc = serviceDefinition.getInstance();
			if (svc.getState() != ServiceState.start)
				throw new ServiceStopException("Invalid service state "+svc.getState());
			svc.stop();
			svc.setState(ServiceState.stop);
			logger.debug("Service "+service.name()+" stopped");
		}

		setState(ServiceFactoryState.stopped);
		logger.debug("Services stopped");
	}
	
	private void destroyServices() throws ServiceDestroyException {
		logger.debug("Destroying services...");
		setState(ServiceFactoryState.destroying);
		
		for (Service service : getServiceList(true)){
			logger.debug("Destroying service "+service.name()+"...");
			ServiceDefinition serviceDefinition = getServiceMap().get(service);
			IMusicHubService svc = serviceDefinition.getInstance();
			if (svc.getState() != ServiceState.stop)
				throw new ServiceDestroyException("Invalid service state "+svc.getState());
			svc.destroy();
			svc.setState(ServiceState.destroy);
			logger.debug("Service "+service.name()+" destroyed");
		}

		setState(ServiceFactoryState.destroyed);
		logger.debug("Services destroyed");
	}
}
