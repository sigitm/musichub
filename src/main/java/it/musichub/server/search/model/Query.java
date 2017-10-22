package it.musichub.server.search.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import it.musichub.server.library.utils.SmartBeanComparator.Order;

public class Query implements Serializable {

	public Query() {
		super();
	}
	
	private CompoundClause filter = new CompoundClause();
	
	private Map<String, Order> ordering = null;
	
	private static final Map<String, Order> DEFAULT_ORDER = new LinkedHashMap<String, Order>(){
		{
			put("artist", Order.asc);
			put("title", Order.asc);
		}
	};
	
	public void addClause(Clause clause) {
		filter.addClause(clause);
	}
	
	public String getExpression() {
		return filter.getExpression();
	}
	
	public void addOrdering(String orderBy, Order orderType) {
		if (ordering == null)
			ordering = new LinkedHashMap<>();
		
		ordering.put(orderBy, orderType);
	}
	
	public Map<String, Order> getOrdering() {
		if (ordering == null)
			return DEFAULT_ORDER;
		
		return ordering;
	} 
}
