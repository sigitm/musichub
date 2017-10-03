package it.musichub.server.runner;

import it.musichub.server.config.Configuration;
import it.musichub.server.runner.IMusicHubService.ServiceState;

public abstract class MusicHubServiceImpl {

	private ServiceState state = null;

	public ServiceState getState() {
		return state;
	}

	public void setState(ServiceState state) {
		this.state = state;
	}

	protected static Configuration getConfiguration(){
		return ServiceFactory.getInstance().getConfiguration();
	}
}
