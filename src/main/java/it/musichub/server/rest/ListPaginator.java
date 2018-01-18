package it.musichub.server.rest;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;

import com.google.gson.Gson;

import it.musichub.server.config.Constants;
import it.musichub.server.rest.model.utils.PaginatedList;
import it.musichub.server.rest.model.utils.PagingCoordinates;
import it.musichub.server.rest.model.utils.PagingLinks;
import it.musichub.server.rest.model.utils.PagingSubList;
 
public class ListPaginator {
 
    public static <T> PaginatedList<T> paginateList(List<T> list, String url, Integer limit, Integer start){
    	if (list == null)
    		return null;
    	
    	if (limit == null || limit <= 0)
    		limit = Constants.DEFAULT_JSON_PAGINATION_LIMIT;
    	
    	if (start == null || start < 0)
    		start = 0;
    	
    	PagingSubList<T> subList = getSubList(list, limit, start);
    	
    	int total = list.size();
    	List<T> results = subList.subList;
    	
    	//creazione dei link
    	String self = null;
    	try {
			URIBuilder ub = new URIBuilder(url);
			ub.setParameter("limit", String.valueOf(limit));
			ub.setParameter("start", String.valueOf(start));
			self = ub.build().toASCIIString();
		} catch (URISyntaxException e){
			//Nothing to do
		}
    	String prev = null;
		if (subList.prev != null){
			try {
				URIBuilder ub = new URIBuilder(url);
				ub.setParameter("limit", String.valueOf(subList.prev.limit));
				ub.setParameter("start", String.valueOf(subList.prev.start));
				prev = ub.build().toASCIIString();
			} catch (URISyntaxException e){
				//Nothing to do
			}
		}
    	String next = null;
		if (subList.next != null){
			try {
				URIBuilder ub = new URIBuilder(url);
				ub.setParameter("limit", String.valueOf(subList.next.limit));
				ub.setParameter("start", String.valueOf(subList.next.start));
				next = ub.build().toASCIIString();
			} catch (URISyntaxException e){
				//Nothing to do
			}
		}
    	PagingLinks links = new PagingLinks(self, prev, next);
    	
    	PaginatedList<T> paginatedJson = new PaginatedList<>(total, results, links);
    	return paginatedJson;
    }
    
    private static <T> PagingSubList<T> getSubList(List<T> list, int limit, int start){
    	//assumption: limit is not null and >0
    	//assumption: start is not null and >=0
    	int size = list.size();
    		
    	/**
    	 * APPUNTI (non aggiornati)...
    	 * 
    	 * 
    	 * CASO start after (quindi start >= size)
    	 * 
    	 * - lista vuota
    	 * - prev: start=size-limit>=0? size-limit : 0), limit=size-limit>0?limit:size
    	 * - next null
    	 * 
    	 * CASO start into ma start+limit sfora o arriva a size (quindi start+limit >= size)
    	 * 
    	 * - lista: (start, size!!) invece che start+limit
    	 * - prev: start=start-limit>=0? start-limit : 0), limit=start-limit>0?limit:start
    	 * - next: null
    	 * 
    	 * CASO start into e start+limit < size
    	 * 
    	 * - lista: (start, start+limit)
    	 * - prev: start=start-limit>=0? start-limit : 0), limit=start-limit>0?limit:start
    	 * - next: start=start+limit, limit=start+limit+limit<=size?proprio quello : size
    	 * 
    	 * CASO start=0 ...
    	 * 
    	 * - prev: null
    	 * fromIndex = 
    	 */
    	
    	List<T> subList = null;
    	if (start < size){
    		int fromIndex = start;
    		int toIndex = start+limit <= size ? start+limit : size;
    		subList = list.subList(fromIndex, toIndex);
    	}
    	
    	PagingCoordinates prev = null;
    	if (start > 0 && start < size){
    		int newStart = start-limit >= 0 ? (start-limit >= size ? size-limit : start-limit) : 0;
    		int newLimit = start-limit > 0 ? limit : start;
    		prev = new PagingCoordinates(newStart, newLimit);
    	}
    	
    	PagingCoordinates next = null;
    	if (start+limit < size){
    		int newStart = start+limit;
    		int newLimit = limit <= size ? limit : size;
    		next = new PagingCoordinates(newStart, newLimit);
    	}
    	
    	return new PagingSubList<>(subList, prev, next);
    }
    
//    public static void main(String[] args) {
//		//test
//    	List<Integer> l = new ArrayList<>();
//    	l.add(0);
//    	l.add(1);
//    	l.add(2);
//    	l.add(3);
//    	l.add(4);
//    	l.add(5);
//    	l.add(6);
//    	l.add(7);
//    	l.add(8);
//    	l.add(9);
//    	
//    	PagingSubList<Integer> psl = getSubList(l, /*limit*/3, /*start*/1);
//    	System.out.println("psl="+psl);
//	}
    

    


 
}