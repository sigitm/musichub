package it.musichub.server.rest.model;

import java.io.Serializable;

public class SongDto implements Serializable {

	private String id;
	private String folderName;
	private String filename;
	private Long size;
	private Long lastModified;
	private Long length;
	
	/*
	 * Song data
	 */
	private String title;
	private String artist;
	private String albumTitle;
	private Integer year;
	private String track;
	private String genre;
	private Integer rating;
	private String albumImage;
	private String albumImageMimeType;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbumTitle() {
		return albumTitle;
	}

	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getAlbumImage() {
		return albumImage;
	}

	public void setAlbumImage(String albumImage) {
		this.albumImage = albumImage;
	}

	public String getAlbumImageMimeType() {
		return albumImageMimeType;
	}

	public void setAlbumImageMimeType(String albumImageMimeType) {
		this.albumImageMimeType = albumImageMimeType;
	}
	
	
	
	public String getLengthHhMmSs() {
		if (length == null)
			return null;
		
		return String.format("%02d:%02d:%02d", length / 3600, (length % 3600) / 60, (length % 60));
	}
	
	public String getLengthMmSs() {
		if (length == null)
			return null;
		
		return String.format("%02d:%02d", (length % 3600) / 60, (length % 60));
	}
	
	public String getReadableLength() {
		if (length == null)
			return null;
		
		return (length / 3600 > 0) ? getLengthHhMmSs() : getLengthMmSs();
	}

	@Override
	public String toString() {
		return "SongDto [id=" + id + ", folderName=" + folderName + ", filename=" + filename + ", size=" + size
				+ ", lastModified=" + lastModified + ", length=" + length + ", title=" + title + ", artist=" + artist
				+ ", albumTitle=" + albumTitle + ", year=" + year + ", track=" + track + ", genre=" + genre
				+ ", rating=" + rating + "]";
	}
	
}
