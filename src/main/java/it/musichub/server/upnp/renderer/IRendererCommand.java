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

import it.musichub.server.upnp.model.IPlaylistState;
import it.musichub.server.upnp.model.TrackMetadata;

public interface IRendererCommand {

	// Pause/resume backgroud state update
	public void pauseUpdates();

	public void resumeUpdates();

	// / Status
	public Boolean commandPlay(boolean sync);

	public Boolean commandStop(boolean sync);

	public Boolean commandPause(boolean sync);

	public Boolean commandToggle(boolean sync);

	public Boolean updateStatus(boolean sync);

	// / Position
	public Boolean commandSeek(String relativeTimeTarget, boolean sync);

	public Boolean updatePosition(boolean sync);

	// / Volume
	public Boolean setVolume(final int volume, boolean sync);

	public Boolean setMute(final boolean mute, boolean sync);

	public Boolean toggleMute(boolean sync);

	public Boolean updateVolume(boolean sync);

	// / URI
	public Boolean setURI(String uri, TrackMetadata trackMetadata, boolean sync);
//	public void launchItem(final IDIDLItem uri);
//	public void launchTrackMetadata(final TrackMetadata trackMetadata);
	public void launchPlaylist();

	// / Update
	public void updateFull(boolean sync);

	
	
	
	public void play();
	public void pause();
	public void stop();
	public void first();
	public void previous();
	public void next();
	public void last();
}
