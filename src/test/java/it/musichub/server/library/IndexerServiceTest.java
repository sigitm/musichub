package it.musichub.server.library;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;

public class IndexerServiceTest {

	public static void main(String[] args) {
		String startingDirStr = "D:\\users\\msigismondi.INT\\Desktop";
		String startingDirStr2 = "D:\\users\\msigismondi.INT\\Desktop";
		try {
			if ("SIGIQC".equals(InetAddress.getLocalHost().getHostName())){
				startingDirStr = "N:\\incoming\\##mp3 new";
				startingDirStr2 = "N:\\incoming\\##mp3 new\\Zucchero TODO\\Zucchero - Greatest Hits (1996)NLT-Release";
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ServiceFactory sf = ServiceFactory.getInstance();
		sf.addParam("startingDir", startingDirStr);
		
		sf.init();
		sf.start();
		
		IndexerService si = (IndexerService) sf.getServiceInstance(Service.indexer);
		
		
		
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

		
//		try {
//			TimeUnit.SECONDS.sleep(3);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		System.out.println("****** PARTE 3: refresh parziale ******");
//		si.refresh(startingDirStr2);
		
		
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
		
		///TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX aggiornare!
//		String queryStr = "song.artist == 'Ligabue'";
//		SearchService ss = (SearchService) sf.getServiceInstance(Service.search);
//		QueryOLD query = ss.createQuery(queryStr);
//		List<Song> results = ss.execute(query);
//		System.out.println("search results:");
//		for (Song song : results)
//			System.out.println(song);
		
		

		
		
		
		
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("****** FINE ******");
		
		
		
		Folder f = si.getStartingFolder();
		
		sf.shutdown();
		
		
		
		
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
