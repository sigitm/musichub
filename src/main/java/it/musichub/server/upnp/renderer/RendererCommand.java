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

package it.musichub.server.upnp.renderer;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.mutable.MutableBoolean;
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
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import it.musichub.server.library.model.Song;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.upnp.MediaServer;
import it.musichub.server.upnp.UpnpControllerService;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceFactory;
import it.musichub.server.upnp.model.IPlaylistState;
import it.musichub.server.upnp.model.TrackMetadata;
import it.musichub.server.upnp.model.UpnpFactory;
import it.musichub.server.upnp.renderer.IRendererState.State;

@SuppressWarnings("rawtypes")
public class RendererCommand implements Runnable, IRendererCommand {

//	private static final String TAG = "RendererCommand";
	private final static Logger logger = Logger.getLogger(RendererCommand.class);

	private final RendererState rendererState;
	private final ControlPoint controlPoint;

	public Thread thread;
	boolean pause = false;

	public RendererCommand(ControlPoint controlPoint, RendererState rendererState){
		this.rendererState = rendererState;
		this.controlPoint = controlPoint;

		thread = new Thread(this);
		pause = true;
	}

	@Override
	public void finalize(){
		this.pauseUpdates();
	}

	@Override
	public void pauseUpdates(){
		logger.trace("Interrupt");
		pause = true;
		thread.interrupt();
	}

