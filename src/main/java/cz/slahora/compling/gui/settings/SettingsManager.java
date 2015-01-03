package cz.slahora.compling.gui.settings;

import java.util.prefs.Preferences;

public class SettingsManager {

	private static final String FONT_SIZE_MULTIPLIER  = "font_size";

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

	public void writeFontSize(double fontSize) {
		prefs.putDouble(FONT_SIZE_MULTIPLIER, fontSize);
	}

	public double getFontSize() {
		return prefs.getDouble(FONT_SIZE_MULTIPLIER, 1d);
	}
}
