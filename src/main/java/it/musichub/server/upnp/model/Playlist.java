package it.musichub.server.upnp.model;

import java.util.ArrayList;
import java.util.Arrays;

import it.musichub.server.library.model.Song;

public class Playlist extends ArrayList<Song> {

	private String name; //TODO XXXXXX da tenere qui???

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Playlist [name=" + name + ", songs=" + super.toString() + "]";
	}
	
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		sb.append("Playlist ["+name+"]\n");
		for (int i=0; i<size(); i++){
			sb.append(i+1+". "+getSongTitle(get(i))+"\n");
		}
		return sb.toString();
	}
	
	protected static String getSongTitle(Song song){
		return song.getArtist() + " - " + song.getTitle() + " ("+song.getReadableLength()+")";
	}
	
}
