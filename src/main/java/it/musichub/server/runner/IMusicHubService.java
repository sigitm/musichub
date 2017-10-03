package it.musichub.server.runner;

import it.musichub.server.ex.ServiceDestroyException;
import it.musichub.server.ex.ServiceInitException;
import it.musichub.server.ex.ServiceStartException;
import it.musichub.server.ex.ServiceStopException;

public interface IMusicHubService {
	
	public enum ServiceState {init, start, stop, destroy}
	
	public ServiceState getState();
	public void setState(ServiceState state);

	public void init() throws ServiceInitException;
	
	public void start() throws ServiceStartException;
	
	public void stop() throws ServiceStopException;
	
	public void destroy() throws ServiceDestroyException;
	
}
