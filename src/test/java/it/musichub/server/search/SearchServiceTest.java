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

public class SearchServiceTest {
	
	//TODO trasformare in junit!!!

	public static void main(String[] args) throws Exception {
		SearchService ss = new SearchServiceImpl();
		ss.init();
		ss.start();
		
		
		Folder f = new Folder();

		Song s1 = new Song();
		s1.setArtist("Ligabue");
		s1.setTitle("Certe notti");
		s1.setAlbumTitle("Buon compleanno Elvis");
		s1.setYear(1995);
		Song s2 = new Song();
		s2.setArtist("Bryan Adams");
		s2.setTitle("Summer of '69");
		s2.setYear(1981);
		s2.setRating(3);
		Song s3 = new Song();
		s3.setArtist("Imagine Dragons");
		s3.setTitle("Whatever It Takes");
		s3.setAlbumTitle("Evolve");
		s3.setYear(2017);
		s3.setRating(5);
		
		f.addSong(s1);
		f.addSong(s2);
		f.addSong(s3);
		

//		Clause c1 = new BasicClause("song.artist.toLowerCase() =~ '^Ligabue$'.toLowerCase()");
		Clause c1 = new SimpleClause(LogicalOperator.OR, "artist", Operator.EQUALS, "ligabue");
		Clause c2 = new SimpleClause(LogicalOperator.OR, "artist", Operator.EQUALS, "Bryan Adams");
		CompoundClause c3 = new CompoundClause(LogicalOperator.AND);
		c3.addClause(c1);
		c3.addClause(c2);
		CompoundClause c4 = new CompoundClause(LogicalOperator.OR);
		c4.addClause(c3);
		c4.addClause(new SimpleClause(LogicalOperator.AND, "year", Operator.GREATER, 1980));
		System.out.println("c4="+c4);
		System.out.println();
		
		Query q4 = new Query();
		q4.addClause(c4);
		System.out.println("q4="+q4);
		List<Song> res1 = ss.search(q4, f, true);
		System.out.println("res1="+res1);
		System.out.println();
		
		Query q5 = new Query();
		QbeClause c5 = new QbeClause(s2);
		System.out.println("c5qbe="+c5);
		q5.addClause(c5);
		System.out.println("q5="+q5);
		List<Song> res2 = ss.search(q5, f, true);
		System.out.println("res2="+res2);
		System.out.println();
		
		
		List<Integer> ratings = ((SearchServiceImpl)ss).enumerateRatings(Order.asc, new Query(), f, true);
		System.out.println("ratings="+ratings);
		System.out.println();
		
		
		
		ss.stop();
		ss.destroy();
	}
	
	
}
