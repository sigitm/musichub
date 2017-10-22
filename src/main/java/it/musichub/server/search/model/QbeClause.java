package it.musichub.server.search.model;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import it.musichub.server.library.model.Song;
import it.musichub.server.search.model.SimpleClause.Operator;

public class QbeClause extends CompoundClause {

	private final static Logger logger = Logger.getLogger(QbeClause.class);
	
	public QbeClause(Song qbe) {
		super();
		addClauses(qbe, true);
	}

	public QbeClause(LogicalOperator logicalOperator, Song qbe) {
		super(logicalOperator);
		addClauses(qbe, true);
	}
	
	private void addClauses(Song qbe, boolean useLike){
		try {
			BeanInfo bi = Introspector.getBeanInfo(qbe.getClass());
		    PropertyDescriptor[] pds = bi.getPropertyDescriptors();
		    
		    for (PropertyDescriptor pd : pds) {
		        // Get property name
		        String propName = pd.getName();

		        // Discard invalid properties
		        if ("class".equals(propName))
		        	continue;

		        Object value;
		    	try {
			        // Get value
			        value = pd.getReadMethod().invoke(qbe);
			    } catch (Exception e) {
					logger.warn("Cannot access qbe field "+propName+" from song "+qbe, e);
					continue;
				}
		    	
		        // Create clause
		        if (value != null){
		        	Operator operator = useLike && value instanceof String ? Operator.LIKE : Operator.EQUALS;
		        	addClause(new SimpleClause(LogicalOperator.AND, propName, operator, value));
		        }
		    }
		} catch (Exception e) {
			logger.error("Error creating qbe from song "+qbe, e);
		}
	}
}