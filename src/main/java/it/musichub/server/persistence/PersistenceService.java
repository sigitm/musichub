package it.musichub.server.persistence;

import java.io.File;

import it.musichub.server.persistence.ex.FileNotFoundException;
import it.musichub.server.persistence.ex.LoadException;
import it.musichub.server.persistence.ex.SaveException;
import it.musichub.server.runner.IMusicHubService;

public interface PersistenceService extends IMusicHubService {

	public <T> T loadFromDisk(Class<T> clazz, String fileName) throws LoadException;

	public <T> void saveToDisk(T object, String fileName) throws SaveException;

}
