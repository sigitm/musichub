package it.musichub.server.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import it.musichub.server.library.utils.SmartComparator;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.search.model.Query;

public class SearchServiceImpl extends MusicHubServiceImpl implements SearchService {

	/*
	 * EVOLUZIONI:
	 * V possibilità di cercare in una subfolder (usare getFolderRecursive?)
	 * V possibilità di decidere se includere le subFolder o solo la folder corrente
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
	
	private static <T> T evaluate(Song song, JexlExpression expr, Class<T> exprClass){
		// populate the context
	    JexlContext context = new MapContext();
	    context.set("song", song);

	    // work it out
	    T result = (T) expr.evaluate(context);
	    return result;
	}
	
	private boolean evaluateBoolean(Song song, JexlExpression expr){
	    return evaluate(song, expr, boolean.class);
	}
	
	@Override
    public List<Song> search(Query query){
		Folder folder = getIndexerService().getStartingFolder();
    	return search(query, folder, true);
    }
	
	@Override
    public List<Song> search(Query query, Folder folder, boolean recurse){
		return search(query, folder, recurse, -1, -1);
	}
	
	@Override
    public List<Song> search(Query query, Folder folder, boolean recurse, int from, int to){
		
		// create expression
		JexlExpression expr = jexl.createExpression(query.getExpression());
    	
		// visit tree
    	List<Song> searchResult = new ArrayList<Song>();
    	doSearchVisit(folder, recurse, expr, searchResult);
    	
    	// ordering
    	Map<String, Order> ordering = query.getOrdering();
    	List<String> reverseOrdering = new ArrayList<>(ordering.keySet());
    	Collections.reverse(reverseOrdering);
    	for (String field : reverseOrdering){
    		Order orderType = ordering.get(field);
    		Collections.sort(searchResult, new SmartBeanComparator(field, orderType));
    	}
    	
    	//pagination
    	List<Song> result = null;
    	if (from >= 0 && to >= 0){
    		result = searchResult.subList(from, to+1); //TODO XXXXX gestire i casi di indexoutofbound? //TODO XXX usare la PaginatedList?
    	}else{
    		result = searchResult;
    	}
    	
    	//TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXX clonare le songs!! altrimenti arrivano tutti i rami!
    	
    	
    	return result;
    }
   
    private void doSearchVisit(Folder folder, boolean recurse, JexlExpression expr, List<Song> searchResult){
		if (folder.getSongs() != null){
			for (Song song : folder.getSongs()) {
				if (evaluateBoolean(song, expr))
					searchResult.add(song);
			}
		}
		if (recurse && folder.getFolders() != null){
			for (Folder child : folder.getFolders())
				doSearchVisit(child, recurse, expr, searchResult);
		}
    }
    
    private <T> void doEnumerateVisit(JexlExpression enumExpr, Class<T> enumExprClass, Folder folder, boolean recurse, JexlExpression expr, List<T> enumResult){
		if (folder.getSongs() != null){
			for (Song song : folder.getSongs()) {
				if (evaluateBoolean(song, expr)){
					T result = evaluate(song, enumExpr, enumExprClass);
					enumResult.add(result);
				}
					
			}
		}
		if (recurse && folder.getFolders() != null){
			for (Folder child : folder.getFolders())
				doEnumerateVisit(enumExpr, enumExprClass, child, recurse, expr, enumResult);
		}
    }
    
    @Override
    public <T> List<T> enumerate(String expression, Class<T> expressionClass){
    	return enumerate(expression, expressionClass, Order.asc);
    }
    
    @Override
    public <T> List<T> enumerate(String expression, Class<T> expressionClass, Order orderType){
    	Folder folder = getIndexerService().getStartingFolder();
    	return enumerate(expression, expressionClass, orderType, new Query(), folder, true);
    }
    
    @Override
    public <T> List<T> enumerate(String expression, Class<T> expressionClass, Order orderType, Query query, Folder folder, boolean recurse){
    	Comparator<T> comparator = new SmartComparator<T>(orderType != null ? orderType : Order.asc);
    	List<T> enumResult = enumerate(expression, expressionClass, comparator, query, folder, recurse);
    	return enumResult;
    }
    
    @Override
    public <T> List<T> enumerate(String expression, Class<T> expressionClass, Comparator<T> expressionComparator, Query query, Folder folder, boolean recurse){
    	
    	//create enumerate expression
    	JexlExpression enumExpr = jexl.createExpression("song."+expression);
	 
		// create expression
		JexlExpression expr = jexl.createExpression(query.getExpression());
    	
		// visit tree
    	List<T> enumResult = new ArrayList<T>();
    	doEnumerateVisit(enumExpr, expressionClass, folder, recurse, expr, enumResult);
    	
    	// ordering
   		Collections.sort(enumResult, expressionComparator);
    	
    	return enumResult;
    }
    
//    public static void main(String[] args) {
//    	JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(true).create();
//    	JexlExpression enumExpr = jexl.createExpression("song.rating");
//    	
//    	// populate the context EVALUATE
//    	Song x = new Song();
//    	x.setArtist("pippo");
//    	x.setTitle("titolone");
//    	x.setAlbum("album");
//    	x.setRating(3);
//    	
//	    JexlContext context = new MapContext();
//	    context.set("song", x);
//
//	    // work it out
//	    Object result = (Object) enumExpr.evaluate(context);
//	    System.out.println("result="+result+", class="+(result != null ? result.getClass() : "null"));
//	}
    
    public List<Integer> enumerateRatings(Order orderType){
    	Folder folder = getIndexerService().getStartingFolder();
    	return enumerateRatings(orderType, new Query(), folder, true);
    }
    
    public List<Integer> enumerateRatings(Order orderType, Query query, Folder folder, boolean recurse){
    	return enumerate("rating", Integer.class, orderType, query, folder, recurse);
    }
    
    
    
    
	
}
