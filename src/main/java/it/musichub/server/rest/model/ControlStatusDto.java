package it.musichub.server.rest.model;

public class ControlStatusDto {

	// / Player info
	private String state;
	private Integer volume;
	private Boolean mute;

	// / Track info
	private SongDto currentSong;
	private String duration;
	private String remainingDuration;
	private String position;
	private Integer elapsedPercent;
	private Long durationSeconds;
	private String title;
	private String artist;
	
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Integer getVolume() {
		return volume;
	}
	public void setVolume(Integer volume) {
		this.volume = volume;
	}
	public Boolean getMute() {
		return mute;
	}
	public void setMute(Boolean mute) {
		this.mute = mute;
	}
	public SongDto getCurrentSong() {
		return currentSong;
	}
	public void setCurrentSong(SongDto currentSong) {
		this.currentSong = currentSong;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getRemainingDuration() {
		return remainingDuration;
	}
	public void setRemainingDuration(String remainingDuration) {
		this.remainingDuration = remainingDuration;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public Integer getElapsedPercent() {
		return elapsedPercent;
	}
	public void setElapsedPercent(Integer elapsedPercent) {
		this.elapsedPercent = elapsedPercent;
	}
	public Long getDurationSeconds() {
		return durationSeconds;
	}
	public void setDurationSeconds(Long durationSeconds) {
		this.durationSeconds = durationSeconds;
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
	
	
}
