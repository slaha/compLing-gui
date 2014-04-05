package cz.slahora.compling.gui.utils;

import java.awt.*;
import java.net.URL;

/**
 * 
 * TODO
 * 
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd>5.4.14 12:16</dd>
 * </dl>
 */
public class HyperlinkUtils {
	public static boolean openUrl(URL url) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(url.toURI());
				return true;
			} catch (Exception e) {
				System.err.println("Unable to open URL " + url.toString() + " in browser");
			}
		}
		else if (desktop == null) {
			System.err.println("Desktop is not supported on current platform");
		} else {
			System.err.println("Desktop action BROWSE is not supported on current platform");
		}
		return false;
	}
}
