package it.musichub.server.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.log4j.Logger;

import it.musichub.server.ex.ServiceDestroyException;
import it.musichub.server.ex.ServiceInitException;
import it.musichub.server.ex.ServiceStartException;
import it.musichub.server.ex.ServiceStopException;
import it.musichub.server.library.IndexerService;
import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;
import it.musichub.server.library.utils.SmartBeanComparator;
import it.musichub.server.library.utils.SmartBeanComparator.Order;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.search.model.Clause;
import it.musichub.server.search.model.Clause.LogicalOperator;
import it.musichub.server.search.model.CompoundClause;
import it.musichub.server.search.model.QbeClause;
import it.musichub.server.search.model.Query;
import it.musichub.server.search.model.SimpleClause;
import it.musichub.server.search.model.SimpleClause.Operator;

public class SearchServiceImpl extends MusicHubServiceImpl implements SearchService {

	/*
	 * EVOLUZIONI:
	 * - possibilità di cercare in una subfolder (usare getFolderRecursive?)
	 * - possibilità di decidere se includere le subFolder o solo la folder corrente
	 * - non si può restituire le song originali perchè contengono i rami. Clonare la Song? Creare un apposito dto?
	 * - metodi di ricerca più avanzati; vedere sotto
	 */

	private JexlEngine jexl = null;
	
	private final static Logger logger = Logger.getLogger(SearchServiceImpl.class);
	
	private IndexerService getIndexerService(){
		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
	}
	
	public SearchServiceImpl() {
		super();
	}

	@Override
	public void init() throws ServiceInitException {
	    // Assuming we have a JexlEngine instance initialized in our class named 'jexl':
	    // Create an expression object for our calculation
		
		jexl = new JexlBuilder().cache(512).strict(true).silent(true).create();
	}
	
	@Override
	public void start() throws ServiceStartException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void stop() throws ServiceStopException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void destroy() throws ServiceDestroyException {
		jexl = null;
	}
	
//	@Override
//	public QueryOLD createQuery(String query){
//		JexlExpression e = jexl.createExpression(query);
//		return new QueryOLD(e);
//	}
//	
	private boolean evaluate(Song song, JexlExpression expr){
		// populate the context
	    JexlContext context = new MapContext();
	    context.set("song", song);

	    // work it out
	    boolean result = (boolean) expr.evaluate(context);
	    return result;
	}
	
	@Override
    public List<Song> execute(Query query){
		Folder folder = getIndexerService().getStartingFolder();
    	return execute(query, folder, true);
    }
	
	@Override
    public List<Song> execute(Query query, Folder folder, boolean recurse){
		//TODO XXXXXXXXXXXXX l'ordering inglobarlo nella query??
		
		// create expression
		JexlExpression expr = jexl.createExpression(query.getExpression());
    	
		// visit tree
    	List<Song> searchResult = new ArrayList<Song>();
    	doVisit(folder, recurse, expr, searchResult);
    	
    	// ordering
    	Map<String, Order> ordering = query.getOrdering();
    	List<String> reverseOrdering = new ArrayList<>(ordering.keySet());
    	Collections.reverse(reverseOrdering); //TODO: va gestito anche l'asc/desc
    	for (String field : reverseOrdering){
    		Order orderType = ordering.get(field);
    		Collections.sort(searchResult, new SmartBeanComparator(field, orderType));
    	}
    	
    	//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXX clonare le songs!! altrimenti arrivano tutti i rami!
    	
    	
    	return searchResult;
    }
   
    private void doVisit(Folder folder, boolean recurse, JexlExpression expr, List<Song> searchResult){
		if (folder.getSongs() != null){
			for (Song song : folder.getSongs()) {
				if (evaluate(song, expr))
					searchResult.add(song);
			}
		}
		if (recurse && folder.getFolders() != null){
			for (Folder child : folder.getFolders())
				doVisit(child, recurse, expr, searchResult);
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
	

    

    

    

	

	

	
	public static void main(String[] args) {
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXX testare le clause.. dopo diventerà un junit!
		
		Folder f = new Folder();

		Song s1 = new Song();
		s1.setArtist("Ligabue");
		s1.setTitle("Certe notti");
		s1.setYear(1995);
		Song s2 = new Song();
		s2.setArtist("Bryan Adams");
		s2.setTitle("Summer of '69");
		s2.setYear(1981);
		
		f.addSong(s1);
		f.addSong(s2);
		

//		Clause c1 = new BasicClause("song.artist.toLowerCase() =~ '^Ligabue$'.toLowerCase()");
		Clause c1 = new SimpleClause(LogicalOperator.OR, "artist", Operator.EQUALS, "ligabue");
		Clause c2 = new SimpleClause(LogicalOperator.OR, "artist", Operator.EQUALS, "Bryan Adams");
		CompoundClause c3 = new CompoundClause(LogicalOperator.AND);
		c3.addClause(c1);
		c3.addClause(c2);
		CompoundClause c4 = new CompoundClause(LogicalOperator.OR);
		c4.addClause(c3);
		c4.addClause(new SimpleClause(LogicalOperator.AND, "year", Operator.GREATER, 1980));
		System.out.println(c4);
		System.out.println();
		
		Query query = new Query();
//		query.TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
//		FINIRE L'ESEMPIO CON LA QUERY NUOVA
		
		
//		JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(true).create();
//		JexlExpression e = jexl.createExpression(c4.getExpression());
//		System.out.println("s1="+evaluate(s1, new QueryOLD(e)));
//		System.out.println("s2="+evaluate(s2, new QueryOLD(e)));
//		System.out.println();
//		
//		QbeClause c5 = new QbeClause(s2);
//		System.out.println(c5);
//		System.out.println();
//		e = jexl.createExpression(c5.getExpression());
//		System.out.println("s1="+evaluate(s1, new QueryOLD(e)));
//		System.out.println("s2="+evaluate(s2, new QueryOLD(e)));
		
		
	}
	
	
}
