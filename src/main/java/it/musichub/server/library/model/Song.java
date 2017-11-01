package it.musichub.server.library.model;

import java.io.File;
import java.io.Serializable;

public class Song implements Serializable {

	/*
	 * File data
	 */
	private File file;
	private String path;
	private String relativePath;
	private Folder folder;
	private String filename;
	private Long size;
	private Long lastModified;
	private Long length;
	private Integer bitrate;
	private Boolean vbr;
	
	/*
	 * Song data
	 */
	private Boolean id3v1;
	private Boolean id3v2;
	private String title;
	private String artist;
	private String albumTitle;
	private Integer year;
	private String track;
	private String genre;
	private Integer rating;
	private byte[] albumImage;
	private String albumImageMimeType;
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public Folder getFolder() {
		return folder;
	}
	public void setFolder(Folder folder) {
		this.folder = folder;
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
	public Integer getBitrate() {
		return bitrate;
	}
	public void setBitrate(Integer bitrate) {
		this.bitrate = bitrate;
	}
	public Boolean getVbr() {
		return vbr;
	}
	public void setVbr(Boolean vbr) {
		this.vbr = vbr;
	}
	public Boolean getId3v1() {
		return id3v1;
	}
	public void setId3v1(Boolean id3v1) {
		this.id3v1 = id3v1;
	}
	public Boolean getId3v2() {
		return id3v2;
	}
	public void setId3v2(Boolean id3v2) {
		this.id3v2 = id3v2;
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
	
	public Album getAlbum(){
		return new Album(artist, albumTitle, year, genre, albumImage, albumImageMimeType);
	}
	
	public String getId(){
		return path != null ? Integer.toString(path.hashCode()) : null; 
	}

	@Override
	public String toString() {
		return "Song [" + artist + " - "+ title + " (" + relativePath + ")]";
	}
	
}
