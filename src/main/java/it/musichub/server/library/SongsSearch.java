package it.musichub.server.library;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;

public class SongsSearch implements Serializable {

	/*
	 * EVOLUZIONI:
	 * - non si può restituire le song originali perchè contengono i rami. Clonare la Song? Creare un apposito dto?
	 * - metodi di ricerca più avanzati; vedere sotto
	 */
	
	private Folder folder;
	private String query;
	
	private JexlExpression e = null;
	
	private static final JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(false).create();
	
	public SongsSearch(Folder folder, String query) {
		super();
		this.folder = folder;
		this.query = query;
		
		init();
	}

	private void init(){
	    // Assuming we have a JexlEngine instance initialized in our class named 'jexl':
	    // Create an expression object for our calculation
		
		JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(false).create();
	    e = jexl.createExpression(query);
	}
	
	private boolean evaluate(Song song){
		// populate the context
	    JexlContext context = new MapContext();
	    context.set("song", song);

	    // work it out
	    boolean result = (boolean) e.evaluate(context);
	    return result;
	}
	
	
    public List<Song> execute(){
    	List<Song> searchResult = new ArrayList<Song>();
    	doVisit(folder, searchResult);
    	
    	
    	//TODO: test ordinamento
    	List<String> ordering = Arrays.asList("artist", "title"); //TODO: da prendere in input
    	
    	List<String> reverseOrdering = new ArrayList<>(ordering);
    	Collections.reverse(reverseOrdering); //TODO: va gestito anche l'asc/desc
    	for (String field : reverseOrdering)
    		Collections.sort(searchResult, new BeanComparator(field));
    	
    	//TODO clonare le songs!! altrimenti arrivano tutti i rami!
    	
    	
    	return searchResult;
    }
   
    private void doVisit(Folder folder, List<Song> searchResult){
		if (folder.getSongs() != null){
			for (Song song : folder.getSongs()) {
				if (evaluate(song))
					searchResult.add(song);
			}
		}
		if (folder.getFolders() != null){
			for (Folder child : folder.getFolders())
				doVisit(child, searchResult);
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
