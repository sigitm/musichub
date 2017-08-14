package it.musichub.server.runner;

import it.musichub.server.config.Configuration;

public abstract class MusicHubServiceImpl {

	public static Configuration getConfiguration(){
		return ServiceFactory.getInstance().getConfiguration();
	}

}
