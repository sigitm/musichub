package it.musichub.server.upnp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;

public class PlaylistState implements IPlaylistState {

	private State state;
	private Playlist songs;
	private List<Integer> songPointers;
	private Integer currentPointer;
	private boolean shuffle;
	private RepeatMode repeat; 
	
	public PlaylistState() {
		super();
		init();
	}
	
	@Override
	public State getState(){
		return state;
	}

	@Override
	public void setState(State state){
		this.state = state;
	}
	
	private synchronized void init(){
		clear();
		clearSettings();
	}
	
	@Override
	public synchronized void clear(){
		clearState();
		clearContent();
	}
	
	@Override
	public synchronized void clearState(){
		state = State.STOP;
		currentPointer = null;
	}
	
	private synchronized void clearContent(){
		songs = new Playlist();
		songPointers = new ArrayList<Integer>();
	}
	
	private synchronized void clearSettings(){
		shuffle = false;
		repeat = RepeatMode.OFF;
	}
	
	
	@Override
	public synchronized Song getCurrentSong(){
		if (currentPointer == null)
			return null;
		
		return getSongAtPointer(currentPointer);
	}
	
	private synchronized Song getSongAtPointer(int pointer){
		return songs.get(songPointers.get(pointer));
	}
	
//	private synchronized int getPointerForSong(Song song){
//		int pos = songs.indexOf(song); 
//		
//		if (pos == -1)
//			return -1;
//		
//		return songPointers.indexOf(pos);
//	}
	
	@Override
	public Song getSongById(String id){
		if (id == null)
			return null;
		
		for (Song song : songs){
			if (id.equals(song.getId()))
				return song;
		}
			
		return null;
	}
	
	@Override
	public List<Song> getSongs(){
		return songs;
	}
	
	@Override
	public synchronized boolean addSong(Song song){
		songs.add(song);
		songPointers.add(songs.size()-1);
		
		if (shuffle)
			shuffle();
		
		return true; //as in Collection.add(...) specification
	}
	
	@Override
	public void addSongs(List<Song> songs){
		for (Song song : songs)
			addSong(song);
	}
	
	@Override
	public void addFolder(Folder folder, boolean recursive){
		List<Song> songs = folder.getSongs();
		if (songs != null)
			addSongs(songs);
		
		if (recursive){
			List<Folder> subFolders = folder.getFolders();
			if (subFolders != null){
				for (Folder subFolder : subFolders)
					addFolder(subFolder, recursive);
			}
		}
	}
	
	//remove
	@Override
	public synchronized boolean removeSong(Song song){
		int pos = songs.indexOf(song);
		if (pos < 0)
			return false;
		
		songs.remove(pos);
		
		Integer pointer = new Integer(pos);
		songPointers.remove(pointer);
			
		//check if currentPointer is still valid
		if (currentPointer != null && currentPointer >= songPointers.size())
			currentPointer = songPointers.isEmpty() ? null : songPointers.size()-1;

		return true;
	}
	
	@Override
	public void removeSongs(List<Song> songs){
		for (Song song : songs)
			removeSong(song);
	}
	
	@Override
	public void keepSongs(List<Song> songs){
		for (Song song : this.songs){
			if (!songs.contains(song))
				removeSong(song);
		}
	}
	
	@Override
	public boolean isEmpty(){
		return songs.isEmpty();
	}
	
	@Override
	public boolean hasCurrent(){
		return getCurrentSong() != null;
	}
	
	@Override
	public synchronized boolean hasNext(){
		if (songPointers.isEmpty())
			return false;

		Boolean hasNext = null;
		if (currentPointer == null)
			hasNext = true;
		else{
			if (repeat == RepeatMode.TRACK){
				//Nothing to do: next is itself
				hasNext = true;
			}else{
				if (currentPointer < songPointers.size()-1){ //pointer is not in last position
					hasNext = true;
				}else{ //pointer is in last position
					if (repeat == RepeatMode.ALL)
						hasNext = true;
					else
						hasNext = false;
				}
			}
		}
		return hasNext;
	}

