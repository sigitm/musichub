/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * 
 * This file is part of DroidUPNP.
 * 
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.musichub.server.upnp.model.x;

import org.apache.log4j.Logger;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.PlaylistItem;
import org.fourthline.cling.support.model.item.TextItem;
import org.fourthline.cling.support.model.item.VideoItem;
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.upnp.UpnpControllerService;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceFactory;
import it.musichub.server.upnp.model.IPlaylistState;
import it.musichub.server.upnp.model.x.IRendererState.State;

@SuppressWarnings("rawtypes")
public class RendererCommand implements Runnable, IRendererCommand {

//	private static final String TAG = "RendererCommand";
	private final static Logger logger = Logger.getLogger(RendererCommand.class);

	private final RendererState rendererState;
	private final ControlPoint controlPoint;

	public Thread thread;
	boolean pause = false;

	public RendererCommand(ControlPoint controlPoint, RendererState rendererState)
	{
		this.rendererState = rendererState;
		this.controlPoint = controlPoint;

		thread = new Thread(this);
		pause = true;
	}

	@Override
	public void finalize()
	{
		this.pause();
	}

	@Override
	public void pause()
	{
		logger.trace("Interrupt");
		pause = true;
		thread.interrupt();
	}

	@Override
	public void resume()
	{
		logger.trace("Resume");
		pause = false;
		if (!thread.isAlive())
			thread.start();
		else
			thread.interrupt();
	}

	private static UpnpControllerService getUpnpControllerService(){
		return (UpnpControllerService) ServiceFactory.getServiceInstance(it.musichub.server.runner.ServiceRegistry.Service.upnpcontroller);
	}
	
	public static Service getRenderingControlService() {
		UpnpControllerService ds = getUpnpControllerService();
		try {
			if (!ds.isDeviceSelected() || !ds.isSelectedDeviceOnline())
				return null;
		} catch (NoSelectedDeviceException e) {
			logger.error("error asking if selected device is offline", e); ////TODO XXX sistemare........
			return null;
		}

		Device device = ds.getSelectedDevice();
		RemoteDevice clingDevice = DeviceFactory.toClingDevice(device, ds.getUpnpService());
		if (clingDevice == null)
			return null;
		
		return clingDevice.findService(new UDAServiceType("RenderingControl"));
	}

	public static Service getAVTransportService() {
		UpnpControllerService ds = getUpnpControllerService();
		try {
			if (!ds.isDeviceSelected() || !ds.isSelectedDeviceOnline())
				return null;
		} catch (NoSelectedDeviceException e) {
			logger.error("error asking if selected device is offline", e); ////TODO XXX sistemare........
			return null;
		}

		Device device = ds.getSelectedDevice();
		RemoteDevice clingDevice = DeviceFactory.toClingDevice(device, ds.getUpnpService());
		if (clingDevice == null)
			return null;
		
		return clingDevice.findService(new UDAServiceType("AVTransport"));
	}

	@Override
	public void commandPlay()
	{
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new Play(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.trace("Success playing ! ");
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
	public void commandStop()
	{
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new Stop(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.trace("Success stopping ! ");
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
	public void commandPause()
	{
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new Pause(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.trace("Success pausing ! ");
				// TODO update player state
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to pause ! " + arg2);
			}
		});
	}

	@Override
	public void commandToggle()
	{
		RendererState.State state = rendererState.getState();
		if (state == RendererState.State.PLAY)
		{
			commandPause();
		}
		else
		{
			commandPlay();
		}
	}

	@Override
	public void commandSeek(String relativeTimeTarget)
	{
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new Seek(getAVTransportService(), relativeTimeTarget) {
			// TODO fix it, what is relativeTimeTarget ? :)

			@Override
			public void success(ActionInvocation invocation)
			{
				logger.trace("Success seeking !");
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
	public void setVolume(final int volume)
	{
		if (getRenderingControlService() == null)
			return;

		controlPoint.execute(new SetVolume(getRenderingControlService(), volume) {
			@Override
			public void success(ActionInvocation invocation)
			{
				super.success(invocation);
				logger.trace("Success to set volume");
				rendererState.setVolume(volume);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to set volume ! " + arg2);
			}
		});
	}

	@Override
	public void setMute(final boolean mute)
	{
		if (getRenderingControlService() == null)
			return;

		controlPoint.execute(new SetMute(getRenderingControlService(), mute) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.trace("Success setting mute status ! ");
				rendererState.setMute(mute);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to set mute status ! " + arg2);
			}
		});
	}

	@Override
	public void toggleMute()
	{
		setMute(!rendererState.isMute());
	}

	public void setURI(String uri, TrackMetadata trackMetadata)
	{
		logger.info("Set uri to " + uri);

		controlPoint.execute(new SetAVTransportURI(getAVTransportService(), uri, trackMetadata.getXML()) {

			@Override
			public void success(ActionInvocation invocation)
			{
				super.success(invocation);
				logger.info("URI successfully set !");
				commandPlay();
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to set URI ! " + arg2);
			}
		});
	}

	@Override
	public void launchItem(final IDIDLItem item)
	{
		if (getAVTransportService() == null)
			return;

		DIDLObject obj = ((ClingDIDLItem) item).getObject();
		if (!(obj instanceof Item))
			return;

		Item upnpItem = (Item) obj;

		String type = "";
		if (upnpItem instanceof AudioItem)
			type = "audioItem";
		else if (upnpItem instanceof VideoItem)
			type = "videoItem";
		else if (upnpItem instanceof ImageItem)
			type = "imageItem";
		else if (upnpItem instanceof PlaylistItem)
			type = "playlistItem";
		else if (upnpItem instanceof TextItem)
			type = "textItem";

		// TODO genre && artURI
		final TrackMetadata trackMetadata = new TrackMetadata(upnpItem.getId(), upnpItem.getParentID(), upnpItem.getTitle(),
				upnpItem.getCreator(), "", "", upnpItem.getFirstResource(),
				"object.item." + type);

		logger.info("TrackMetadata : "+trackMetadata.toString());

		// Stop playback before setting URI
		controlPoint.execute(new Stop(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.trace("Success stopping ! ");
				callback();
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to stop ! " + arg2);
				callback();
			}

			public void callback()
			{
				setURI(item.getURI(), trackMetadata);
			}
		});

	}
	
	@Override
	public void launchItem2(final TrackMetadata trackMetadata)
	{
		if (getAVTransportService() == null)
			return;

		logger.info("TrackMetadata : "+trackMetadata.toString());

		// Stop playback before setting URI
		controlPoint.execute(new Stop(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation)
			{
				logger.trace("Success stopping ! ");
				callback();
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to stop ! " + arg2);
				callback();
			}

			public void callback()
			{
				setURI(trackMetadata.res.getValue(), trackMetadata);
			}
		});

	}
	
	@Override
	public void launchPlaylist(final IPlaylistState playlistState){
		
	}
	
	private void handlePlaylist(){
		IPlaylistState playlist = rendererState.getPlaylist();
		if (playlist != null){
			if (rendererState.getState() == State.STOP && playlist.hasNext() && playlist.getState() == IPlaylistState.State.PLAY){
				//the previous song is over
				//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX launch next song....
			}
		}
	}
		
	
	

	// Update

	public void updateMediaInfo()
	{
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new GetMediaInfo(getAVTransportService()) {
			@Override
			public void received(ActionInvocation arg0, MediaInfo arg1)
			{
				logger.debug("Receive media info ! " + arg1);
				rendererState.setMediaInfo(arg1);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to get media info ! " + arg2);
			}
		});
	}

