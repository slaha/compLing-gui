package cz.slahora.compling.gui;

import cz.slahora.compling.gui.main.MainWindow;
import cz.slahora.compling.gui.main.MainWindowController;
import cz.slahora.compling.gui.main.MainWindowControllerImpl;
import cz.slahora.compling.gui.main.MainWindowMenu;
import cz.slahora.compling.gui.model.WorkingTexts;

import javax.swing.*;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 30.3.14 21:30</dd>
 * </dl>
 */
public class Application {


	private final AppContext context;
	private JMenuBar mainWindowMenu;
	private MainWindow mainWindow;

	public Application(AppContext context) {
		this.context = context;
		WorkingTexts workingTexts = new WorkingTexts();
		MainWindowController ctx = new MainWindowControllerImpl(context, workingTexts);
		mainWindow = new MainWindow(ctx, workingTexts);
		mainWindowMenu = new MainWindowMenu(ctx, mainWindow.mainPanel);
		ctx.setMainPanel(mainWindow.mainPanel);
	}

	public JFrame createFrame() {
		JFrame frame = new JFrame("Statistika v lexikální analýze");
		frame.setJMenuBar(mainWindowMenu);
		frame.setContentPane(mainWindow.mainPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();

		return frame;
	}
}
