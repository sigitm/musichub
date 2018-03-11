package it.musichub.server.upnp.model;

import java.util.List;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;

public interface IPlaylistState {

	//state
	public enum State {PLAY, LAUNCHING, STOP}
	public State getState();
	void setState(State state);
	
	//read
	Song getCurrentSong();
	Song getSongById(String id);
	List<Song> getSongs();
	
	//add
	boolean addSong(Song song);
	void addSongs(List<Song> songs);
	void addFolder(Folder folder, boolean recursive);
	
	//remove
	boolean removeSong(Song song);
	void removeSongs(List<Song> songs);
	void keepSongs(List<Song> songs);
	void clear();
	void clearState();
	
	//iteration
	boolean isEmpty();
	boolean hasCurrent();
	boolean hasNext();
	Song next();
	boolean hasPrevious();
	Song previous();
	Song first();
	Song last();
	Song selectPos(int pos);

	//options
	public enum RepeatMode {OFF, TRACK, ALL}
	boolean getShuffle();
	void setShuffle(boolean shuffle);   //devo anche mescolare il songPointers? 
	void shuffle();  //rimescolo!
	RepeatMode getRepeat(); 
	void setRepeat(RepeatMode repeat);
	
	//loadPlaylist(String name)
	//savePlaylist(String name)
	//exportPlaylist()  --esporta la playlist in un m3u8
	//importPlaylist()  --importa la playlist da un m3u8
	
	public String prettyPrint();
}
