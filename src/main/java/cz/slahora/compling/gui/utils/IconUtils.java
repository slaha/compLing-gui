package cz.slahora.compling.gui.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 5.4.14 11:01</dd>
 * </dl>
 */
public class IconUtils {

	public enum Icon {
		DOCUMENT_OPEN("document-open.png"),
		EXIT("exit.png"),
		SETTINGS("settings.png"),
		ABOUT("about.png");

		private final String fileName;
		Icon(String fileName) {
			this.fileName = fileName;
		}
	}


	public static javax.swing.Icon getIcon(Icon icon) {
		try {
			BufferedImage image = ImageIO.read(ClassLoader.getSystemResource(icon.fileName));
			return new ImageIcon(image);
		} catch (Exception e) {
			System.err.println("Unable to load icon " + icon + " from file " + icon.fileName);
			return null;
		}
	}
}