package cz.slahora.compling.gui.utils;

import cz.slahora.compling.gui.settings.SettingsManager;

public class GraphUtils {

	public static String getStyleSheet() {
		int nodeSize = SettingsManager.getInstance().getGraphNodeSize();
		int fontSize = SettingsManager.getInstance().getGraphNodeTextSize();
		int strokeWidth = SettingsManager.getInstance().getGraphStrokeWidth();

		return "node {" + System.lineSeparator() +
			"  size: " + nodeSize + "px;" + System.lineSeparator() +
			"  fill-mode: plain;" + System.lineSeparator() +
			"  fill-color: white;" + System.lineSeparator() +
			"  stroke-mode: plain;" + System.lineSeparator() +
			"  stroke-color: black;" + System.lineSeparator() +
			"  stroke-width: " + strokeWidth + ";" + System.lineSeparator() +
			"  text-alignment: center;" + System.lineSeparator() +
			"  text-padding: 3px;" + System.lineSeparator() +
			"  text-size: " + fontSize + "px;" + System.lineSeparator() +
			"}"
			+
			"edge {" + System.lineSeparator() +
			"  size: " + strokeWidth + ";" + System.lineSeparator() +
			"}";
	}


}
