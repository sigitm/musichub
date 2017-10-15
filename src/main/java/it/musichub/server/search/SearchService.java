package it.musichub.server.search;

import java.util.List;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;
import it.musichub.server.runner.IMusicHubService;

public interface SearchService extends IMusicHubService {

	public Query createQuery(String query);
	
    public List<Song> execute(Query query);
    public List<Song> execute(Query query, Folder folder, boolean recurse);

}
