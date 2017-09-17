package it.musichub.server.runner;

import it.musichub.server.config.Configuration;

public abstract class MusicHubServiceImpl {

	protected static Configuration getConfiguration(){
		return ServiceFactory.getInstance().getConfiguration();
	}

}
