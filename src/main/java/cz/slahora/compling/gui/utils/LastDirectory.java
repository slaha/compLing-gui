package cz.slahora.compling.gui.utils;


import java.io.File;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 29.3.14 18:41</dd>
 * </dl>
 */
public class LastDirectory {

	private static LastDirectory INSTANCE;

	private File lastDirectory;

	public static LastDirectory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new LastDirectory();
		}
		return INSTANCE;
	}


	public File getLastDirectory() {
		return lastDirectory;
	}

	public void setLastDirectory(File lastDirectory) {
		this.lastDirectory = lastDirectory;
	}
}
