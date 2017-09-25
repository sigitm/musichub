package it.musichub.server.upnp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import fi.iki.elonen.NanoHTTPD;
import it.musichub.server.config.Constants;
import it.musichub.server.library.IndexerService;
import it.musichub.server.library.model.Folder;
import it.musichub.server.persistence.PersistenceService;
import it.musichub.server.persistence.ex.FileNotFoundException;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceFactory;
import it.musichub.server.upnp.model.DeviceRegistry;
import it.musichub.server.upnp.model.DeviceService;
import it.musichub.server.upnp.model.IPlaylistState;
import it.musichub.server.upnp.model.UpnpFactory;
import it.musichub.server.upnp.model.x.IRendererCommand;
import it.musichub.server.upnp.model.x.IRendererState;
import it.musichub.server.upnp.model.x.TrackMetadata;

public class UpnpControllerServiceImpl extends MusicHubServiceImpl implements UpnpControllerService {

	/**
	 * Tengo i device in un mio deviceregistry e li aggiorno con il listener
	 * 
	 * deviceregistry mappa con l'uid come chiave? lo salvo tramite persistence?
	 * 
	 * li tengo tutti, anche i vecchi? 	 ci sarà anche un attributo "online" e "lastSeenOnline"
	 * 
	 * ...
	 * 
	 */
	private DeviceRegistry deviceRegistry = null;
	private UpnpService upnpService = null;
	private IRendererState rendererState = null;
	private IRendererCommand rendererCommand = null;
	private MediaServer mediaServer = null;
	
	private final static Logger logger = Logger.getLogger(UpnpControllerServiceImpl.class);

	@Override
	public UpnpService getUpnpService() {
		return upnpService;
	}
	
	@Override
	public IRendererState getRendererState(){ //TODO provvisorio
		return rendererState;
	}
	
	@Override
	public IRendererCommand getRendererCommand(){ //TODO provvisorio
		return rendererCommand;
	}
	
	public MediaServer getMediaServer(){ //TODO provvisorio???
		return mediaServer;
	}
	
	private static boolean isValidDevice(RemoteDevice device){
		return new UDADeviceType(Constants.UPNP_DEVICE_TYPE).equals(device.getType());
	}
	
	private PersistenceService getPersistenceService(){
		return (PersistenceService) ServiceFactory.getServiceInstance(Service.persistence);
	}
	
	private IndexerService getIndexerService(){
		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
	}
	
	private void loadFromDisk(){
		logger.info("Loading registry from file...");
		
		try {
			deviceRegistry = getPersistenceService().loadFromDisk(DeviceRegistry.class, Constants.REGISTRY_FILE_NAME);
		} catch(FileNotFoundException e) {
			logger.warn("Registry file not found. May be first launch.", e);
		    return;
		} catch(LoadException e) {
			logger.error("Error loading registry file", e);
		    return;
		}
	}
	
	private void saveToDisk(){
		logger.info("Saving registry to file...");
		
		try {
			getPersistenceService().saveToDisk(deviceRegistry, Constants.REGISTRY_FILE_NAME);
		} catch (SaveException e) {
			logger.error("Error saving registry", e);
			return;
		}
		
		logger.info("... registry saved.");
	}
	
	@Override
	public void init() {
		//init registry
		loadFromDisk();
		if (deviceRegistry == null)
			deviceRegistry = new DeviceRegistry();
		deviceRegistry.resetOnlines();
		
		//creating UPnP discovery with a callback
		RegistryListener listener = new MediaRenderersListener();
		upnpService = new UpnpServiceImpl(listener);
		
		//creating renderer state and command
		rendererState = UpnpFactory.createRendererState();
		rendererCommand = UpnpFactory.createRendererCommand(upnpService.getControlPoint(), rendererState);
		
		//creating http server
		mediaServer = new MediaServer(getConfiguration().getMediaHttpPort());
	}

