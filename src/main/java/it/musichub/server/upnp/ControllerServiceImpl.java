package it.musichub.server.upnp;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import it.musichub.server.config.Constants;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.upnp.ex.SelectedDeviceNotAvailableException;
import it.musichub.server.upnp.ex.UpnpException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceService;
import it.musichub.server.upnp.model.DeviceServiceFactory;

public class ControllerServiceImpl extends MusicHubServiceImpl implements ControllerService {

	/**
	 * 
	 * ...
	 * 
	 */
	private ControlPoint controlPoint;
	
	private final static Logger logger = Logger.getLogger(ControllerServiceImpl.class);
	
	private static DiscoveryService getDiscoveryService(){
		return (DiscoveryService) ServiceFactory.getServiceInstance(Service.upnpdiscovery);
	}
	
	private static RemoteService getSelectedDeviceService(String serviceType) throws UpnpException {
		DiscoveryService ds = getDiscoveryService();
		
		if (!ds.isDeviceSelected() || !ds.isSelectedDeviceOnline())
			throw new SelectedDeviceNotAvailableException();
		
		Device device = ds.getSelectedDevice();
		DeviceService service = ds.getSelectedDeviceService(serviceType);

		if (service == null)
			throw new UpnpException("Service type "+serviceType+" not found");
		
		return DeviceServiceFactory.toClingDeviceService(device, service, ds.getUpnpService());
	}

	private static RemoteService getRenderingControlService() throws UpnpException {
		return getSelectedDeviceService(Constants.UPNP_SERVICE_TYPE_RENDERINGCONTROL);
	}

	private static RemoteService getAVTransportService() throws UpnpException {
		return getSelectedDeviceService(Constants.UPNP_SERVICE_TYPE_AVTRANSPORT);
	}
	
	private static ControlPoint getControlPoint() {
		DiscoveryService ds = getDiscoveryService();
		return ds.getUpnpService().getControlPoint();
	}
		
	
	@Override
	public void commandPlay() throws UpnpException {
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new Play(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.debug("Success playing ! ");
				// TODO update player state
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to play ! " + arg2);
			}
		});
	}
	
	@Override
	public void commandStop() throws UpnpException {
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new Stop(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.debug("Success stopping ! ");
				// TODO update player state
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to stop ! " + arg2);
			}
		});
	}

	@Override
	public void commandPause() throws UpnpException {
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new Pause(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.debug("Success pausing ! ");
				// TODO update player state
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to pause ! " + arg2);
			}
		});
	}

//	@Override
//	public void commandToggle()
//	{
//		RendererState.State state = rendererState.getState();
//		if (state == RendererState.State.PLAY)
//		{
//			commandPause();
//		}
//		else
//		{
//			commandPlay();
//		}
//	}

	@Override
	public void commandSeek(String relativeTimeTarget) throws UpnpException {
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new Seek(getAVTransportService(), relativeTimeTarget) {
			// TODO fix it, what is relativeTimeTarget ? :)

			@Override
			public void success(ActionInvocation invocation)
			{
				logger.debug("Success seeking !");
				// TODO update player state
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to seek ! " + arg2);
			}
		});
	}

	@Override
	public void setVolume(final int volume) throws UpnpException {
		if (getRenderingControlService() == null)
			return;

		controlPoint.execute(new SetVolume(getRenderingControlService(), volume) {
			@Override
			public void success(ActionInvocation invocation)
			{
				super.success(invocation);
				logger.debug("Success to set volume");
//				rendererState.setVolume(volume);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to set volume ! " + arg2);
			}
		});
	}

	@Override
	public void setMute(final boolean mute) throws UpnpException {
		if (getRenderingControlService() == null)
			return;

		controlPoint.execute(new SetMute(getRenderingControlService(), mute) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.debug("Success setting mute status ! ");
//				rendererState.setMute(mute);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to set mute status ! " + arg2);
			}
		});
	}

//	@Override
//	public void toggleMute()
//	{
//		setMute(!rendererState.isMute());
//	}

//	public void setURI(String uri, TrackMetadata trackMetadata) throws UpnpException {
//		logger.info("Set uri to " + uri);
//
//		controlPoint.execute(new SetAVTransportURI(getAVTransportService(), uri, trackMetadata.getXML()) {
//
//			@Override
//			public void success(ActionInvocation invocation)
//			{
//				super.success(invocation);
//				logger.info("URI successfully set !");
//				commandPlay();
//			}
//
//			@Override
//			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
//			{
//				logger.warn("Fail to set URI ! " + arg2);
//			}
//		});
//	}

//	@Override
//	public void launchItem(final IDIDLItem item)
//	{
//		if (getAVTransportService() == null)
//			return;
//
//		DIDLObject obj = ((ClingDIDLItem) item).getObject();
//		if (!(obj instanceof Item))
//			return;
//
//		Item upnpItem = (Item) obj;
//
//		String type = "";
//		if (upnpItem instanceof AudioItem)
//			type = "audioItem";
//		else if (upnpItem instanceof VideoItem)
//			type = "videoItem";
//		else if (upnpItem instanceof ImageItem)
//			type = "imageItem";
//		else if (upnpItem instanceof PlaylistItem)
//			type = "playlistItem";
//		else if (upnpItem instanceof TextItem)
//			type = "textItem";
//
//		// TODO genre && artURI
//		final TrackMetadata trackMetadata = new TrackMetadata(upnpItem.getId(), upnpItem.getTitle(),
//				upnpItem.getCreator(), "", "", upnpItem.getFirstResource().getValue(),
//				"object.item." + type);
//
//		logger.info("TrackMetadata : "+trackMetadata.toString());
//
//		// Stop playback before setting URI
//		controlPoint.execute(new Stop(getAVTransportService()) {
//			@Override
//			public void success(ActionInvocation invocation)
//			{
//				logger.debug("Success stopping ! ");
//				callback();
//			}
//
//			@Override
//			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
//			{
//				logger.warn("Fail to stop ! " + arg2);
//				callback();
//			}
//
//			public void callback()
//			{
//				setURI(item.getURI(), trackMetadata);
//			}
//		});
//
//	}

	

	
	@Override
	public void init() {
		DiscoveryService ds = getDiscoveryService();
		controlPoint = ds.getUpnpService().getControlPoint();
	}

	@Override
	public void start() {
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	}

	@Override
	public void stop() {
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	}

	@Override
	public void destroy() {
		controlPoint = null;
	}
	

}
