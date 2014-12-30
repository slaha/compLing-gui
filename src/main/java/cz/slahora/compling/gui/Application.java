package cz.slahora.compling.gui;

import cz.slahora.compling.gui.main.MainWindow;
import cz.slahora.compling.gui.main.MainWindowController;
import cz.slahora.compling.gui.main.MainWindowControllerImpl;
import cz.slahora.compling.gui.main.MainWindowMenu;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.model.WorkingTexts;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Locale;

/**
 *
 * Main class of ComLingGui. It is responsible for creating initial controllers and views
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 30.3.14 21:30</dd>
 * </dl>
 */
public class Application implements MainWindowController.OnTabSelected {


	private static final String TITLE = "Statistika v lexikální analýze";
	private static final String TITLE_APPEND = " – %s";
	private JMenuBar mainWindowMenu;
	private MainWindow mainWindow;
	private JFrame frame;
	private final WorkingTexts workingTexts;

	public Application(AppContext context) {
		workingTexts = new WorkingTexts();
		MainWindowController ctx = new MainWindowControllerImpl(context, workingTexts);
		ctx.registerOnTabChange(this);
		mainWindow = new MainWindow(ctx);
		mainWindowMenu = new MainWindowMenu(ctx, mainWindow.mainPanel, workingTexts);
		ctx.setMainPanel(mainWindow.mainPanel);
	}

	public JFrame createFrame() {
		frame = new JFrame(TITLE);
		frame.setJMenuBar(mainWindowMenu);
		frame.setContentPane(mainWindow.mainPanel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

		int windowWidth =  (int)(width * 0.85);
		int windowHeight = (int)(height * 0.85);
		frame.setSize(windowWidth, windowHeight);

		return frame;
	}

	@Override
	public void onTabSelected(String id) {
		String title;
		if (id != null) {
			final WorkingText workingText = workingTexts.get(id);
			title = String.format(Locale.getDefault(), TITLE + TITLE_APPEND, workingText.getName());
		} else {
			title = TITLE;
		}
		frame.setTitle(title);
	}
}