	@Override
	public synchronized Song next(){
		if (songPointers.isEmpty()){
			currentPointer = null; //should be null already
			return null;
		}
		
		if (currentPointer == null)
			currentPointer = 0;
		else{
			if (repeat == RepeatMode.TRACK){
				//Nothing to do: next is itself
			}else{
				if (currentPointer < songPointers.size()-1){ //pointer is not in last position
					currentPointer++;
				}else{ //pointer is in last position
					if (repeat == RepeatMode.ALL){
						currentPointer = 0;
						if (shuffle)
							shuffle();
					}else
						currentPointer = null;
				}
			}
		}
		return getCurrentSong();
	}
	
	@Override
	public synchronized boolean hasPrevious(){
		if (songPointers.isEmpty())
			return false;

		Boolean hasPrevious = null;
		if (currentPointer == null)
			hasPrevious = true;
		else{
			if (repeat == RepeatMode.TRACK){
				//Nothing to do: previous is itself
				hasPrevious = true;
			}else{
				if (currentPointer > 0){ //pointer is not in first position
					hasPrevious = true;
				}else{ //pointer is in first position
					if (repeat == RepeatMode.ALL)
						hasPrevious = true;
					else
						hasPrevious = false;
				}
			}
		}
		return hasPrevious;
	}
	
	@Override
	public synchronized Song previous(){
		if (songPointers.isEmpty()){
			currentPointer = null; //should be null already
			return null;
		}
		
		if (currentPointer == null)
			currentPointer = 0;
		else{
			if (repeat == RepeatMode.TRACK){
				//Nothing to do: previous is itself
			}else{
				if (currentPointer > 0){ //pointer is not in first position
					currentPointer--;
				}else{ //pointer is in first position
					if (repeat == RepeatMode.ALL)
						currentPointer = 0;
					else
						currentPointer = null;
				}
			}
		}
		return getCurrentSong();
	}
	
	@Override
	public synchronized Song first(){
		if (songPointers.isEmpty()){
			currentPointer = null; //should be null already
			return null;
		}
		
		return selectPos(0);
	}
	
	@Override
	public synchronized Song last(){
		if (songPointers.isEmpty()){
			currentPointer = null; //should be null already
			return null;
		}
		
		return selectPos(songs.size()-1);
	}
	
	@Override
	public synchronized Song selectPos(int pos){
		if (songPointers.isEmpty()){
			currentPointer = null; //should be null already
			return null;
		}
		
		if (pos < 0 || pos > songPointers.size()-1)
			return null;
		
		currentPointer = songPointers.indexOf(pos);
		
		return getCurrentSong();
	}

	@Override
	public boolean getShuffle() {
		return shuffle;
	}

	@Override
	public synchronized void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
		
		if (shuffle){
			shuffle();
		}else{
			Integer songPointer = songPointers.get(currentPointer);
			for (int i=0;i<songPointers.size();i++)
				songPointers.set(i, i);
			currentPointer = songPointers.indexOf(songPointer);
		}
	}
	
	@Override
	public synchronized void shuffle() {
		Integer songPointer = null;
		if (currentPointer != null)
			songPointer = songPointers.get(currentPointer);
		
		Collections.shuffle(songPointers);
		
		if (songPointer != null) //restoring current song pointer
			currentPointer = songPointers.indexOf(songPointer);
	}

	@Override
	public RepeatMode getRepeat() {
		return repeat;
	}

	@Override
	public void setRepeat(RepeatMode repeat) {
		this.repeat = repeat;
	}

	@Override
	public String toString() {
		return "PlaylistState [playlist=" + songs + ", currentSong=" + getCurrentSong() + ", shuffle=" + shuffle
				+ ", repeat=" + repeat + "]";
	}
	
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		sb.append("Playlist ["+songs.getName()+"]\n");
		for (int i=0; i<songs.size(); i++){
			Song song = songs.get(i);
			String sel = StringUtils.EMPTY;
			if (song.equals(getCurrentSong()))
				sel = " (SELECTED)";
			sb.append(i+1+". "+Playlist.getSongTitle(song)+sel+"\n");
		}
		sb.append("SHUFFLE="+shuffle+", REPEAT="+repeat);
		return sb.toString();
	}
	
}
