package it.musichub.server.library.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import it.musichub.server.library.utils.FileUtils;

public class FolderFactory {
	
	public static Folder fromFilePath(String path, String baseFolderPath, Folder father) {
		Folder folder = new Folder();
		File file = new File(path);
		folder.setPath(path);
		folder.setRelativePath(FileUtils.getRelativePath(file, new File(baseFolderPath)));
		folder.setName(FileUtils.extractFilename(path));
		folder.setFather(father);
		folder.setFile(file);

		return folder;
	}
	
	public static Folder fromRelativePath(String relativePath, Folder root) {
		String rootPath = root.getPath();
		String path = rootPath + relativePath;
		return fromFilePath(path, rootPath, null);
	}

}
