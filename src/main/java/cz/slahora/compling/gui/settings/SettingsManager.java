package cz.slahora.compling.gui.settings;

import java.util.prefs.Preferences;

public class SettingsManager {

	private static final String FONT_SIZE_MULTIPLIER  = "font_size";
	private static final String GRAPH_NODE_SIZE = "graph_node_size";
	private static final String GRAPH_NODE_TEXT_SIZE = "graph_node_text_size";
	private static final String GRAPH_STROKE_WIDTH = "graph_stroke_width";

	private static final int DEFAULT_NODE_SIZE = 30;
	private static final int DEFAULT_NODE_TEXT_SIZE = 13;
	private static final int DEFAULT_STROKE_WIDTH = 1;

	private static SettingsManager INSTANCE = null;

	public static SettingsManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SettingsManager();
		}
		return INSTANCE;
	}

	private final Preferences prefs;

	private SettingsManager() {
		prefs = Preferences.userRoot();
	}

	public boolean writeFontSize(double fontSize) {
		double current = getFontSize();
		if (current == fontSize) {
			return false;
		}
		prefs.putDouble(FONT_SIZE_MULTIPLIER, fontSize);
		return true;
	}

	public double getFontSize() {
		return prefs.getDouble(FONT_SIZE_MULTIPLIER, 1d);
	}


	public void writeGraphNodeSize(int graphNodeSize) {
		prefs.putInt(GRAPH_NODE_SIZE, graphNodeSize);
	}

	public int getGraphNodeSize() {
		return prefs.getInt(GRAPH_NODE_SIZE, DEFAULT_NODE_SIZE);
	}

	public void writeGraphNodeTextSize(int graphNodeTextSize) {
		prefs.putInt(GRAPH_NODE_TEXT_SIZE, graphNodeTextSize);
	}

	public int getGraphNodeTextSize() {
		return prefs.getInt(GRAPH_NODE_TEXT_SIZE, DEFAULT_NODE_TEXT_SIZE);
	}


	public void writeGraphStrokeWidth(int graphStrokeWidth) {
		prefs.putInt(GRAPH_STROKE_WIDTH, graphStrokeWidth);
	}


	public int getGraphStrokeWidth() {
		return prefs.getInt(GRAPH_STROKE_WIDTH, DEFAULT_STROKE_WIDTH);
	}
}
