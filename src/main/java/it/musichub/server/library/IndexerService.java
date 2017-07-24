package it.musichub.server.library;

import it.musichub.server.library.model.Folder;
import it.musichub.server.runner.MusicHubService;

public interface IndexerService extends MusicHubService {

	public void refresh();
	public void refresh(String subFolderPath);
	public void refresh(String subFolderPath, boolean parseSubFolders);
	
	public Folder getStartingFolder();

}
