package cz.slahora.compling.gui;

import org.apache.commons.lang3.SystemUtils;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import java.awt.Font;

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

	public static void main(String[] args) {

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

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

		if (SystemUtils.IS_OS_WINDOWS) {
			final UIDefaults defaults = UIManager.getDefaults();
			final Font font = UIManager.getFont("TextArea.font" /*"TextField.font"*/);
			defaults.put("TextArea.font", font.deriveFont(12f));
		}
		AppContext appContext = new AppContext() {
			@Override
			public void exit(int code) {
				System.exit(code);
			}
		};

		final Application application = new Application(appContext);

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				createAndShowGUI(application);
			}
		});
	}

	private static void createAndShowGUI(Application application) {
		JFrame frame = application.createFrame();

		frame.setVisible(true);

	}
}
