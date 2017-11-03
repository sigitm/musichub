package it.musichub.server.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;
import it.musichub.server.library.utils.SmartBeanComparator.Order;
import it.musichub.server.runner.IMusicHubService;
import it.musichub.server.search.model.Query;

public interface SearchService extends IMusicHubService {

    public List<Song> search(Query query);
    public List<Song> search(Query query, Folder folder, boolean recurse);
    public List<Song> search(Query query, Folder folder, boolean recurse, int from, int to);
    
    public <T> List<T> enumerate(String expression, Class<T> expressionClass);
    public <T> List<T> enumerate(String expression, Class<T> expressionClass, Order orderType);
    public <T> List<T> enumerate(String expression, Class<T> expressionClass, Order orderType, Query query, Folder folder, boolean recurse);
    public <T> List<T> enumerate(String expression, Class<T> expressionClass, Comparator<T> expressionComparator, Query query, Folder folder, boolean recurse);

}