	@Override
	public void resumeUpdates(){
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
	
	private static Service getRenderingControlService() {
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

	private static Service getAVTransportService() {
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

	private static void syncWait(Future f){
		try {
			f.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.warn("Error waiting for async operation to end", e);
		}
	}
	
	
	// ********************** AVTransportService commands **********************
	
	@Override
	public Boolean commandPlay(final boolean sync){
    	final MutableBoolean result = new MutableBoolean(false);
    	
		if (getAVTransportService() == null)
			return null;
		
    	Future f = controlPoint.execute(new Play(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation){
				logger.trace("Success playing ! ");
				
				result.setTrue();
				
				//success related operations
				updateTransportInfo(sync);
				updatePositionInfo(sync);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to play ! " + arg2);
				
				result.setFalse();
			}
		});
    	
    	if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}
	
	@Override
	public Boolean commandStop(final boolean sync){
    	final MutableBoolean result = new MutableBoolean(false);
    	
		if (getAVTransportService() == null)
			return null;
		
    	Future f = controlPoint.execute(new Stop(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation){
				logger.trace("Success stopping ! ");
				// TODO update player state
				
				result.setTrue();
				
				//success related operations
				updateTransportInfo(sync);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to stop ! " + arg2);
				
				result.setFalse();
			}
		});
    	
    	if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}
	
	@Override
	public Boolean commandPause(final boolean sync){
    	final MutableBoolean result = new MutableBoolean(false);
    	
		if (getAVTransportService() == null)
			return null;
		
    	Future f = controlPoint.execute(new Stop(getAVTransportService()) {
			@Override
			public void success(ActionInvocation invocation){
				logger.trace("Success stopping ! ");
				// TODO update player state
				
				result.setTrue();
				
				//success related operations
				updateTransportInfo(sync);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to stop ! " + arg2);
				
				result.setFalse();
			}
		});
    	
    	if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}
	
	@Override
	public Boolean commandToggle(final boolean sync){
		RendererState.State state = rendererState.getState();
		if (state == RendererState.State.PLAY)
			return commandPause(sync);
		else
			return commandPlay(sync);
	}

	@Override
	public Boolean commandSeek(String relativeTimeTarget, final boolean sync){
		final MutableBoolean result = new MutableBoolean(false);
		
		if (getAVTransportService() == null)
			return null;

		Future f = controlPoint.execute(new Seek(getAVTransportService(), relativeTimeTarget) {
			// TODO fix it, what is relativeTimeTarget ? :)

			@Override
			public void success(ActionInvocation invocation){
				logger.trace("Success seeking !");
				// TODO update player state
				
				result.setTrue();
				
				//success related operations
				updateTransportInfo(sync);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to seek ! " + arg2);
				
				result.setFalse();
			}
		});
		
    	if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}
	
	@Override
	public Boolean setURI(String uri, TrackMetadata trackMetadata, final boolean sync){
		final MutableBoolean result = new MutableBoolean(false);
		
		if (getAVTransportService() == null)
			return null;
		
		logger.info("Setting uri to " + uri);

		Future f = controlPoint.execute(new SetAVTransportURI(getAVTransportService(), uri, trackMetadata.getXML()) {

			@Override
			public void success(ActionInvocation invocation){
				logger.info("URI successfully set !");
				
				result.setTrue();
				
				//success related operations
				updateMediaInfo(sync);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to set URI ! " + arg2);
				
				result.setFalse();
			}
		});
		
    	if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}

	
	// ********************** RenderingControlService commands **********************
	
	@Override
	public Boolean setVolume(final int volume, final boolean sync){
		final MutableBoolean result = new MutableBoolean(false);
		
		if (getRenderingControlService() == null)
			return null;

		Future f = controlPoint.execute(new SetVolume(getRenderingControlService(), volume) {
			@Override
			public void success(ActionInvocation invocation){
				logger.trace("Success to set volume");
				
				result.setTrue();
				
				//success related operations
				rendererState.setVolume(volume);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to set volume ! " + arg2);
				
				result.setFalse();
			}
		});
		
		if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}

	@Override
	public Boolean setMute(final boolean mute, final boolean sync){
		final MutableBoolean result = new MutableBoolean(false);
		
		if (getRenderingControlService() == null)
			return null;

		Future f = controlPoint.execute(new SetMute(getRenderingControlService(), mute) {
			@Override
			public void success(ActionInvocation invocation){
				logger.trace("Success setting mute status ! ");
				
				result.setTrue();
				
				//success related operations
				rendererState.setMute(mute);
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to set mute status ! " + arg2);
				
				result.setFalse();
			}
		});
		
		if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}

	@Override
	public Boolean toggleMute(final boolean sync){
		return setMute(!rendererState.isMute(), sync);
	}

//	private void invokeBooleanMethod(Method method, boolean value){
//		try {
//			method.setAccessible(true);
//			method.invoke(this, value);
//		} catch (Exception e) {
//			logger.warn("Cannot invoke callback method "+method+" with value "+value);
//		}
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
//		final TrackMetadata trackMetadata = new TrackMetadata(upnpItem.getId(), upnpItem.getParentID(), upnpItem.getTitle(),
//				upnpItem.getCreator(), "", "", upnpItem.getFirstResource(),
//				"object.item." + type);
//
//		logger.info("TrackMetadata : "+trackMetadata.toString());
//
//		// Stop playback before setting URI
//		controlPoint.execute(new Stop(getAVTransportService()) {
//			@Override
//			public void success(ActionInvocation invocation)
//			{
//				logger.trace("Success stopping ! ");
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
	
////	@Override
//	private void launchTrackMetadata(final TrackMetadata trackMetadata)
//	{
//		if (getAVTransportService() == null)
//			return;
//
//		logger.info("TrackMetadata : "+trackMetadata.toString());
//
//		// Stop playback before setting URI
//		controlPoint.execute(new Stop(getAVTransportService()) {
//			@Override
//			public void success(ActionInvocation invocation)
//			{
//				logger.trace("Success stopping ! ");
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
//				setURI(trackMetadata.res.getValue(), trackMetadata);
//			}
//		});
//
//	}
	
	private synchronized void launchTrackMetadata2(final TrackMetadata trackMetadata){
	//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	//TODO fare tutti i metodi _sync e _async? XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	/*
	 * playlistState = LAUNCHING
	 * stop_sync (result non importante)
	 * setURI_sync
	 * if (result)
	 *    result = playCommand_sync
	 *    [if state==play] //devo controllare che sia andato bene?? in caso contrario potrei settare un playlist.stop
	 *    aggiornare il playlistState a PLAY/STOP in base al result (A QUESTO PUNTO handlePlaylist NON DOVREBBE INTERFERIRE IN QUANTO HO GIA' AGG. IL TRANSPORT)
	 * else
	 *    playlistState = STOP
	 */
	
		IPlaylistState playlist = rendererState.getPlaylist();
		playlist.setState(IPlaylistState.State.LAUNCHING);
		
		// Stopping potential playback before setting URI
		commandStop(true);
		
		// Setting URI
		Boolean res = setURI(trackMetadata.res.getValue(), trackMetadata, true);
		
		if (Boolean.TRUE.equals(res)){
			//playing selected URI
			res = commandPlay(true);
			
			playlist.setState(Boolean.TRUE.equals(res) ? IPlaylistState.State.PLAY : IPlaylistState.State.STOP);
		}else{
			playlist.setState(IPlaylistState.State.STOP);
		}
	}
	
	private void launchSong(final Song song){
		IPlaylistState playlist = rendererState.getPlaylist();
		playlist.setState(IPlaylistState.State.LAUNCHING);///TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX l'ho già messo di là
		
		MediaServer mediaServer = getUpnpControllerService().getMediaServer();
		TrackMetadata trackMetadata = UpnpFactory.songToTrackMetadata(mediaServer, song);
		/*Method callbackMethod = null;
		try {
			callbackMethod = this.getClass().getDeclaredMethod("launchSongCallback", boolean.class);
		} catch (Exception e) {
			logger.error("Error loading callback method launchSongCallback", e);
		}*/
		launchTrackMetadata2(trackMetadata);
	}
	
	@Override
	public synchronized void launchPlaylist(){
		IPlaylistState playlist = rendererState.getPlaylist();
		
		if (playlist.hasCurrent()){
			Song song = playlist.getCurrentSong();
			logger.debug("launchPlaylist: launching current song: "+song);
			launchSong(song);
		}else if (playlist.hasNext()){
			Song song = playlist.next();
			logger.debug("launchPlaylist: launching next song: "+song);
			launchSong(song);
		}else{
			logger.debug("launchPlaylist: stopping playlist");
			playlist.setState(IPlaylistState.State.STOP);
		}
	}
	
	private synchronized void handlePlaylistProgression(){
		IPlaylistState playlist = rendererState.getPlaylist();
		if (playlist != null){
			if (rendererState.getState() == State.STOP && playlist.getState() == IPlaylistState.State.PLAY){
				//the previous song is over
				if (playlist.hasNext()){
					//launching next song....
					Song song = playlist.next();
					logger.debug("handlePlaylist: launching next song: "+song);
					launchSong(song);
				}else{
					logger.debug("handlePlaylist: playlist ended");
					playlist.setState(IPlaylistState.State.STOP);
				}
			}
		}
	}
	
	
	

	// ********************** Updates **********************

	public Boolean updateMediaInfo(final boolean sync){
		final MutableBoolean result = new MutableBoolean(false);
		
		if (getAVTransportService() == null)
			return null;

		Future f = controlPoint.execute(new GetMediaInfo(getAVTransportService()) {
			@Override
			public void received(ActionInvocation arg0, MediaInfo arg1){
				logger.debug("Receive media info ! " + arg1);
				rendererState.setMediaInfo(arg1);
				
				result.setTrue();
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to get media info ! " + arg2);
				
				result.setFalse();
			}
		});
		
		if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}

	public Boolean updatePositionInfo(final boolean sync){
		final MutableBoolean result = new MutableBoolean(false);

		if (getAVTransportService() == null)
			return null;

		Future f = controlPoint.execute(new GetPositionInfo(getAVTransportService()) {
			@Override
			public void received(ActionInvocation arg0, PositionInfo arg1){
				logger.debug("Receive position info ! " + arg1);
				rendererState.setPositionInfo(arg1);
				
				result.setTrue();
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to get position info ! " + arg2);
				
				result.setFalse();
			}
		});
		
		if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}

	public Boolean updateTransportInfo(final boolean sync){
		final MutableBoolean result = new MutableBoolean(false);
		
		if (getAVTransportService() == null)
			return null;

		Future f = controlPoint.execute(new GetTransportInfo(getAVTransportService()) {
			@Override
			public void received(ActionInvocation arg0, TransportInfo arg1){
				logger.debug("Receive transport info ! " + arg1);
				rendererState.setTransportInfo(arg1);
				
				result.setTrue();
				
				//success related operations
				handlePlaylistProgression();
			}
			
			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to get transport info ! " + arg2);
				
				result.setFalse();
			}
		});
		
		if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}

	@Override
	public Boolean updateVolume(final boolean sync){
		final MutableBoolean result = new MutableBoolean(false);

		if (getRenderingControlService() == null)
			return null;

		Future f = controlPoint.execute(new GetVolume(getRenderingControlService()) {
			@Override
			public void received(ActionInvocation arg0, int arg1){
				logger.debug("Receive volume ! " + arg1);
				rendererState.setVolume(arg1);
				
				result.setTrue();
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to get volume ! " + arg2);
				
				result.setFalse();
			}
		});
		
		if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}

	public Boolean updateMute(final boolean sync){
		final MutableBoolean result = new MutableBoolean(false);

		if (getRenderingControlService() == null)
			return null;

		Future f = controlPoint.execute(new GetMute(getRenderingControlService()) {
			@Override
			public void received(ActionInvocation arg0, boolean arg1){
				logger.debug("Receive mute status ! " + arg1);
				rendererState.setMute(arg1);
				
				result.setTrue();
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2){
				logger.warn("Fail to get mute status ! " + arg2);
				
				result.setFalse();
			}
		});
		
		if (sync){
    		syncWait(f);
			return result.getValue();
    	}
    	
    	return null;
	}

	@Override
	public void updateFull(final boolean sync)
	{
		updateMediaInfo(sync);
		updatePositionInfo(sync);
		updateVolume(sync);
		updateMute(sync);
		updateTransportInfo(sync);
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

						updatePositionInfo(false);

						if ((count % 3) == 0)
						{
							updateVolume(false);
							updateMute(false);
							updateTransportInfo(false);
						}

						if ((count % 6) == 0)
						{
							updateMediaInfo(false);
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
	public Boolean updateStatus(final boolean sync){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean updatePosition(final boolean sync){
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	//----------------------------------EXPERIMENTS
	
	/*
	 * XXXXXXXXXXXX
	 * 
	 * li faccio tutti sincroni i comandi da interfaccia? potrebbe essere un'idea
	 */
	
	public synchronized void play(){
		/**
		 * se è già in play, riparte la stessa canzone (QUELLA PUNTATA DALLA PLAYLIST) da 0:00 (e risetta la playlistState a play? verificare se serve)
		 * se è in stop, parte la canzone (QUELLA PUNTATA DALLA PLAYLIST)
		 * 
		 * in pratica è sempre un launchPlaylist()
		 */
		launchPlaylist();
	}
	
	public synchronized void pause(){
		commandToggle(true);
	}
	
	public synchronized void stop(){
		IPlaylistState playlist = rendererState.getPlaylist();
		playlist.setState(IPlaylistState.State.STOP);
		
		commandStop(true);
	}
	
	public synchronized void first(){
		IPlaylistState playlist = rendererState.getPlaylist();
		if (!playlist.hasPrevious())
			return;
		
		RendererState.State state = rendererState.getState();
		boolean wasPlaying = (state == RendererState.State.PLAY);
		 
		if (wasPlaying)
			stop();
		
		playlist.first();

		if (wasPlaying)
			play();
	}
	
	public synchronized void previous(){
		IPlaylistState playlist = rendererState.getPlaylist();
		if (!playlist.hasPrevious())
			return;
		
		RendererState.State state = rendererState.getState();
		boolean wasPlaying = (state == RendererState.State.PLAY);
		 
		if (wasPlaying)
			stop();
		
		playlist.previous();

		if (wasPlaying)
			play();
	}
	
	public synchronized void next(){
		IPlaylistState playlist = rendererState.getPlaylist();
		if (!playlist.hasNext())
			return;
		
		RendererState.State state = rendererState.getState();
		boolean wasPlaying = (state == RendererState.State.PLAY);
		 
		if (wasPlaying)
			stop();
		
		playlist.next();

		if (wasPlaying)
			play();
	}
	
	public synchronized void last(){
		IPlaylistState playlist = rendererState.getPlaylist();
		if (!playlist.hasNext())
			return;
		
		RendererState.State state = rendererState.getState();
		boolean wasPlaying = (state == RendererState.State.PLAY);
		 
		if (wasPlaying)
			stop();
		
		playlist.last();

		if (wasPlaying)
			play();
	}
	
	
	
	
}
