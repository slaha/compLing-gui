package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.about.AboutFrame;
import cz.slahora.compling.gui.analysis.Analysis;
import cz.slahora.compling.gui.analysis.AnalysisFactory;
import cz.slahora.compling.gui.analysis.MultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
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
public class MainWindowMenu extends JMenuBar implements MainWindowController.OnTabSelected, ActionListener{

	private static final int EXIT = 0;
	private static final int OPEN = 1;
	//	private static final int APP_SETTINGS = 100;
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

//		JMenuItem settings = createMenuItem("Nastavení aplikace", APP_SETTINGS, IconUtils.Icon.SETTINGS);
//		settings.setEnabled(false);
		JMenuItem about = createMenuItem("O aplikaci", APP_ABOUT, IconUtils.Icon.ABOUT);

//		application.add(settings);
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

		analyzeMenu.addSeparator();

		JMenuItem alliteration = createMenuItem("Aliterace pro text '%s'", AnalysisFactory.ALLITERATION, null);
		analyzeMenu.add(alliteration);
		forActualTextMenus.add(alliteration);

		JMenuItem assonance = createMenuItem("Asonance", AnalysisFactory.ASSONANCE_ALL, null);
		analyzeMenu.add(assonance);

		JMenu aggregation = new JMenu("Agregace");
		createAggregationMenu(aggregation);
		analyzeMenu.add(aggregation);

		analyzeMenu.addSeparator();

		JMenuItem denotation = createMenuItem("Denotační analýza pro '%s'", AnalysisFactory.DENOTATION, null);
		analyzeMenu.add(denotation);
		forActualTextMenus.add(denotation);

		analyzeMenu.setEnabled(false);

		return analyzeMenu;
	}

	private void createCharacterCountMenu(JMenu parent) {
		JMenuItem forActual = createMenuItem("Pro aktuální text '%s'", AnalysisFactory.CHARACTER_COUNTS_ONE, null);
		parent.add(forActual);
		forActualTextMenus.add(forActual);

		JMenuItem forAll = createMenuItem("Pro všechny texty", AnalysisFactory.CHARACTER_COUNTS_ALL, null);
		parent.add(forAll);
	}

	private void createWordCountMenu(JMenu parent) {

		JMenuItem forActual = createMenuItem("Pro aktuální text '%s'", AnalysisFactory.WORD_COUNTS_ONE, null);
		parent.add(forActual);
		forActualTextMenus.add(forActual);

		JMenuItem forAll = createMenuItem("Pro všechny texty", AnalysisFactory.WORD_COUNTS_ALL, null);
		parent.add(forAll);
	}

	private void createAggregationMenu(JMenu parent) {

		JMenuItem forActual = createMenuItem("Pro aktuální text '%s'", AnalysisFactory.AGGREGATION_ONE, null);
		parent.add(forActual);
		forActualTextMenus.add(forActual);

		JMenuItem forAll = createMenuItem("Pro všechny texty", AnalysisFactory.AGGREGATION_ALL, null);
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

			case AnalysisFactory.CHARACTER_COUNTS_ONE:
			case AnalysisFactory.CHARACTER_COUNTS_ALL:
			case AnalysisFactory.WORD_COUNTS_ONE:
			case AnalysisFactory.WORD_COUNTS_ALL:
			case AnalysisFactory.ALLITERATION:
			case AnalysisFactory.ASSONANCE_ALL:
			case AnalysisFactory.AGGREGATION_ONE:
			case AnalysisFactory.AGGREGATION_ALL:
			case AnalysisFactory.DENOTATION:
				Analysis analysis = AnalysisFactory.create(id);
				if (analysis instanceof SingleTextAnalysis) {
					controller.analyse((SingleTextAnalysis)analysis);
				} else if (analysis instanceof MultipleTextsAnalysis) {
					controller.analyse((MultipleTextsAnalysis)analysis);
				}
				break;

			case APP_ABOUT:

				ProjectInfo projectInfo = new CompLingGuiInfo();

				new AboutFrame("O aplikaci", "https://github.com/slaha/compLing-gui", projectInfo).setVisible(true);
				break;
//			case APP_SETTINGS:
//				break;
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