	@Override
	public void start() {
		// Send a search message to all devices and services, they should
		// respond soon
//		for (UDADeviceType udaType : getDeviceTypes())
			upnpService.getControlPoint().search(new UDADeviceTypeHeader(/*udaType*/new UDADeviceType(Constants.UPNP_DEVICE_TYPE)));
			
		//init http server
        try {
        	mediaServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException ioe) {
            logger.error("Couldn't start server",ioe);
//            System.exit(-1);
            return;
        }

        

        
        //EXPERIMENT ---------------------------------------------
        try {
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {}
        
        
        Collection<Device> devices = deviceRegistry.values();
        for (Device device : devices){
        	if (isDeviceOnline(device)){
        		setSelectedDevice(device);
        		logger.fatal("Selected device "+device.getFriendlyName());
        	}
        }
        if (!isDeviceSelected()){
	        try {
	        	logger.fatal("Manually selecting device...");
				setSelectedDevice("uuid:5f9ec1b3-ed59-1900-4530-00a0deb52729");
			} catch (DeviceNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }

//        rendererCommand.resume();
        try {
			TimeUnit.SECONDS.sleep(4);
		} catch (Exception e) {}
        
        IndexerService is = getIndexerService();
        Folder root = is.getStartingFolder();
//      Song song0 = root.getSongs().get(0);
        
        IPlaylistState playlist = rendererState.getPlaylist();
        playlist.addFolder(root, false);
        
        rendererCommand.launchPlaylist();
        //e una nextSong?? devo implementare qui tutti i controlli del player!!!!
        
        
        
        
////        MusicTrack mt = UpnpFactory.songToMusicTrack(mediaServer, song0);
//        TrackMetadata trackMetadata = UpnpFactory.songToTrackMetadata(mediaServer, song0);
//        logger.fatal("Track metadata xml: "+trackMetadata.getXML());
//        
////        Res res = new Res(new ProtocolInfo("http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000"), 5835548L, "0:04:01.000", 24000L, "http://192.168.1.30:9790/minimserver/*/music/MP3s/Pop/Bryan*20Adams*20-*20Summer*20of*20*2769.mp3");
////        TrackMetadata trackMetadataFAKE = new TrackMetadata("0$=Artist$1314$items$*i3586", "0$=Artist$1314$items", "Summer of '69", "Bryan Adams", "Rock", "", res, "object.item.audioItem.musicTrack");
////        logger.fatal("FAKE Track metadata xml: "+trackMetadataFAKE.getXML());
////        
////        - MediaInfo/TransportInfo mettere toString come PositionInfo (fare un toString da fuori)
////        - gestione servizi con gli stati.. gestire casi di interruzione stato (es. porta http già occupata; cartella N:\ non accessibile)
//        logger.fatal("Protocol info: "+trackMetadata.res.getProtocolInfo().toString());
////        trackMetadata.res.setProtocolInfo(new ProtocolInfo("http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000"));
////        logger.fatal("Protocol info tarocco: "+trackMetadata.res.getProtocolInfo().toString());
//        
//        rendererCommand.launchItem2(trackMetadata);
//        try {
//			TimeUnit.SECONDS.sleep(6);
//		} catch (Exception e) {}
//        
//        rendererCommand.setMute(true);
//        try {
//			TimeUnit.SECONDS.sleep(8);
//		} catch (Exception e) {}
//        
//        rendererCommand.setMute(false);
//        try {
//			TimeUnit.SECONDS.sleep(8);
//		} catch (Exception e) {}
//        
//        rendererCommand.commandPause();
//        try {
//			TimeUnit.SECONDS.sleep(8);
//		} catch (Exception e) {}
//        
//        if (rendererState.getState() != State.PLAY){
//	        rendererCommand.commandPlay();
//	        try {
//				TimeUnit.SECONDS.sleep(8);
//			} catch (Exception e) {}
//        }
//        
//        rendererCommand.commandStop();
		
        
        /////TODO scoprire perchè la pausa va con BubbleUPNP e non queste canzoni
        //forse un problema del webServer? probabile... tentare il fakeTrackMetadata
        
        
        
//        <?xml version="1.0" encoding="UTF-8"?>
//        <DIDL-Lite xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dlna="urn:schemas-dlna-org:metadata-1-0/" xmlns:pv="http://www.pv.com/pvns/" xmlns:sec="http://www.sec.co.kr/" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">
//           <item id="0$=Artist$1314$items$*i3586" parentID="0$=Artist$1314$items" restricted="1">
//              <upnp:class>object.item.audioItem.musicTrack</upnp:class>
//              <dc:title>Summer of '69</dc:title>
//              <dc:creator>Bryan Adams</dc:creator>
//              <upnp:artist>Bryan Adams</upnp:artist>
//              <upnp:albumArtURI>http://192.168.1.30:9790/minimserver/*/music/MP3s/Pop/Bryan*20Adams*20-*20Summer*20of*20*2769.mp3/$!picture-1361-31291.jpg</upnp:albumArtURI>
//              <upnp:genre>Rock</upnp:genre>
//              <dc:date>1997-01-01</dc:date>
//              <upnp:album>MTV Unplugged</upnp:album>
//              <upnp:originalTrackNumber>1</upnp:originalTrackNumber>
//              <res protocolInfo="http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000" bitrate="24000" sampleFrequency="44100" nrAudioChannels="2" size="5835548" duration="0:04:01.000">http://192.168.1.30:9790/minimserver/*/music/MP3s/Pop/Bryan*20Adams*20-*20Summer*20of*20*2769.mp3</res>
//           </item>
//        </DIDL-Lite>
        
	}

	@Override
	public void stop() {
		mediaServer.stop();
		
		upnpService.shutdown();
		
		saveToDisk();
	}

	@Override
	public void destroy() {
		mediaServer = null;
		
		upnpService = null;
		rendererState = null;
		rendererCommand = null;
		
		deviceRegistry = null;
	}
	
	
	private class MediaRenderersListener implements RegistryListener {
		
		private void registerOnline(RemoteDevice device){
			Device dev = DeviceFactory.fromClingDevice(device);
			dev = deviceRegistry.mergeDevice(dev);
			dev.registerOnline();
		}
		
		private void registerOffline(RemoteDevice device){
			Device dev = DeviceFactory.fromClingDevice(device);
			dev = deviceRegistry.mergeDevice(dev);
			dev.registerOffline();
		}
		
		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
//			logger.info("Discovery started: " + device.getDisplayString());
		}

		public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
//			logger.info("Discovery failed: " + device.getDisplayString() + " => " + ex);
		}

		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
//			logger.debug("Remote device available: " + device.getDisplayString());
			if (isValidDevice(device)){
				registerOnline(device);
			}
		}

