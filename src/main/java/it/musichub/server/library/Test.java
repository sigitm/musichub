package it.musichub.server.library;

import java.util.List;
import java.util.concurrent.TimeUnit;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;

public class Test {

	public static void main(String[] args) {
		String startingDirStr = "N:\\incoming\\##mp3 new";
		String startingDirStr2 = "N:\\incoming\\##mp3 new\\Zucchero TODO\\Zucchero - Greatest Hits (1996)NLT-Release";
		
		SongsIndexer si = new SongsIndexer(startingDirStr);
		
		si.init();
		si.start();
		
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("****** PARTE 2: refresh totale ******");
		si.refresh();

		
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("****** PARTE 3: refresh parziale ******");
		si.refresh(startingDirStr2);
		
		
//		try {
//		TimeUnit.SECONDS.sleep(3);
//	} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	System.out.println();
//	System.out.println();
//	System.out.println();
//	System.out.println("****** PARTE 4: refresh parziale 2 ******");
//	si.refresh(startingDirStr, false);
		
		
		
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("****** PARTE 4: search ******");
		
		String query = "song.artist == 'Ligabue'";
		List<Song> results = si.search(query);
		System.out.println("search results:");
		for (Song song : results)
			System.out.println(song);
		
		

		
		
		
		
		
		si.stop();
		
		
		Folder f = si.getStartingFolder();
		
		si.destroy();
		
		
		
		
//		//test visita
//		System.out.println();
//		System.out.println("****** test visita ******");
//		visitTest(f);
		
		
		
	}
	
	private static void visitTest(Folder folder){
		System.out.println("Folder = "+folder);
		
		if (folder.getSongs() != null){
			for (Song song : folder.getSongs())
				System.out.println("Song = "+song);
		}
		
		if (folder.getFolders() != null){
			for (Folder child : folder.getFolders())
				visitTest(child);
		}
			
		System.out.println();
	}
}
