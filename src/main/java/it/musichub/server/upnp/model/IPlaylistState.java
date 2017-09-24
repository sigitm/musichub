package it.musichub.server.upnp.model;

import java.util.List;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;

public interface IPlaylistState {

	//state
	public enum State {PLAY, STOP}
	public State getState();
	void setState(State state);
	
	//read
	Song getCurrentSong();
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
	
	//iteration
	boolean isEmpty();
	boolean hasNext();
	Song next();
	boolean hasPrevious();
	Song previous();
	Song first();
	Song last();
	Song selectPos(int pos);
	
	
	//TODO XXX XXXXXXXXXX DECIDERE SE TENERE QUI... sarebbero i comandi da interfaccia (con regole diverse!)
//	Song goToFirst();
//	Song goToPrevious();
//	Song goToNext();
//	Song goToLast();
	
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
}
