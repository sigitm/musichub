package it.musichub.server.library.model;

import java.io.Serializable;
import java.util.Comparator;

import it.musichub.server.library.utils.SmartComparator;

public class Album implements Serializable, Comparable<Album> {

	//KEYS
	private String artist;
	private String title;
	
	//ADDITIONAL INFOS
	private Integer year;
	private String genre;
	private byte[] albumImage;
	private String albumImageMimeType;
	
	public Album(String artist, String title, Integer year, String genre, byte[] albumImage, String albumImageMimeType) {
		super();
		this.artist = artist;
		this.title = title;
		this.year = year;
		this.genre = genre;
		this.albumImage = albumImage;
		this.albumImageMimeType = albumImageMimeType;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public byte[] getAlbumImage() {
		return albumImage;
	}

	public void setAlbumImage(byte[] albumImage) {
		this.albumImage = albumImage;
	}

	public String getAlbumImageMimeType() {
		return albumImageMimeType;
	}

	public void setAlbumImageMimeType(String albumImageMimeType) {
		this.albumImageMimeType = albumImageMimeType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Album other = (Album) obj;
		if (artist == null) {
			if (other.artist != null)
				return false;
		} else if (!artist.equals(other.artist))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artist == null) ? 0 : artist.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public int compareTo(Album o) {
		if (o == null)
			return -1;
		
		Comparator<String> comp = new SmartComparator<>();

		int compare = comp.compare(artist, o.getArtist());
		if (compare == 0)
			compare = comp.compare(title, o.getTitle());
		
		return compare;
	}

	@Override
	public String toString() {
		return "Album [artist=" + artist + ", title=" + title + ", year=" + year + ", genre=" + genre + "]";
	}
	
	
}
