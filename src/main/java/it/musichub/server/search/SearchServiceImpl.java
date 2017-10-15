package it.musichub.server.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import it.musichub.server.ex.ServiceDestroyException;
import it.musichub.server.ex.ServiceInitException;
import it.musichub.server.ex.ServiceStartException;
import it.musichub.server.ex.ServiceStopException;
import it.musichub.server.library.IndexerService;
import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;
import it.musichub.server.library.utils.SmartBeanComparator;
import it.musichub.server.runner.MusicHubServiceImpl;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.search.SearchServiceImpl.Clause.LogicalOperator;
import it.musichub.server.search.SearchServiceImpl.SimpleClause.Operator;

public class SearchServiceImpl extends MusicHubServiceImpl implements SearchService {

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
		Folder folder = getIndexerService().getStartingFolder();
    	return execute(query, folder, true);
    }
	
	@Override
    public List<Song> execute(Query query, Folder folder, boolean recurse){
    	
    	List<Song> searchResult = new ArrayList<Song>();
    	doVisit(folder, recurse, query, searchResult);
    	
    	
    	//TODO: test ordinamento
    	List<String> ordering = Arrays.asList("artist", "title"); //TODO: da prendere in input
    	
    	List<String> reverseOrdering = new ArrayList<>(ordering);
    	Collections.reverse(reverseOrdering); //TODO: va gestito anche l'asc/desc
    	for (String field : reverseOrdering)
    		Collections.sort(searchResult, new SmartBeanComparator(field));
    	
    	//TODO clonare le songs!! altrimenti arrivano tutti i rami!
    	
    	
    	return searchResult;
    }
   
    private void doVisit(Folder folder, boolean recurse, Query query, List<Song> searchResult){
		if (folder.getSongs() != null){
			for (Song song : folder.getSongs()) {
				if (evaluate(song, query))
					searchResult.add(song);
			}
		}
		if (recurse && folder.getFolders() != null){
			for (Folder child : folder.getFolders())
				doVisit(child, recurse, query, searchResult);
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
	
    public static interface Clause {
    	
    	public static enum LogicalOperator{
    		AND {
				@Override
				public String getExpression() {
					return "&&";
				}
			},

    		OR {
				@Override
				public String getExpression() {
					return "||";
				}
			};
    	
    		public abstract String getExpression();
    	};
    	
		public LogicalOperator getLogicalOperator();
		public String getExpression();
    }
    
    public static abstract class AbstractClause implements Clause {
    	private LogicalOperator logicalOperator = LogicalOperator.AND;
    	
		public AbstractClause() {
			super();
		}

		public AbstractClause(LogicalOperator logicalOperator) {
			this();
			this.logicalOperator = logicalOperator;
		}

		public LogicalOperator getLogicalOperator() {
			return logicalOperator;
		}

		public void setLogicalOperator(LogicalOperator logicalOperator) {
			this.logicalOperator = logicalOperator;
		}
		
		public abstract String getExpression();

		@Override
		public String toString() {
			return this.getClass().getSimpleName()+" [logicalOperator=" + getLogicalOperator() + ", expression="+ getExpression() + "]";
		}
    }
    
    public static class BasicClause extends AbstractClause {
    	private String expression;

		public BasicClause(String expression) {
			super();
			this.expression = expression;
		}

		public BasicClause(LogicalOperator logicalOperator, String expression) {
			super(logicalOperator);
			this.expression = expression;
		}
		
		@Override
		public String getExpression() {
			return expression;
		}
    }
    
	public static class SimpleClause extends AbstractClause {
    	
		public static enum Operator{
    		EQUALS {
				@Override
				public String getExpression() {
					return "==";
				}
			},

    		NOT_EQUALS {
				@Override
				public String getExpression() {
					return "!=";
				}
			},
    		
    		LIKE {
				@Override
				public String getExpression() {
					return "=~";
				}
			},
			
			NOT_LIKE {
				@Override
				public String getExpression() {
					return "!~";
				}
			},
			
			LESS {
				@Override
				public String getExpression() {
					return "<";
				}
			},
			
			LESS_EQUALS {
				@Override
				public String getExpression() {
					return "<=";
				}
			},
			
			GREATER {
				@Override
				public String getExpression() {
					return ">";
				}
			},
			
			GREATER_EQUALS {
				@Override
				public String getExpression() {
					return ">=";
				}
			};
    	
    		public abstract String getExpression();
    	};
		
		private String property;
		private Operator operator;
		private Object value;
//		private boolean caseSensitive = false; //TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		
		public SimpleClause(String property, Operator operator, Object value) {
			super();
			this.property = property;
			this.operator = operator;
			this.value = value;
		}
		
		public SimpleClause(LogicalOperator logicalOperator, String property, Operator operator, Object value) {
			super(logicalOperator);
			this.property = property;
			this.operator = operator;
			this.value = value;
		}
		
		public String getProperty() {
			return property;
		}

		public void setProperty(String property) {
			this.property = property;
		}

		public Operator getOperator() {
			return operator;
		}

		public void setOperator(Operator operator) {
			this.operator = operator;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
		
		@Override
		public String getExpression() {
			String propertyStr = "song."+property;
			String valueStr = value != null ? value.toString() : null;
			if (operator == Operator.LIKE || operator == Operator.NOT_LIKE)
				valueStr = wildcardToRegex(valueStr);
			///TODO ESCAPE DEGLI APOSTROFI!!!
//			if (!caseSensitive && (operator=="EQUALS" || operator=="NOT_EQUALS")){  //TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
//				propertyStr
//			}
			
			if (/*!caseSensitive &&*/ value instanceof String && (operator == Operator.EQUALS || operator == Operator.NOT_EQUALS || operator == Operator.LIKE || operator == Operator.NOT_LIKE)){
				propertyStr += ".toLowerCase()";
				valueStr += ".toLowerCase()"; //TODO XXXXXXXXX CASO NULL
			}
			
			return propertyStr+" "+operator.getExpression()+" '"+valueStr+"'";
		}
		
		private static String wildcardToRegex(String wildcardString) {
			if (wildcardString == null)
				return null;
			
		    // The 12 is arbitrary, you may adjust it to fit your needs depending
		    // on how many special characters you expect in a single pattern.
		    StringBuilder sb = new StringBuilder(wildcardString.length() + 12);
		    sb.append('^');
		    for (int i = 0; i < wildcardString.length(); ++i) {
		        char c = wildcardString.charAt(i);
		        if (c == '*') {
		            sb.append("\\w*");
		        } else if (c == '?') {
		            sb.append("\\w");
		        } else if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
		            sb.append('\\');
		            sb.append(c);
		        } else {
		            sb.append(c);
		        }
		    }
		    sb.append('$');
		    return sb.toString();
		}
	}
	
	public static class CompoundClause extends AbstractClause {
		private List<Clause> clauses = new ArrayList<>();
		
		public CompoundClause() {
			super();
		}

		public CompoundClause(LogicalOperator logicalOperator) {
			super(logicalOperator);
		}

		public void addClause(Clause clause) {
			clauses.add(clause);
		}

		@Override
		public String getExpression() {
			StringBuilder sb = new StringBuilder();
			boolean firstClause = true; //TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX importare le StringUtils.concat
			for (Clause clause : clauses){
				if (!firstClause){
					sb.append(" ");
					sb.append(clause.getLogicalOperator().getExpression());
					sb.append(" ");
				}
				sb.append(" ( ");
				sb.append(clause.getExpression());
				sb.append(" ) ");
				firstClause = false;
			}
			return sb.toString();
		}
	}
	
	public static class QbeClause extends CompoundClause {

		public QbeClause(Object qbe) {
			super();
			addClauses(qbe);
		}

		public QbeClause(LogicalOperator logicalOperator, Object qbe) {
			super(logicalOperator);
			addClauses(qbe);
		}
		
		private void addClauses(Object qbe){
			//TODO XXXXXXXXXXXXXX con la reflection prendo tutti i campi not null e li metto in AND (e l'operazione?? like??)
			//for..............
//			Clause x = new SimpleClause("artist", Operator.LIKE, "Liga*");
//			addClause(x);
		}
		
		
	}
	
	public static void main(String[] args) {
		//TODO XXXXXXXXXXXXXXXXXXXXXXXXX testare le clause.. dopo diventerà un junit!
		
		
		Song s1 = new Song();
		s1.setArtist("Ligabue");
		s1.setTitle("Certe notti");
		s1.setYear(1995);
		Song s2 = new Song();
		s2.setArtist("Bryan Adams");
		s2.setTitle("Summer of '69");
		s2.setYear(1981);
		
		
		//TODO XXXXXXXX sistemare gli operator e le operator con un enum
//		Clause c1 = new BasicClause("song.artist.toLowerCase() =~ '^Ligabue$'.toLowerCase()");
		Clause c1 = new SimpleClause(LogicalOperator.OR, "artist", Operator.LIKE, "Ligabue");
		Clause c2 = new SimpleClause(LogicalOperator.OR, "artist", Operator.EQUALS, "Bryan Adams");
		CompoundClause c3 = new CompoundClause(LogicalOperator.AND);
		c3.addClause(c1);
		c3.addClause(c2);
		CompoundClause c4 = new CompoundClause(LogicalOperator.OR);
		c4.addClause(c3);
		c4.addClause(new SimpleClause(LogicalOperator.AND, "year", Operator.GREATER, 1980));
		System.out.println(c4);
		System.out.println();
		
		JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(true).create();
		JexlExpression e = jexl.createExpression(c4.getExpression());
		System.out.println("s1="+evaluate(s1, new Query(e)));
		System.out.println("s2="+evaluate(s2, new Query(e)));
	}
	
	
}
