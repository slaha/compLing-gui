package cz.slahora.compling.gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Frame;
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
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setExtendedState(Frame.MAXIMIZED_VERT);
	}
}
