package it.musichub.server.library;

import java.util.List;

import it.musichub.server.library.model.Song;
import it.musichub.server.runner.MusicHubService;

public interface SearchService extends MusicHubService {

	public Query createQuery(String query);
	
    public List<Song> execute(Query query);

}
