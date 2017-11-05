package it.musichub.server.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
import it.musichub.server.library.model.Album;
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
		s1.setRating(5);
		s1.setGenre("Pop");
		s1.setLastModified(new Date().getTime());
		f.addSong(s1);
		
		Song s2 = new Song();
		s2.setArtist("Bryan Adams");
		s2.setTitle("Summer of '69");
		s2.setYear(1981);
		s2.setRating(3);
		s2.setGenre("Rock");
		s2.setLastModified(new Date().getTime()+10000);
		f.addSong(s2);
		
		Song s3 = new Song();
		s3.setArtist("Imagine Dragons");
		s3.setTitle("Whatever It Takes");
		s3.setAlbumTitle("Evolve");
		s3.setYear(2017);
		s3.setGenre("Nu Metal");
		s3.setLastModified(new Date().getTime()+20000);
		f.addSong(s3);
		
		Song s4 = new Song();
		s4.setArtist("ligabue");
		s4.setTitle("Quella che non sei");
		s4.setAlbumTitle("Buon compleanno Elvis");
		s4.setYear(1995);
		s4.setRating(5);
		s4.setGenre("Rock");
		s4.setLastModified(new Date().getTime()+30000);
		f.addSong(s4);
		

		//search: tests with clauses
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
		
		//search: most 2 recent songs
		Query qRecent = new Query();
		qRecent.addOrdering("lastModified", Order.desc);
		List<Song> resRecent = ss.search(qRecent, f, true, 0, 1);
		System.out.println("resRecent="+resRecent);
		System.out.println();
		
		//enum: all ratings
		List<Integer> ratings = ((SearchServiceImpl)ss).enumerateRatings(Order.asc, null, f, true);
		System.out.println("ratings="+ratings);
		System.out.println();
		
		//enum: not null ratings
		Clause f1 = new SimpleClause("rating", Operator.NOT_EQUALS, null);
		ratings = ((SearchServiceImpl)ss).enumerateRatings(Order.asc, f1, f, true);
		System.out.println("ratings="+ratings);
		System.out.println();
		
		//enum: ratings from 5 to 4
		CompoundClause f2 = new CompoundClause();
		f2.addClause(new SimpleClause(LogicalOperator.AND, "rating", Operator.GREATER_EQUALS, 4));
		f2.addClause(new SimpleClause(LogicalOperator.AND, "rating", Operator.LESS_EQUALS, 5));
		ratings = ((SearchServiceImpl)ss).enumerateRatings(Order.desc, f2, f, true);
		System.out.println("ratings="+ratings);
		System.out.println();
		
		//enum: all artists
		List<String> artists = ((SearchServiceImpl)ss).enumerateArtists(Order.asc, null, f, true);
		System.out.println("artists="+artists);
		System.out.println();
		
		//enum: all albums (except null ones)
		Clause albumClause = new SimpleClause("albumTitle", Operator.NOT_EQUALS, null);
		List<Album> albums = ((SearchServiceImpl)ss).enumerateAlbums(Order.asc, albumClause, f, true);
		System.out.println("albums="+albums);
		System.out.println();
		
		//enum: all years
		List<Integer> years = ((SearchServiceImpl)ss).enumerateYears(Order.asc, null, f, true);
		System.out.println("years="+years);
		System.out.println();
		
		//enum: all genres
		List<String> genres = ((SearchServiceImpl)ss).enumerateGenres(Order.asc, null, f, true);
		System.out.println("genres="+genres);
		System.out.println();
		
		
		ss.stop();
		ss.destroy();
	}
	
}
