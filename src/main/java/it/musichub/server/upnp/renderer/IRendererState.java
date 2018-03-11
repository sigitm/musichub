package it.musichub.server.upnp.renderer;

import it.musichub.server.library.model.Song;
import it.musichub.server.upnp.model.IPlaylistState;

public interface IRendererState {

	enum State {PLAY, PAUSE, STOP}
	void reset();
	
	State getState();
	void setState(State state);

	int getVolume();
	void setVolume(int volume);
	
	IPlaylistState getPlaylist();
	void setPlaylist(IPlaylistState playlist);

	boolean isMute();
	void setMute(boolean mute);

	String getDuration();
	String getRemainingDuration();
	String getPosition();
	int getElapsedPercent();
	long getDurationSeconds();
	String getTitle();
	String getArtist();
	Song getCurrentSong();

}
