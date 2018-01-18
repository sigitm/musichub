package it.musichub.server.search;

import java.util.Comparator;
import java.util.List;

import it.musichub.server.library.model.Album;
import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;
import it.musichub.server.library.utils.SmartBeanComparator.Order;
import it.musichub.server.runner.IMusicHubService;
import it.musichub.server.search.model.Clause;
import it.musichub.server.search.model.Query;

public interface SearchService extends IMusicHubService {

	//search
    public List<Song> search(Query query);
    public List<Song> search(Query query, Folder folder, boolean recurse);
    public List<Song> search(Query query, Folder folder, boolean recurse, int from, int to);
    
    //enumerate
    public <T> List<T> enumerate(String expression, Class<T> expressionClass);
    public <T> List<T> enumerate(String expression, Class<T> expressionClass, Order orderType);
    public <T> List<T> enumerate(String expression, Class<T> expressionClass, Order orderType, Clause filter, Folder folder, boolean recurse);
    public <T> List<T> enumerate(String expression, Class<T> expressionClass, Comparator<T> expressionComparator, Clause filter, Folder folder, boolean recurse);

    //enumerate examples
    public List<String> enumerateCaseInsensitiveString(String expression, Order orderType, Clause filter, Folder folder, boolean recurse);
    public List<String> enumerateArtists(Order orderType, Clause filter, Folder folder, boolean recurse);
    public List<Album> enumerateAlbums(Order orderType, Clause filter, Folder folder, boolean recurse);
    public List<String> enumerateGenres(Order orderType, Clause filter, Folder folder, boolean recurse);
	public List<Integer> enumerateRatings(Order orderType, Clause filter, Folder folder, boolean recurse);
	public List<Integer> enumerateYears(Order orderType, Clause filter, Folder folder, boolean recurse);
	
}
