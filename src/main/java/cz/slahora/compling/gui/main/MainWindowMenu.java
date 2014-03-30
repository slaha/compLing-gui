package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.analysis.character.CharacterMultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.character.CharacterSingleTextAnalysis;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * TODO 
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

	private final MainWindowController controller;
	private final JComponent parentComponent;
	private final JMenu analyzeMenu;

	public MainWindowMenu(MainWindowController controller, JComponent parentComponent) {
		this.controller = controller;
		this.parentComponent = parentComponent;

		controller.registerOnTabChange(this);

		add(createFileMenu());

		analyzeMenu = new JMenu("Analýza");
		add(createAnalyzeMenu());
	}

	private JMenu createAnalyzeMenu() {
		JMenu characterCountMenu = new JMenu("Četnost znaků");

		JMenuItem forActual = createMenuItem("Pro aktuální text", CHARACTER_COUNTS_ONE);
		characterCountMenu.add(forActual);

		JMenuItem forAll = createMenuItem("Pro všechny text", CHARACTER_COUNTS_ALL);
		characterCountMenu.add(forAll);

		analyzeMenu.add(characterCountMenu);
		analyzeMenu.setEnabled(false);

		return analyzeMenu;
	}

	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("Soubor");
		fileMenu.add(createMenuItem("Otevřít", OPEN));
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("Ukončit", EXIT));
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
			case EXIT:
				controller.exit(0);
				break;
		}
	}

	private JMenuItem createMenuItem(String text, int id) {
		JMenuItem item = new JMenuItem(text);
		item.putClientProperty("id", id);
		item.addActionListener(this);
		return item;
	}

	@Override
	public void onTabSelected(String id) {
		analyzeMenu.setEnabled(id != null);
	}
}