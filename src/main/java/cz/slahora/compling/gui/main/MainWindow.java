package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.model.WorkingTexts;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 22.3.14 7:34</dd>
 * </dl>
 */
public class MainWindow implements ActionListener, TabHolder {

	private final MainWindowController controller;
	private final WorkingTexts texts;

	public JPanel mainPanel;
	private JTextArea textArea;
	private JButton openFileButton;
	private JButton newTabButton;
	private JLabel nameOfOpenedText;
	private JPanel tabsPanel;

	private TabPanels tabPanels;

	public MainWindow(MainWindowController mainWindowController, WorkingTexts texts) {
		this.controller = mainWindowController;
		this.texts = texts;
		this.tabPanels = new TabPanels();
		openFileButton.addActionListener(this);
		newTabButton.addActionListener(this);

		newTabButton.setContentAreaFilled(false);
		newTabButton.setBorder(new MatteBorder(0, 0, 1, 0, Color.gray));

		newTabButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				newTabButton.setOpaque(false);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				newTabButton.setOpaque(true);
			}
		});

	}

	private void createUIComponents() {

		tabsPanel = new JPanel();
		tabsPanel.setLayout(new BoxLayout(tabsPanel, BoxLayout.Y_AXIS));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == openFileButton) {
			java.util.List<WorkingText> workingTexts = controller.openFileUsingDialog(mainPanel);
			if (!workingTexts.isEmpty()) {
				for (WorkingText workingText : workingTexts) {
					tabPanels.addPanel(workingText, this);
				}
				refreshTabs();
			}
		} else if (e.getSource() == newTabButton) {
			WorkingText workingText = controller.newEmptyTab(mainPanel);
			if (workingText != null) {
				tabPanels.addPanel(workingText, this);
				refreshTabs();
			}
		}
	}

	private void refreshTabs() {
		tabsPanel.removeAll();

		for (TabPanel panel : tabPanels.getAll()) {
			tabsPanel.add(panel);
		}

		String id = tabPanels.getCurrentId();
		if (id == null) {
			id = tabPanels.pickNewCurrent();
		}
		onTabChange(id);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tabsPanel.revalidate();
				tabsPanel.repaint();
			}
		});
	}

	@Override
	public void onTabClose(String id) {
		controller.removeTab(id);
		tabPanels.removePanel(id);
		if (tabPanels.currentId(id)) {
			tabPanels.pickNewCurrent();
		}
		refreshTabs();
	}

	@Override
	public void onTabChange(String id) {
		WorkingText workingText = null;
		TabPanel panel = null;
		if (id != null) {
			workingText = controller.onTabChange(id);
			panel = tabPanels.getPanel(id);
		}

		tabPanels.setCurrent(panel);
		textArea.setText(workingText == null ? "" : workingText.getText());
		textArea.setEnabled(workingText != null);
		textArea.setCaretPosition(0);
		nameOfOpenedText.setText(workingText == null ? "" : workingText.getName());
	}
}
