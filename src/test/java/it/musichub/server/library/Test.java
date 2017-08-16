package it.musichub.server.library;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.musichub.server.library.model.Folder;
import it.musichub.server.library.model.Song;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;

public class Test {

	public static void main(String[] args) {
		//TODO PROVVISORIO mettere in un test
		String startingDirStr = "D:\\users\\msigismondi.INT\\Desktop";
		String startingDirStr2 = "D:\\users\\msigismondi.INT\\Desktop";
		try {
			if ("SIGIQC".equals(InetAddress.getLocalHost().getHostName())){
				startingDirStr = "N:\\incoming\\##mp3 new";
				startingDirStr2 = "N:\\incoming\\##mp3 new\\Zucchero TODO\\Zucchero - Greatest Hits (1996)NLT-Release";
			}else if ("SARANB".equals(InetAddress.getLocalHost().getHostName())){
				startingDirStr = "C:\\Users\\Sara\\Desktop";
				startingDirStr2 = "C:\\Users\\Sara\\Desktop";
			} 
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ServiceFactory sf = ServiceFactory.getInstance();
		sf.init();
//		sf.getConfiguration().setContentDir(startingDirStr);
		sf.start();
		
//		try {
//			TimeUnit.SECONDS.sleep(30);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println();
//		sf.shutdown();
	}
}
