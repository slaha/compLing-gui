package cz.slahora.compling.gui;

import cz.slahora.compling.gui.main.*;
import cz.slahora.compling.gui.model.WorkingTexts;

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

	private static AppContext appContext;

	public static void main(String[] args) {

		appContext = new AppContext() {
			@Override
			public void exit(int code) {
				System.exit(code);
			}
		};

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

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				createAndShowGUI(appContext);
			}
		});
	}

	private static void createAndShowGUI(AppContext appContext) {

		WorkingTexts workingTexts = new WorkingTexts();
		MainWindowController ctx = new MainWindowControllerImpl(appContext, workingTexts);

		MainWindow mainWindow = new MainWindow(ctx, workingTexts);
		MainWindowMenu mainWindowMenu = new MainWindowMenu(ctx, mainWindow.mainPanel);
		ctx.setMainPanel(mainWindow.mainPanel);

		JFrame frame = new JFrame("Statistika v lexikální analýze");
		frame.setJMenuBar(mainWindowMenu);
		frame.setContentPane(mainWindow.mainPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.pack();

		frame.setVisible(true);

	}
}