		public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
//			logger.debug("Remote device updated: " + device.getDisplayString());
			if (isValidDevice(device)){
				registerOnline(device);
			}
		}

		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
//			logger.debug("Remote device removed: " + device.getDisplayString());
			if (isValidDevice(device)){
				registerOffline(device);
			}
		}

		public void localDeviceAdded(Registry registry, LocalDevice device) {
//			logger.info("Local device added: " + device.getDisplayString());
		}

		public void localDeviceRemoved(Registry registry, LocalDevice device) {
//			logger.info("Local device removed: " + device.getDisplayString());
		}

		public void beforeShutdown(Registry registry) {
//			logger.info("Before shutdown, the registry has devices: " + registry.getDevices().size());
		}

		public void afterShutdown() {
//			logger.info("Shutdown of registry complete!");
		}
	};
	
	
	@Override
	public boolean isDeviceSelected(){
		return deviceRegistry.getSelectedDevice() != null;
	}
	
	@Override
	public Device getSelectedDevice(){
		return deviceRegistry.getSelectedDevice();
	}
	
	@Override
	public void setSelectedDevice(String udn) throws DeviceNotFoundException{
		setSelectedDevice(getDevice(udn));
	}
	
	@Override
	public void setSelectedDevice(Device device){
		deviceRegistry.setSelectedDevice(device);
		rendererState.reset();
		rendererCommand.resume();
	}
	
	@Override
	public boolean isSelectedDeviceOnline() throws NoSelectedDeviceException{
		Device device = getSelectedDevice();
		if (device == null)
			throw new NoSelectedDeviceException();
		return isDeviceOnline(device);
	}
	
	@Override
	public void clearSelectedDevice(){
		deviceRegistry.clearSelectedDevice();
		rendererState.reset();
		rendererCommand.pause();
	}

	@Override
	public List<Device> getDevices(){
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXX usare gli streams o fare refactoring del registry
		return new ArrayList<Device>(deviceRegistry.values());
	}
	
	@Override
	public Device getDevice(String udn) throws DeviceNotFoundException {
		Device device = deviceRegistry.get(udn);
		if (device == null)
			throw new DeviceNotFoundException("Device "+udn+" not found");
		return device;
	}

	@Override
	public Device getDeviceByCustomName(String customName) throws DeviceNotFoundException {
		// TODO Auto-generated method stub
		return null;
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	}

	@Override
	public List<Device> getOnlineDevices(){
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXX usare gli streams o fare refactoring del registry
		List<Device> d = new ArrayList<Device>();
		for (Device device : deviceRegistry.values())
			d.add(device);
		return d;
	}
	
	@Override
	public boolean isDeviceOnline(Device device){
		return device.isOnline();
	}
	
	@Override
	public boolean isDeviceOnline(String udn) throws DeviceNotFoundException {
		return isDeviceOnline(getDevice(udn));
	}

	@Override
	public void setDeviceCustomName(String udn, String customName) throws DeviceNotFoundException {
		Device device = getDevice(udn);
		device.setCustomName(customName);
	}

	@Override
	public DeviceService getSelectedDeviceService(String serviceType) throws NoSelectedDeviceException {
		Device device = getSelectedDevice();
		DeviceService[] services = device.getServices();
		for (DeviceService service : services){
			if (service.getServiceType().equals(serviceType))
				return service;
		}
		return null;
	}
	
	

}
