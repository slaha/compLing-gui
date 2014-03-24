package cz.slahora.compling.gui.main;

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
public class MainWindowMenu extends JMenuBar implements ActionListener {

	private static final int OPEN = 1;
	private static final int EXIT = 0;

	private final MainWindowController controller;
	private final JComponent parentComponent;

	public MainWindowMenu(MainWindowController controller, JComponent parentComponent) {
		this.controller = controller;
		this.parentComponent = parentComponent;

		JMenu souborPopupMenu = new JMenu("Soubor");

		souborPopupMenu.add(createMenuItem("Otevřít", OPEN));
		souborPopupMenu.addSeparator();
		souborPopupMenu.add(createMenuItem("Ukončit", EXIT));

		add(souborPopupMenu);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JComponent component = (JComponent) e.getSource();
		final int id = (Integer)component.getClientProperty("id");
		switch (id) {
			case OPEN:
				controller.openFileUsingDialog(parentComponent);
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
}