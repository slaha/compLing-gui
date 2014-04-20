package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.model.WorkingTexts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * CompLingGui main window
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

	private final DocumentListener documentListener;

	public JPanel mainPanel;
	private JTextArea textArea;
	private JButton openFileButton;
	private JButton newTabButton;
	private JLabel nameOfOpenedText;
	private JPanel tabsPanel;

	public MainWindow(MainWindowController mainWindowController, WorkingTexts texts) {
		this.controller = mainWindowController;
		this.controller.registerTabHolder(this);
		this.texts = texts;
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

		this.documentListener = new DocumentListener(controller);

		textArea.getDocument().addDocumentListener(documentListener);
		textArea.setBorder(new EmptyBorder(2, 7, 2, 7));
	}

	private void createUIComponents() {

		tabsPanel = new JPanel();
		tabsPanel.setLayout(new BoxLayout(tabsPanel, BoxLayout.Y_AXIS));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == openFileButton) {
			controller.openFileUsingDialog(mainPanel);

		} else if (e.getSource() == newTabButton) {
			controller.newEmptyTab(mainPanel);
		}
	}

	private void refreshTabs() {
		tabsPanel.removeAll();

		for (TabPanel panel : controller.getAllPanels()) {
			tabsPanel.add(panel);
		}

		String id = controller.getCurrentPanelId();
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
	public void onNewTab(java.util.List<String> id) {
		refreshTabs();
	}

	@Override
	public void onTabClose(String id) {
		controller.removeTab(id);


		refreshTabs();
	}

	@Override
	public void onTabChange(String id) {
		WorkingText workingText = controller.onTabChange(id);

		documentListener.suppress(true);
		textArea.setText(workingText == null ? "" : workingText.getText());
		textArea.setEnabled(workingText != null);
		textArea.setCaretPosition(0);
		nameOfOpenedText.setText(workingText == null ? "" : workingText.getName());
		textArea.getDocument().addDocumentListener(documentListener);
		documentListener.suppress(false);
	}

	private static class DocumentListener implements javax.swing.event.DocumentListener {

		private final MainWindowController controller;
		private boolean suppressed;

		private DocumentListener(MainWindowController controller) {
			this.controller = controller;
		}

		public void suppress(boolean suppress) {
			this.suppressed = suppress;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			textChanged(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			textChanged(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			textChanged(e);
		}

		private void textChanged(DocumentEvent e) {
			if (!suppressed) {
				Document document = e.getDocument();
				try {
					String newText = document.getText(0, document.getLength());
					controller.textChanged(newText);
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
