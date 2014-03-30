package cz.slahora.compling.gui;

import javax.swing.*;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>mainWindow.
 * <dd> 22.3.14 7:31</dd>
 * </dl>
 */
public class Run {

	public static void main(String[] args) {

		try {
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
		frame.setVisible(true);
	}
}
