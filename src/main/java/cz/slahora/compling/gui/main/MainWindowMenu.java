package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.about.AboutFrame;
import cz.slahora.compling.gui.about.Licence;
import cz.slahora.compling.gui.analysis.character.CharacterMultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.character.CharacterSingleTextAnalysis;
import cz.slahora.compling.gui.analysis.denotation.DenotationSingleTextAnalysis;
import cz.slahora.compling.gui.analysis.words.WordMultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.words.WordSingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingTexts;
import cz.slahora.compling.gui.utils.IconUtils;
import org.jfree.ui.about.ProjectInfo;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * Menu from menu bar of MainWindow
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 22.3.14 10:48</dd>
 * </dl>
 */
public class MainWindowMenu extends JMenuBar implements MainWindowController.OnTabSelected,ActionListener{

	private static final int EXIT = 0;
	private static final int OPEN = 1;
	private static final int CHARACTER_COUNTS_ONE = 10;
	private static final int CHARACTER_COUNTS_ALL = 11;
	private static final int WORD_COUNTS_ONE = 15;
	private static final int WORD_COUNTS_ALL = 16;
	private static final int DENOTATION = 99;
	private static final int APP_SETTINGS = 100;
	private static final int APP_ABOUT = 101;

	private final MainWindowController controller;
	private final JComponent parentComponent;
	private final WorkingTexts workingTexts;

	private final JMenu analyzeMenu;
	private final Collection<JMenuItem> forActualTextMenus;

	public MainWindowMenu(MainWindowController controller, JComponent parentComponent, WorkingTexts workingTexts) {
		this.controller = controller;
		this.parentComponent = parentComponent;
		this.workingTexts = workingTexts;
		forActualTextMenus = new ArrayList<JMenuItem>();

		controller.registerOnTabChange(this);

		add(createFileMenu());

		analyzeMenu = new JMenu("Analýza");
		add(createAnalyzeMenu());


		add(createFileApplication());
	}

	private JMenu createFileApplication() {
		JMenu application = new JMenu("Aplikace");

		JMenuItem settings = createMenuItem("Nastavení aplikace", APP_SETTINGS, IconUtils.Icon.SETTINGS);
		settings.setEnabled(false);
		JMenuItem about = createMenuItem("O aplikaci", APP_ABOUT, IconUtils.Icon.ABOUT);

		application.add(settings);
		application.addSeparator();
		application.add(about);
		return application;
	}

	private JMenu createAnalyzeMenu() {
		JMenu characterCountMenu = new JMenu("Četnost znaků");

		createCharacterCountMenu(characterCountMenu);
		analyzeMenu.add(characterCountMenu);

		JMenu wordCountMenu = new JMenu("Četnost slov");

		createWordCountMenu(wordCountMenu);
		analyzeMenu.add(wordCountMenu);

		JMenuItem denotation = createMenuItem("Denotační analýza pro '%s'", DENOTATION, null);
		analyzeMenu.add(denotation);
		forActualTextMenus.add(denotation);

		analyzeMenu.setEnabled(false);

		return analyzeMenu;
	}

	private void createCharacterCountMenu(JMenu parent) {
		JMenuItem forActual = createMenuItem("Pro aktuální text '%s'", CHARACTER_COUNTS_ONE, null);
		parent.add(forActual);
		forActualTextMenus.add(forActual);

		JMenuItem forAll = createMenuItem("Pro všechny texty", CHARACTER_COUNTS_ALL, null);
		parent.add(forAll);
	}

	private void createWordCountMenu(JMenu parent) {

		JMenuItem forActual = createMenuItem("Pro aktuální text '%s'", WORD_COUNTS_ONE, null);
		parent.add(forActual);
		forActualTextMenus.add(forActual);

		JMenuItem forAll = createMenuItem("Pro všechny texty", WORD_COUNTS_ALL, null);
		parent.add(forAll);
	}

	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("Soubor");
		fileMenu.add(createMenuItem("Otevřít", OPEN, IconUtils.Icon.DOCUMENT_OPEN));
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("Ukončit", EXIT, IconUtils.Icon.EXIT));
		return fileMenu;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JComponent component = (JComponent) e.getSource();
		final int id = (Integer)component.getClientProperty("id");
		switch (id) {
			case OPEN:
				controller.openFileUsingDialog(parentComponent);
				break;

			case CHARACTER_COUNTS_ONE:
				controller.analyse(new CharacterSingleTextAnalysis());
				break;
			case CHARACTER_COUNTS_ALL:
				controller.analyse(new CharacterMultipleTextsAnalysis());
				break;

			case WORD_COUNTS_ONE:
				controller.analyse(new WordSingleTextAnalysis());
				break;
			case WORD_COUNTS_ALL:
				controller.analyse(new WordMultipleTextsAnalysis());
				break;
			case DENOTATION:
				controller.analyse(new DenotationSingleTextAnalysis());
				break;

			case APP_ABOUT:
				ProjectInfo projectInfo = new ProjectInfo(
					"CompLing Gui", //..name of app
					"0.1_ALPHA",  //..version
					"<html><p style='text-align:center;'>This application provides graphical user interface for using <a href='https://github.com/slaha/compLing'>compLing - the computional linguistic library</a>."
					+ "<p style='text-align:center;'>The application is developed as part of my diploma thesis on Univerzita Pardubice</html>",
					null,
					"Jan Šlahora",
					"Unlicense - Public Domain",
					Licence.LICENCE
				);

				new AboutFrame("O aplikaci", "https://github.com/slaha/compLing-gui",projectInfo).setVisible(true);
				break;
			case APP_SETTINGS:
				break;
			case EXIT:
				controller.exit(0);
				break;
		}
	}

	private JMenuItem createMenuItem(String text, int id, IconUtils.Icon icon) {
		JMenuItem item = new JMenuItem(text);
		if (icon != null) {
			item.setIcon(IconUtils.getIcon(icon));
		}
		item.putClientProperty("id", id);
		item.putClientProperty("text", text);
		item.addActionListener(this);
		return item;
	}

	@Override
	public void onTabSelected(String id) {
		analyzeMenu.setEnabled(id != null);
		if (id != null) {
			final String name = workingTexts.get(id).getName();
			for (JMenuItem item : forActualTextMenus) {
				item.setText(String.format((String) item.getClientProperty("text"), name));
			}
		}
	}
}