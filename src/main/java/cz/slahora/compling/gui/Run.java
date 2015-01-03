package cz.slahora.compling.gui;

import cz.slahora.compling.gui.settings.FilePreferencesFactory;
import cz.slahora.compling.gui.settings.SettingsManager;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Executable class of CompLingGui
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 22.3.14 7:31</dd>
 * </dl>
 */
public class Run {

	private static JFrame frame;
	private static Map<Object, FontUIResource> defaultFonts = new HashMap<Object, FontUIResource>();

	public static void main(String[] args) {

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());

		try {
			//FIXME remove before release
			String gtk = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			UIManager.setLookAndFeel(gtk);
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		setUiFont();

		AppContext appContext = new AppContext() {
			@Override
			public void exit(int code) {
				System.exit(code);
			}

			@Override
			public void settingsChanged() {
				setUiFont();
				SwingUtilities.updateComponentTreeUI(frame);
			}
		};

		final Application application = new Application(appContext);

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				createAndShowGUI(application);
			}
		});
	}

	private static void setUiFont() {
		final UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		if (SystemUtils.IS_OS_WINDOWS) {
			Font font = (Font) defaults.get("TextArea.font");
			defaults.put("TextArea.font", new FontUIResource(font.getName(), font.getStyle(), 12));
		}
		Enumeration keys = UIManager.getDefaults().keys();
		if (defaultFonts.isEmpty()) {
			while (keys.hasMoreElements()) {
				Object key = keys.nextElement();
				Object value = UIManager.get(key);
				if (value instanceof FontUIResource) {
					Font orig = (FontUIResource) value;
					defaultFonts.put(key, new FontUIResource(orig.getName(), orig.getStyle(), orig.getSize()));
				}
			}
		}

		final double coefficient = SettingsManager.getInstance().getFontSize();
		keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				FontUIResource orig = defaultFonts.get(key);
				final int size = (int) (orig.getSize() * coefficient);
				FontUIResource font = new FontUIResource(orig.getName(), orig.getStyle(), size);
				UIManager.put(key, font);
				if ("Table.font".equals(key)) {
					UIManager.put("Table.rowHeight", size + 2);
				}
			}
		}
	}

	private static void createAndShowGUI(Application application) {
		frame = application.createFrame();
		frame.setVisible(true);
	}
}
