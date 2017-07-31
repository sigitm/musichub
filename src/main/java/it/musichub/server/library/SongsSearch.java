package it.musichub.server.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;
import it.musichub.server.library.utils.SmartBeanComparator;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;

public class SongsSearch implements SearchService {

	/*
	 * EVOLUZIONI:
	 * - possibilità di cercare in una subfolder (usare getFolderRecursive?)
	 * - possibilità di decidere se includere le subFolder o solo la folder corrente
	 * - non si può restituire le song originali perchè contengono i rami. Clonare la Song? Creare un apposito dto?
	 * - metodi di ricerca più avanzati; vedere sotto
	 */

	private JexlEngine jexl = null;
	
	private IndexerService getIndexerService(){
		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
	}
	
	public SongsSearch() {
		super();
	}

	@Override
	public void init(){
	    // Assuming we have a JexlEngine instance initialized in our class named 'jexl':
	    // Create an expression object for our calculation
		
		jexl = new JexlBuilder().cache(512).strict(true).silent(true).create();
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public Query createQuery(String query){
		JexlExpression e = jexl.createExpression(query);
		return new Query(e);
	}
	
	private static boolean evaluate(Song song, Query query){
		// populate the context
	    JexlContext context = new MapContext();
	    context.set("song", song);

	    // work it out
	    boolean result = (boolean) query.getExpression().evaluate(context);
	    return result;
	}
	
	@Override
    public List<Song> execute(Query query){
    	//TODO xxxxxxxxxxx PROVVISORIO
    	Folder folder = getIndexerService().getStartingFolder();
    	//TODO xxxxxxxxxxx PROVVISORIO
    	
    	List<Song> searchResult = new ArrayList<Song>();
    	doVisit(folder, query, searchResult);
    	
    	
    	//TODO: test ordinamento
    	List<String> ordering = Arrays.asList("artist", "title"); //TODO: da prendere in input
    	
    	List<String> reverseOrdering = new ArrayList<>(ordering);
    	Collections.reverse(reverseOrdering); //TODO: va gestito anche l'asc/desc
    	for (String field : reverseOrdering)
    		Collections.sort(searchResult, new SmartBeanComparator(field));
    	
    	//TODO clonare le songs!! altrimenti arrivano tutti i rami!
    	
    	
    	return searchResult;
    }
   
    private void doVisit(Folder folder, Query query, List<Song> searchResult){
		if (folder.getSongs() != null){
			for (Song song : folder.getSongs()) {
				if (evaluate(song, query))
					searchResult.add(song);
			}
		}
		if (folder.getFolders() != null){
			for (Folder child : folder.getFolders())
				doVisit(child, query, searchResult);
		}
    }
    
	/*
	 * oggetto ricerca campo:
	 * isLikeSearch (con wildcard)
	 * isCaseSensitive (per i campi stringa)
	 * 
	 */	
	
	/*
	 * pattern visitor??
	 * https://dzone.com/articles/design-patterns-visitor
	 * 
	 * jexl
	 * 
	 * 
	 * proposte di ricerca:
	 * - qbe
	 * SongSearch s = new SongSearch();
	 * s.setTitle(new StringFieldSearch("Bryan*", true, false));
	 * query.addQbeFilter(s); //considera come condizioni i campi not null
	 * query.execute(folder);
	 * 
	 * 
	 * - per i "minore di"? operatori unari
	 * - e per gli intervalli?? "between" --> beh per questo bastano due condizioni in and
	 * - ha senso includere anche "equals"? sì e anche like
	 * 
	 * query.addFilter(new SimpleFindClause(FindClause.AND, "title", FindClause.LIKE, "Bryan*"));
	 * query.addFilter(new SimpleFindClause(FindClause.AND, "rating", FindClause.LESS_THAN, 2));
	 * 
	 * 
	 * 
	 * 
	 * - e per fare le condizioni complesse? es. OR, parentesi...
	 */
	
	
	
}
