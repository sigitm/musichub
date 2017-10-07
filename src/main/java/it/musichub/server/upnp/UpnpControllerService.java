package it.musichub.server.upnp;

import java.util.List;

import org.fourthline.cling.UpnpService;

import it.musichub.server.runner.IMusicHubService;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.ex.NoSelectedDeviceException;
import it.musichub.server.upnp.model.Device;
import it.musichub.server.upnp.model.DeviceRegistry;
import it.musichub.server.upnp.model.DeviceService;
import it.musichub.server.upnp.renderer.IRendererCommand;
import it.musichub.server.upnp.renderer.IRendererState;

public interface UpnpControllerService extends IMusicHubService {

	/*
	 * nuova proposta
	 * 
	 * metodi per ottenere la lista dei device, settare il selectedDevice, 
	 * (POTREI FARE UN'INTERFACCIA INTERNAL PER L'USO INTERNO....SOLO DA ALTRI SERVIZI)
	 */
	// INTERNAL METHODS
	public UpnpService getUpnpService();
	public DeviceRegistry getDeviceRegistry(); //TODO provvisorio???
	public IRendererState getRendererState(); //TODO provvisorio
	public IRendererCommand getRendererCommand(); //TODO provvisorio
	public MediaServer getMediaServer(); //TODO provvisorio???
	
	// HANDLE DEVICES
	public List<Device> getDevices();
	public Device getDevice(String udn) throws DeviceNotFoundException;
	public Device getDeviceByCustomName(String customName) throws DeviceNotFoundException;
	public List<Device> getOnlineDevices();
	public boolean isDeviceOnline(Device device); //TODO nascondere??
	public boolean isDeviceOnline(String udn) throws DeviceNotFoundException;
	public void setDeviceCustomName(String udn, String customName) throws DeviceNotFoundException;

	// HANDLE SELECTED DEVICE
	public boolean isDeviceSelected();
	public Device getSelectedDevice();
	public void setSelectedDevice(Device device); //TODO nascondere??
	public void setSelectedDevice(String udn) throws DeviceNotFoundException;
	public void clearSelectedDevice();
	public boolean isSelectedDeviceOnline() throws NoSelectedDeviceException;
	public DeviceService getSelectedDeviceService(String serviceType) throws NoSelectedDeviceException;
	
//	// PLAYLIST
//	List<Song> getSongs();
//	//add
//	boolean addSong(Song song);
//	void addSongs(List<Song> songs);
//	void addFolder(Folder folder, boolean recursive);
//	//remove
//	boolean removeSong(Song song);
//	void removeSongs(List<Song> songs);
//	void keepSongs(List<Song> songs);
//	void clearPlaylist();
//	
//	//iteration
//	boolean isEmpty();
//	boolean hasNext();
//	Song next();
//	boolean hasPrevious();
//	Song previous();
//	Song first();
//	Song last();
//	Song selectPos(int pos);
//	
//	
//	//TODO XXX XXXXXXXXXX DECIDERE SE TENERE QUI... sarebbero i comandi da interfaccia (con regole diverse!)
////	Song goToFirst();
////	Song goToPrevious();
////	Song goToNext();
////	Song goToLast();
//	
//	//options
//	public enum RepeatMode {OFF, TRACK, ALL}
//	boolean getShuffle();
//	void setShuffle(boolean shuffle);   //devo anche mescolare il songPointers? 
//	void shuffle();  //rimescolo!
//	RepeatMode getRepeat(); 
//	void setRepeat(RepeatMode repeat);
//	
//	//loadPlaylist(String name)
//	//savePlaylist(String name)
//	//exportPlaylist()  --esporta la playlist in un m3u8
//	//importPlaylist()  --importa la playlist da un m3u8
}
