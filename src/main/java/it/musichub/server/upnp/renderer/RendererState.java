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

import org.apache.log4j.Logger;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;

import it.musichub.server.library.model.Song;
import it.musichub.server.upnp.model.IPlaylistState;
import it.musichub.server.upnp.model.PlaylistState;
import it.musichub.server.upnp.model.TrackMetadata;

public class RendererState implements IRendererState {

	private final static Logger logger = Logger.getLogger(RendererState.class);

	// / Player info
	private State state;
	private int volume;
	private boolean mute;
	private IPlaylistState playlist;

	// / Track info
	private PositionInfo positionInfo;
	private MediaInfo mediaInfo;
	private TransportInfo transportInfo;

	public RendererState()
	{
		super();

		reset();

//		notifyAllObservers();
	}
	
	@Override
	public void reset(){
		state = State.STOP;
		volume = -1;
		playlist = new PlaylistState();
		resetTrackInfo();
	}
	
	// / Getter/Setter

	@Override
	public State getState()
	{
		return state;
	}

	@Override
	public void setState(State state)
	{
		if (this.state == state){
			logger.debug("rendererState: confirming state "+state);
			return;
		}
		
		logger.debug("rendererState: updating state to "+state);

		if (state == State.STOP && (this.state == State.PLAY || this.state == State.PAUSE))
		{
			// Stop !
			resetTrackInfo();
		}

		this.state = state;
//		notifyAllObservers();
	}

	@Override
	public int getVolume()
	{
		return volume;
	}

	@Override
	public void setVolume(int volume)
	{
		if (this.volume == volume)
			return;

		this.volume = volume;
//		notifyAllObservers();
	}

	@Override
	public boolean isMute()
	{
		return mute;
	}

	@Override
	public void setMute(boolean mute)
	{
		if (this.mute == mute)
			return;

		this.mute = mute;
//		notifyAllObservers();
	}
	
	public IPlaylistState getPlaylist() {
		return playlist;
	}

	public void setPlaylist(IPlaylistState playlist) {
		this.playlist = playlist;
	}

	public void setPositionInfo(PositionInfo positionInfo)
	{
		try
		{
			if (this.positionInfo.getRelTime().compareTo(positionInfo.getRelTime()) == 0
					&& this.positionInfo.getAbsTime().compareTo(positionInfo.getAbsTime()) == 0)
				return;

			this.positionInfo = positionInfo;
//			notifyAllObservers();
		}
		catch (Exception e)
		{
			logger.error((e.getMessage() == null) ? "Ëxception !" : e.getMessage());
			for (StackTraceElement m : e.getStackTrace())
				logger.error(m.toString());
		}

	}

	public MediaInfo getMediaInfo()
	{
		return mediaInfo;
	}

	public void setMediaInfo(MediaInfo mediaInfo)
	{
		if (this.mediaInfo.hashCode() == mediaInfo.hashCode())
			return;

		this.mediaInfo = mediaInfo;
		// notifyAllObservers();
	}

	public TransportInfo getTransportInfo()
	{
		return transportInfo;
	}

	public void setTransportInfo(TransportInfo transportInfo)
	{
		this.transportInfo = transportInfo;
		logger.debug("Setting transportInfo with state "+transportInfo.getCurrentTransportState());

		if (transportInfo.getCurrentTransportState() == TransportState.PAUSED_PLAYBACK
				|| transportInfo.getCurrentTransportState() == TransportState.PAUSED_RECORDING)
			setState(State.PAUSE);
		else if (transportInfo.getCurrentTransportState() == TransportState.PLAYING)
			setState(State.PLAY);
		else
			// if(transportInfo.getCurrentTransportState() == TransportState.STOPPED)
			setState(State.STOP);
	}

	private TrackMetadata getTrackMetadata()
	{
		return new TrackMetadata(positionInfo.getTrackMetaData());
	}

	private String formatTime(long h, long m, long s)
	{
		return ((h >= 10) ? "" + h : "0" + h) + ":" + ((m >= 10) ? "" + m : "0" + m) + ":"
				+ ((s >= 10) ? "" + s : "0" + s);
	}

	@Override
	public String getRemainingDuration()
	{
		long t = positionInfo.getTrackRemainingSeconds();
		long h = t / 3600;
		long m = (t - h * 3600) / 60;
		long s = t - h * 3600 - m * 60;
		return "-" + formatTime(h, m, s);
	}

	@Override
	public String getDuration()
	{
		long t = positionInfo.getTrackDurationSeconds();
		long h = t / 3600;
		long m = (t - h * 3600) / 60;
		long s = t - h * 3600 - m * 60;
		return formatTime(h, m, s);
	}

	@Override
	public String getPosition()
	{
		long t = positionInfo.getTrackElapsedSeconds();
		long h = t / 3600;
		long m = (t - h * 3600) / 60;
		long s = t - h * 3600 - m * 60;
		return formatTime(h, m, s);
	}

	@Override
	public long getDurationSeconds()
	{
		return positionInfo.getTrackDurationSeconds();
	}

	public void resetTrackInfo()
	{
		positionInfo = new PositionInfo();
		mediaInfo = new MediaInfo();
//		notifyAllObservers();
	}

	@Override
	public String toString()
	{
		return "RendererState [state=" + state + ", volume=" + volume + ", repeatMode=" + /*repeatMode + ", randomMode="
				+ randomMode + */", positionInfo=" + positionInfo + ", mediaInfo=" + mediaInfo + ", trackMetadata="
				+ new TrackMetadata(positionInfo.getTrackMetaData()) + "]";
	}

	@Override
	public int getElapsedPercent()
	{
		return positionInfo.getElapsedPercent();
	}

	@Override
	public String getTitle()
	{
		return getTrackMetadata().title;
	}

	@Override
	public String getArtist()
	{
		return getTrackMetadata().artist;
	}
	
	@Override
	public Song getCurrentSong()
	{
		return getPlaylist().getCurrentSong();
	}

}
