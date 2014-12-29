package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.analysis.Analysis;
import cz.slahora.compling.gui.analysis.AnalysisFactory;
import cz.slahora.compling.gui.analysis.MultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.ui.ScrollablePanel;

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

	private final DocumentListener documentListener;

	public JPanel mainPanel;
	private JTextArea textArea;
	private JButton openFileButton;
	private JButton newTabButton;
	private JLabel nameOfOpenedText;
	private JPanel tabsPanel;
	private JScrollPane tabsScrollPane;
	private JLabel nameLabel;
	private JPanel namePanel;
	private JButton oneTextAnalyse;
	private JButton multipleTextAnalyse;

	public MainWindow(final MainWindowController mainWindowController) {
		this.controller = mainWindowController;
		this.controller.registerTabHolder(this);
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

		final MouseAdapter labelOnClick = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				String newName = MainWindowUtils.renameTabDialog(namePanel, controller.getCurrentPanelId(), nameOfOpenedText.getText());
				if (newName != null) {
					controller.renameText(controller.getCurrentPanelId(), newName);
				}
			}
		};
		namePanel.addMouseListener(labelOnClick);

		oneTextAnalyse.setComponentPopupMenu(createOneTextAnalyseMenu(createMenuListener()));
		oneTextAnalyse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				oneTextAnalyse.getComponentPopupMenu().show(oneTextAnalyse, 10, oneTextAnalyse.getHeight());
			}
		});

		multipleTextAnalyse.setComponentPopupMenu(createMultipleTextAnalyseMenu(createMenuListener()));
		multipleTextAnalyse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				multipleTextAnalyse.getComponentPopupMenu().show(multipleTextAnalyse, 10, multipleTextAnalyse.getHeight());
			}
		});

		oneTextAnalyse.setEnabled(false);
		multipleTextAnalyse.setEnabled(false);
	}

	private ActionListener createMenuListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComponent component = (JComponent) e.getSource();
				int id = (Integer)component.getClientProperty("id");
				final Analysis analysis = AnalysisFactory.create(id);
				if (analysis instanceof SingleTextAnalysis) {
					controller.analyse((SingleTextAnalysis)analysis);
				} else if (analysis instanceof MultipleTextsAnalysis) {
					controller.analyse((MultipleTextsAnalysis)analysis);
				}
			}
		};
	}

	private JPopupMenu createOneTextAnalyseMenu(ActionListener l) {
		final JPopupMenu menu = new JPopupMenu();
		JMenuItem characterFrequency = createMenuItem("Četnost znaků", AnalysisFactory.CHARACTER_COUNTS_ONE, l);
		JMenuItem wordFrequency = createMenuItem("Četnost slov", AnalysisFactory.WORD_COUNTS_ONE, l);

		JMenuItem alliteration = createMenuItem("Aliterace", AnalysisFactory.ALLITERATION, l);
		JMenuItem aggregation = createMenuItem("Agregace", AnalysisFactory.AGGREGATION_ONE, l);

		JMenuItem denotation = createMenuItem("Denotační analýza", AnalysisFactory.DENOTATION, l);

		menu.add(characterFrequency);
		menu.add(wordFrequency);
		menu.addSeparator();
		menu.add(alliteration);
		menu.add(aggregation);
		menu.addSeparator();
		menu.add(denotation);

		return menu;
	}

	private JPopupMenu createMultipleTextAnalyseMenu(ActionListener l) {

		final JPopupMenu menu = new JPopupMenu();
		JMenuItem characterFrequency = createMenuItem("Četnost znaků", AnalysisFactory.CHARACTER_COUNTS_ALL, l);
		JMenuItem wordFrequency = createMenuItem("Četnost slov", AnalysisFactory.WORD_COUNTS_ALL, l);

		JMenuItem assonance = createMenuItem("Asonance", AnalysisFactory.ASSONANCE_ALL, l);
		JMenuItem aggregation = createMenuItem("Agregace", AnalysisFactory.AGGREGATION_ALL, l);


		menu.add(characterFrequency);
		menu.add(wordFrequency);
		menu.addSeparator();
		menu.add(assonance);
		menu.add(aggregation);

		return menu;
	}
	
	private JMenuItem createMenuItem(String text, int actionId, ActionListener l) {
		JMenuItem item = new JMenuItem(text);
		item.putClientProperty("id", actionId);
		item.addActionListener(l);
		return item;
	}

	private void createUIComponents() {

		ScrollablePanel tabsPanel = new ScrollablePanel();
		tabsPanel.setLayout(new BoxLayout(tabsPanel, BoxLayout.Y_AXIS));
		tabsPanel.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
		tabsPanel.setScrollableBlockIncrement(
			ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PERCENT, 100);

		this.tabsPanel = tabsPanel;
		tabsScrollPane = new JScrollPane(tabsPanel);
		tabsScrollPane.getViewport().setBackground(new Color(237, 236, 235));
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

		boolean analysisPossible = controller.getAllPanels().iterator().hasNext();
		oneTextAnalyse.setEnabled(analysisPossible);
		multipleTextAnalyse.setEnabled(analysisPossible);
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
