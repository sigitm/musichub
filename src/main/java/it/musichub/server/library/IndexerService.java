package it.musichub.server.library;

import it.musichub.server.library.model.Folder;
import it.musichub.server.runner.IMusicHubService;

public interface IndexerService extends IMusicHubService {

	public void refresh();
	public void refresh(String subFolderPath);
	public void refresh(String subFolderPath, boolean parseSubFolders);
	
	public Folder getStartingFolder();

}
