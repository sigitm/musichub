package it.musichub.server.upnp.model;

import java.util.ArrayList;

import it.musichub.server.library.model.Song;

public class Playlist extends ArrayList<Song> {

	private String name; //TODO XXXXXX da tenere qui???

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