	public void updatePositionInfo()
	{
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new GetPositionInfo(getAVTransportService()) {
			@Override
			public void received(ActionInvocation arg0, PositionInfo arg1)
			{
				logger.debug("Receive position info ! " + arg1);
				rendererState.setPositionInfo(arg1);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to get position info ! " + arg2);
			}
		});
	}

	public void updateTransportInfo()
	{
		if (getAVTransportService() == null)
			return;

		controlPoint.execute(new GetTransportInfo(getAVTransportService()) {
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to get transport info ! " + arg2);
			}

			@Override
			public void received(ActionInvocation arg0, TransportInfo arg1)
			{
				logger.debug("Receive transport info ! " + arg1);
				rendererState.setTransportInfo(arg1);
				handlePlaylist();
			}
		});
	}

	@Override
	public void updateVolume()
	{
		if (getRenderingControlService() == null)
			return;

		controlPoint.execute(new GetVolume(getRenderingControlService()) {
			@Override
			public void received(ActionInvocation arg0, int arg1)
			{
				logger.debug("Receive volume ! " + arg1);
				rendererState.setVolume(arg1);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to get volume ! " + arg2);
			}
		});
	}

	public void updateMute()
	{
		if (getRenderingControlService() == null)
			return;

		controlPoint.execute(new GetMute(getRenderingControlService()) {
			@Override
			public void received(ActionInvocation arg0, boolean arg1)
			{
				logger.debug("Receive mute status ! " + arg1);
				rendererState.setMute(arg1);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
			{
				logger.warn("Fail to get mute status ! " + arg2);
			}
		});
	}

	@Override
	public void updateFull()
	{
		updateMediaInfo();
		updatePositionInfo();
		updateVolume();
		updateMute();
		updateTransportInfo();
	}

	@Override
	public void run()
	{
		// LastChange lastChange = new LastChange(new AVTransportLastChangeParser(),
		// AVTransportVariable.CurrentTrackMetaData.class);

		// SubscriptionCallback callback = new SubscriptionCallback(getRenderingControlService(), 600) {
		//
		// @Override
		// public void established(GENASubscription sub)
		// {
		// logger.error("Established: " + sub.getSubscriptionId());
		// }
		//
		// @Override
		// public void failed(GENASubscription sub, UpnpResponse response, Exception ex, String msg)
		// {
		// logger.error(createDefaultFailureMessage(response, ex));
		// }
		//
		// @Override
		// public void ended(GENASubscription sub, CancelReason reason, UpnpResponse response)
		// {
		// // Reason should be null, or it didn't end regularly
		// }
		//
		// @Override
		// public void eventReceived(GENASubscription sub)
		// {
		// logger.error("Event: " + sub.getCurrentSequence().getValue());
		// Map<String, StateVariableValue> values = sub.getCurrentValues();
		// StateVariableValue status = values.get("Status");
		// if (status != null)
		// logger.error("Status is: " + status.toString());
		// }
		//
		// @Override
		// public void eventsMissed(GENASubscription sub, int numberOfMissedEvents)
		// {
		// logger.error("Missed events: " + numberOfMissedEvents);
		// }
		// };

		// controlPoint.execute(callback);

		while (true)
			try
			{
				int count = 0;
				while (true)
				{
					if (!pause)
					{
						logger.debug("Update state !");

						count++;

						updatePositionInfo();

						if ((count % 3) == 0)
						{
							updateVolume();
							updateMute();
							updateTransportInfo();
						}

						if ((count % 6) == 0)
						{
							updateMediaInfo();
						}
					}
					Thread.sleep(1000);
				}
			}
			catch (InterruptedException e)
			{
				logger.info("State updater interrupt, new state " + ((pause) ? "pause" : "running"));
			}
	}

	@Override
	public void updateStatus()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void updatePosition()
	{
		// TODO Auto-generated method stub

	}
}
