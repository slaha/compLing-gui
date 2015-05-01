package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.DenotationElement;
import cz.compling.model.denotation.DenotationWord;
import cz.compling.model.denotation.Hreb;
import cz.slahora.compling.gui.analysis.CsvExporter;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.LastDirectory;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.*;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 6.4.14 12:05</dd>
 * </dl>
 */
public class GuiDenotationAnalysis {

	public static final int SPIKE_NUMBER_COLUMN = 0;
	public static final int SPIKE_SIZE_COLUMN = 1;
	public static final int SPIKE_WORDS_COLUMN = 2;

	public static class DenotationPanel extends JPanel implements ActionListener {

		private final DenotationSingleTextAnalysis denotationAnalysis;
		private final JFrame frame;

		private final JButton exportBtn;
		private final JButton importBtn;
		private final JButton cancelBtn;
		private final JButton doneBtn;

		private final WorkingText workingText;

		private final ImportExportHandler importExportHandler;

		private GuiDenotationModel model;
		private DenotationHrebsPanel denotationHrebsPanel;
		private DenotationPoemPanel denotationPoemPanel;
		private JSplitPane middlePanel;

		public DenotationPanel(DenotationSingleTextAnalysis denotationAnalysis, JFrame frame, WorkingText workingText) {
			super(new GridBagLayout());

			this.denotationAnalysis = denotationAnalysis;
			this.frame = frame;
			this.workingText = workingText;
			this.importExportHandler = new ImportExportHandler(this, LastDirectory.getInstance());

			JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
			toolBar.setFloatable(false);
			exportBtn = new JButton("Export", IconUtils.getIcon(IconUtils.Icon.DOCUMENT_SAVE));
			importBtn = new JButton("Import", IconUtils.getIcon(IconUtils.Icon.DOCUMENT_OPEN));
			importBtn.addActionListener(this);
			exportBtn.addActionListener(this);
			toolBar.add(importBtn);
			toolBar.add(exportBtn);
			GridBagConstraints gbc;
			gbc = new GridBagConstraintBuilder().gridXY(0, 0).fill(GridBagConstraints.HORIZONTAL).weightX(1).build();
			add(toolBar, gbc);

			final JPanel bottomPanel = new JPanel(new GridBagLayout());

			cancelBtn = new JButton("Cancel", IconUtils.getIcon(IconUtils.Icon.CANCEL));
			cancelBtn.addActionListener(this);

			doneBtn = new JButton("Hotovo", IconUtils.getIcon(IconUtils.Icon.NEXT));
			doneBtn.addActionListener(this);
			doneBtn.setHorizontalTextPosition(JButton.LEFT);
			doneBtn.setEnabled(false);
			Insets insets = new Insets(3, 0, 3, 5);

			JPanel buttonsPanel = new JPanel();
			buttonsPanel.add(cancelBtn);
			buttonsPanel.add(doneBtn);
			gbc = new GridBagConstraintBuilder().gridXY(0, 0).insets(insets).anchor(GridBagConstraints.EAST).weightX(1).build();
			bottomPanel.add(buttonsPanel, gbc);

			gbc = new GridBagConstraintBuilder().gridXY(0, 2).fill(GridBagConstraints.HORIZONTAL).weightX(1).build();
			add(bottomPanel, gbc);

			setModel(new GuiDenotationModel(workingText));
		}

		private void setModel(GuiDenotationModel denotationModel) {
			this.model = denotationModel;
			if (middlePanel != null) {
				remove(middlePanel);
			}

			denotationPoemPanel = new DenotationPoemPanel(model, this);
			denotationHrebsPanel = new DenotationHrebsPanel(model.getHrebsModel(), this);
			middlePanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(denotationPoemPanel), new JScrollPane(denotationHrebsPanel));
			middlePanel.setContinuousLayout(true);

			GridBagConstraints gbc = new GridBagConstraintBuilder().gridXY(0, 1).fill(GridBagConstraints.BOTH).weightX(1).weightY(1).build();
			add(middlePanel, gbc);

			validate();

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					int location = (int) denotationPoemPanel.getPreferredSize().getWidth();
					location += middlePanel.getDividerSize() * 2;
					location += 50;
					middlePanel.setDividerLocation(location);
					denotationPoemPanel.refresh(-1); //..refresh all
					denotationHrebsPanel.refresh();
				}
			});

			onElementAssigned();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source = e.getSource();
			if (source == exportBtn) {
				importExportHandler.export(model);
			}
			else if (source == importBtn) {
				final GuiDenotationModel denotationModel = importExportHandler.importCsv(model.getCsvLoader(), workingText);
				if (denotationModel != null) {
					setModel(denotationModel);

				}
			}
			else if (source == doneBtn) {
				denotationAnalysis.done();
			}
			else if (source == cancelBtn) {
				closeWindow();
			}
		}

		public IDenotation getDenotation() {
			return model.getDenotation();
		}

		public void refreshHrebs(int number) {
			denotationHrebsPanel.refresh(number);
		}

		public void refreshPoems(int number) {
			denotationPoemPanel.refresh(number);
		}

		public boolean isAnyHrebInTheTable() {
			return denotationHrebsPanel.isAnyHrebInTheTable();
		}

		public void save() {
			importExportHandler.export(model);
		}

		public void closeWindow() {
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}

		public void onElementAssigned() {
			doneBtn.setEnabled(denotationPoemPanel.hasEveryWordHreb());
		}
	}

	private static class ImportExportHandler {

		private final JComponent parent;
		private final LastDirectory lastDir;

		public ImportExportHandler(JComponent parent, LastDirectory instance) {
			this.parent = parent;
			this.lastDir = instance;
		}

		public void export(GuiDenotationModel model) {
			CsvExporter exporter = new CsvExporter(model.getCsvSaver().saveToCsv(model));
			File csvFile = FileChooserUtils.getFileToSave(lastDir.getLastDirectory(), parent, "csv");
			if (csvFile == null) {
				return;
			} else if (csvFile.exists()) {
				int i = JOptionPane.showConfirmDialog(parent, "Soubor " + csvFile.getName() + " již existuje. Chcete jej přepsat?", "Soubor již existuje", JOptionPane.YES_NO_OPTION);
				if (i != JOptionPane.YES_OPTION) {
					return;
				}
			}
			try {
				exporter.export(csvFile);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(parent, "Chyba při ukládání souboru " + csvFile.getName(), "Chyba", JOptionPane.ERROR_MESSAGE);
			} finally {
				lastDir.setLastDirectory(csvFile.getParentFile());
			}

		}

		public GuiDenotationModel importCsv(Csv.CsvLoader<GuiDenotationModel> csvLoader, WorkingText workingText) {
			File file = FileChooserUtils.getFileToOpen(lastDir.getLastDirectory(), parent, "csv");
			if (file == null) {
				return null;
			}
			try {
				List<String> lines = IOUtils.readLines(new FileInputStream(file), FileUtils.UTF8);
				CsvData csvData = new CsvData(lines);
				GuiDenotationModel newModel = new GuiDenotationModel(workingText);
				csvLoader.loadFromCsv(csvData, newModel);
				return newModel;

			} catch (IOException ex) {
				JOptionPane.showMessageDialog(parent, "Chyba při čtení souboru " + file.getName(), "Chyba", JOptionPane.ERROR_MESSAGE);
			} catch (Csv.CsvParserException pe) {
				JOptionPane.showMessageDialog(parent, "Chyba při parsování souboru " + file.getName() + "\n\nChyba: " + pe.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
				pe.printStackTrace();
			} finally {
				lastDir.setLastDirectory(file.getParentFile());
			}
			return null;
		}
	}

	private static class DenotationPoemPanel extends JPanel {

		private static final Insets STROPHE_INSETS = new Insets(1, 1, 25, 1);
		private static final GridBagConstraintBuilder BUILDER = new GridBagConstraintBuilder().anchor(GridBagConstraints.LINE_START);

		private final TIntObjectMap<WordPanel> wordPanels;

		private final DenotationPanel denotationPanel;

		public DenotationPoemPanel(GuiDenotationModel denotationModel, DenotationPanel denotationPanel) {
			super(new GridBagLayout());

			setBackground(Color.white);

			this.denotationPanel = denotationPanel;
			GuiDenotationPoemModel poemModel = denotationModel.getPoemModel();

			this.wordPanels = new TIntObjectHashMap<WordPanel>();

			JPanel poemPanel = new JPanel(new GridBagLayout());
			poemPanel.setBackground(Color.white);

			int currentStrophe = 0;
			int currentStropheInPoem = 0;
			int currentVerse = 0;
			int currentVerseInStrophe = 0;
			JPanel strophePanel = null;
			JPanel versePanel = null;
			WordPanel.parent = this;
			for (int wordIndex = 1; wordIndex <= poemModel.getCountOfWords(); wordIndex++) {
				//..get word from poem
				DenotationWord word = poemModel.getWord(wordIndex);

				//..new strophe → we need new panel
				if (currentStrophe != word.getStropheNumber() || strophePanel == null) {
					strophePanel = new JPanel(new GridBagLayout());
					strophePanel.setBackground(Color.white);
					poemPanel.add(strophePanel, BUILDER.copy().insets(STROPHE_INSETS).gridY(currentStropheInPoem++).build());
					currentStrophe = word.getStropheNumber();
					currentVerseInStrophe = 0;
				}

				if (currentVerse != word.getVerseNumber() || versePanel == null) {
					versePanel = new JPanel(new GridBagLayout());
					strophePanel.add(versePanel, BUILDER.copy().gridY(currentVerseInStrophe++).build());
					currentVerse = word.getVerseNumber();
				}


				WordPanel wordPanel = new WordPanel(new GuiDenotationWord(word, poemModel.getDenotation()));
				wordPanels.put(word.getNumber(), wordPanel);
				versePanel.add(wordPanel, BUILDER.copy().build());
			}

			add(poemPanel, new GridBagConstraintBuilder().gridXY(0, 0).anchor(GridBagConstraints.NORTH).weightY(1).build());
		}

		public void refresh(WordPanel wordPanel) {
			refresh(wordPanel.word.getDenotationWord().getNumber());
		}

		public void refresh(final int _number) {
			wordPanels.forEachEntry(new TIntObjectProcedure<WordPanel>() {
				@Override
				public boolean execute(int number, WordPanel wordPanel) {
					if (number >= _number) {
						wordPanel.refresh();

					}

					return true;
				}
			});

		}

		public void refreshHrebs(int number) {
			denotationPanel.refreshHrebs(number);
		}

		public boolean hasEveryWordHreb() {
			for (WordPanel wordPanel : wordPanels.valueCollection()) {
				final DenotationWord word = wordPanel.word.getDenotationWord();

				if (!word.isIgnored() && !word.isJoined() && !word.areAllHrebsAssigned()) {
					return false;
				}
			}
			return true;
		}

		public void onElementAssigned() {
			denotationPanel.onElementAssigned();

		}
	}
	private static class WordPanel extends JPanel {

		static DenotationPoemPanel parent;

		private static final Insets INSETS = new Insets(1, 7, 1, 1);
		private static final Insets IGNORED_INSETS = new Insets(1, 0, 1, 0);

		final JLabel numberLabel, wordLabel;
		final Font _font;

		private GuiDenotationWord word;
		private final int wordNumber;

		public WordPanel(GuiDenotationWord guiDenotationWord) {
			super(new GridBagLayout());
			setBackground(Color.white);
			setBorder(new EmptyBorder(INSETS));

			this.word = guiDenotationWord;
			this.wordNumber = guiDenotationWord.getDenotationWord().getNumber();

			numberLabel = new JLabel(word.getDenotationElementsAsString());
			wordLabel = new JLabel(word.getDenotationWordsAsString());
			_font = wordLabel.getFont();

			add(numberLabel, new GridBagConstraintBuilder().gridXY(0, 0).anchor(GridBagConstraints.NORTH).build());
			add(wordLabel, new GridBagConstraintBuilder().gridXY(0, 1).anchor(GridBagConstraints.SOUTH).build());

			addMouseListener(new MouseAdapter());

			setComponentPopupMenu(new WordPanelPopup(guiDenotationWord.getDenotation()));
		}

		private void changeFont(boolean hovered) {
			final Font font;
			if (hovered) {
				font = _font.deriveFont(Font.BOLD);
			} else {
				font = _font.deriveFont(Font.PLAIN);
			}
			Map<TextAttribute, Object> attributes;
			if (word.getDenotationWord().getWords().size() > 1) {
				attributes = new HashMap<TextAttribute, Object>();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			} else {
				attributes = null;
			}
			final Font underlineFont =  font.deriveFont(attributes);
			final Color wordColor, numberColor;
			if (word.getDenotationWord().isIgnored()) {
				numberColor = wordColor = Color.gray;

			} else if (!word.getDenotationWord().hasFreeElement()) {
				numberColor = Color.gray;
				wordColor = Color.black;
			} else {
				numberColor = wordColor = Color.black;
			}

			wordLabel.setFont(underlineFont);
			wordLabel.setForeground(wordColor);

			numberLabel.setFont(font);
			numberLabel.setForeground(numberColor);
		}

		public void refresh() {
			DenotationWord denotationWord = word.getDenotation().getWord(wordNumber);
			if (!word.getDenotationWord().equals(denotationWord)) {
				word = new GuiDenotationWord(denotationWord, word.getDenotation());
			}
			String txt;
			txt = word.getDenotationElementsAsString();
			numberLabel.setText(StringUtils.isEmpty(txt) ? " " : txt);

			txt = word.getDenotationWordsAsString();
			wordLabel.setText(StringUtils.isEmpty(txt) ? " " : txt);
			setBorder(new EmptyBorder(word.getDenotationWord().isJoined() ? IGNORED_INSETS: INSETS));
			setVisible(!word.getDenotationWord().isJoined());
			changeFont(false);
			if (word.getDenotationWord().isInHreb()) {
				setToolTipText("Patří do hřebu č. " + word.getDenotationWord().getHrebs());
			} else {
				setToolTipText(null);
			}
		}

		private class MouseAdapter extends java.awt.event.MouseAdapter {

			@Override
			public void mouseEntered(MouseEvent e) {
				changeFont(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				changeFont(false);
			}
		}

		private class WordPanelPopup extends JPopupMenu implements ActionListener {

			public static final int SPIKES_PER_MENU = 20;

			public static final String SPIKES_ADD_SUBMENU = "hrebs_add_submenu";
			public static final String SPIKES_DUPLICATE_SUBMENU = "hrebs_duplicate_submenu";
			public static final String SPIKES_REMOVE_SUBMENU = "hrebs_remove_submenu";
			public static final String SPIKE_KEY = "hreb";

			private final IDenotation denotation;
			private final JMenuItem ignore, addElement, removeElement, join, split;
			private final JMenu hrebsAddMenu, hrebsDuplicateMenu, hrebsRemoveMenu;

			private WordPanelPopup(IDenotation denotation) {
				this.denotation = denotation;

				ignore = new JMenuItem("Ignorovat");
				addElement = new JMenuItem("Přidat denotační element");
				removeElement = new JMenuItem("Odebrat denotační element");
				join = new JMenuItem("Sloučit s " + (word.getNextWordToJoin() != null ? word.getNextWordToJoin().getWords() : ""));
				split = new JMenuItem("Oddělit " + (word.hasJoinedWords() ? word.getLastJoinedWord() :""));

				hrebsAddMenu = new JMenu("Přidat do hřebu");
				hrebsDuplicateMenu = new JMenu("Přidat do jiného hřebu");
				hrebsRemoveMenu = new JMenu("Odebrat z hřebu");

				ignore.addActionListener(this);
				addElement.addActionListener(this);
				removeElement.addActionListener(this);
				join.addActionListener(this);
				split.addActionListener(this);

				addPopupMenuListener(new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						onWordIgnored();
						onElementChanged();
						onWordJoined();
						loadHrebs();
					}

					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					}

					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {
					}
				});
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (source == ignore) {
					final boolean isIgnoredNow = word.getDenotationWord().isIgnored();
					if (!isIgnoredNow && word.getDenotationWord().isInHreb()) {
						if (!MessagesUtils.notifyIsInHreb(parent, word)) {
							return;
						} else {
							for (DenotationElement element : word.getDenotationWord().getDenotationElements()) {
								final Hreb hreb = element.getHreb();
								if (hreb != null) {
									hreb.remove(word.getDenotationWord());
								}

							}

						}
					}

					word.setIgnored(!isIgnoredNow);

				} else if (source == addElement) {
					word.addElement();

				} else if (source == removeElement) {
					final DenotationElement highestNumber = word.getHighestDenotationElement();
					if (highestNumber.isInHreb()) {
						if (!MessagesUtils.notifyIsInElement(parent, highestNumber)) {
							return;
						}
					}

					word.removeElement(highestNumber);

				} else if (source == join) {
					word.joinNext();

				} else if (source == split) {
					word.splitLast();

				} else if (source instanceof JMenuItem) {
					JMenuItem menuItem = (JMenuItem) source;
					if (SPIKES_ADD_SUBMENU.equals(menuItem.getName())) {

						Hreb hreb = (Hreb) menuItem.getClientProperty(SPIKE_KEY);
						DenotationElement hrebNumber = word.getDenotationWord().getFreeElement();

						//..check if the word is already in any other hreb. If so, ask for divide
						if (word.getDenotationWord().getDenotationElements().size() > 1
							&& word.getDenotationWord().isInHreb()) {

							String input = null;
							do {
								input = MessagesUtils.getDividedValue(parent.denotationPanel, word.getDenotationWord(), input);
							} while (input != null && !MessagesUtils.checkInput(input, word.getDenotationWord().getWords().toString()));
							if (input == null) {
								//..canceled
								return;
							}
							hreb.addWord(word.getDenotationWord(), input);

						}
						else {

							hreb.addWord(word.getDenotationWord(), null);
							hrebNumber.onAddToHreb(hreb);
						}
						parent.onElementAssigned();
						parent.refreshHrebs(hreb.getNumber());

					} else if (SPIKES_DUPLICATE_SUBMENU.equals(menuItem.getName())) {

						Hreb hreb = (Hreb) menuItem.getClientProperty(SPIKE_KEY);
						DenotationElement duplicate = word.duplicate(word.getHighestDenotationElement());

						hreb.addWord(word.getDenotationWord(), duplicate.getText(), duplicate.getNumber());
						duplicate.onAddToHreb(hreb);
						parent.refreshHrebs(hreb.getNumber());

					} else if (SPIKES_REMOVE_SUBMENU.equals(menuItem.getName())) {
						Hreb hreb = (Hreb) menuItem.getClientProperty(SPIKE_KEY);
						hreb.remove(word.getDenotationWord());
						parent.onElementAssigned();
						parent.refreshHrebs(hreb.getNumber());
					}
				}
				parent.onElementAssigned();
				parent.refresh(WordPanel.this);
			}

			private void loadHrebs() {
				hrebsAddMenu.removeAll();
				hrebsDuplicateMenu.removeAll();
				hrebsRemoveMenu.removeAll();
				int menuPosition = 0;

				final int hrebsCount = denotation.getHrebs().size();
				if (hrebsCount > 0 && !word.getDenotationWord().isIgnored()) {

					JMenu[] hrebsSubMenus = createHrebsSubmenus();

					if (hrebsSubMenus.length > 0) {
						String menuItemMessage;
						String menuItemName;
						if (word.getDenotationWord().hasFreeElement()) {
							menuItemMessage = "Přidat do hřebu č. ";
							menuItemName = SPIKES_ADD_SUBMENU;
						} else {
							menuItemMessage = "Duplikovat do hřebu č. ";
							menuItemName = SPIKES_DUPLICATE_SUBMENU;
						}

						for (Hreb hreb : denotation.getHrebs()) {
							if (!word.getDenotationWord().isInHreb(hreb)) {
								JMenuItem hrebItem = new JMenuItem(menuItemMessage + hreb.getNumber());
								hrebItem.setName(menuItemName);
								hrebItem.putClientProperty(SPIKE_KEY, hreb);
								hrebItem.addActionListener(this);
								int index = hreb.getNumber() / SPIKES_PER_MENU;
								//..we need to decrement index. But only if there is no not-full menu
								if (hreb.getNumber() % SPIKES_PER_MENU == 0) {
									index--;
								}
								if (index >= hrebsSubMenus.length) {
									index = hrebsSubMenus.length - 1;
								}
								hrebsSubMenus[index].add(hrebItem);
							}
						}

						final JMenu toAdd, toRemove;
						if (word.getDenotationWord().hasFreeElement()) {
							toAdd = hrebsAddMenu;
							toRemove = hrebsDuplicateMenu;
						} else {
							toAdd = hrebsDuplicateMenu;
							toRemove = hrebsAddMenu;
						}
						for (JMenu hrebsSubMenu : hrebsSubMenus) {
							if (hrebsSubMenu.getMenuComponentCount() > 0) {
								toAdd.add(hrebsSubMenu);
							}
						}

						add(toAdd, menuPosition++);
						remove(toRemove);

					} else {
						//..no hreb available
						remove(hrebsAddMenu);
						remove(hrebsDuplicateMenu);
					}
				} else {
					remove(hrebsAddMenu);
					remove(hrebsDuplicateMenu);
					remove(hrebsRemoveMenu);
				}
				if (hrebsCount > 0 && !word.getDenotationWord().isIgnored() && word.getDenotationWord().isInHreb()) {
					for (Hreb hreb : denotation.getHrebs()) {
						if (word.getDenotationWord().isInHreb(hreb)) {
							JMenuItem hrebItem = new JMenuItem("Odebrat z hřebu č. " + hreb.getNumber());
							hrebItem.setName(SPIKES_REMOVE_SUBMENU);
							hrebItem.putClientProperty(SPIKE_KEY, hreb);
							hrebItem.addActionListener(this);
							hrebsRemoveMenu.add(hrebItem);
						}
					}
					add(hrebsRemoveMenu, menuPosition);
				} else {
					remove(hrebsRemoveMenu);
				}
			}

			private JMenu[] createHrebsSubmenus() {
				final int hrebsCount = countOfHrebsForWord();
				final int lastMenuItems = hrebsCount % SPIKES_PER_MENU;
				//..count of hrebs / hrebs in one submenu + one menu if hrebs count is not divided by hrebsPerMenu
				final int menusCount = hrebsCount / SPIKES_PER_MENU + ((lastMenuItems > 0) ? 1 : 0);
				JMenu[] menus = new JMenu[menusCount];
				for (int i = 0; i < menus.length; i++) {
					int l, h;
					l = i * SPIKES_PER_MENU + 1;
					h = l + SPIKES_PER_MENU - 1;
					menus[i] = new JMenu("Hřeby č. " + l + " až " + h);
				}

				return menus;
			}

			private int countOfHrebsForWord() {
				int count = 0;
				for (Hreb hreb : denotation.getHrebs()) {
					if (!word.getDenotationWord().isInHreb(hreb)) {
						count++;
					}
				}
				return count;
			}

			private void onElementChanged() {

				if (word.getDenotationWord().isIgnored()) {
					remove(addElement);
					return;
				}
				add(addElement, 1);

				if (word.canRemoveElement()) {
					add(removeElement, 2);
				} else {
					remove(removeElement);
				}
			}

			private void onWordJoined() {
				DenotationWord nextWord = word.getNextWordToJoin();
				if (nextWord == null)  {
					remove(join);
				} else {
					join.setText("Sloučit s " + nextWord.getWords());
					add(join);

					onElementChanged();
				}
				if (word.hasJoinedWords()) {
					split.setText("Oddělit " + word.getLastJoinedWord());
					add(split);
				}
			}

			private void onWordIgnored() {

				add(ignore, 0);
				if (word.getDenotationWord().isIgnored()) {
					ignore.setText("Brát v potaz");
				} else {
					ignore.setText("Ignorovat");
				}
			}

		}
	}

	private static class DenotationHrebsPanel extends JPanel implements ActionListener {
		private final GuiDenotationHrebsModel model;
		private final DenotationHrebsTableModel tableModel;

		private final JButton addBtn;
		private final JButton removeBtn;
		private final JTable table;
		private final DenotationPanel panel;

		public DenotationHrebsPanel(GuiDenotationHrebsModel model, DenotationPanel denotationPanel) {
			super(new GridBagLayout());

			this.panel = denotationPanel;
			this.model = model;
			this.tableModel = new DenotationHrebsTableModel(model);

			JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
			toolBar.setFloatable(false);
			addBtn = new JButton("Nový hřeb", IconUtils.getIcon(IconUtils.Icon.ADD));
			removeBtn = new JButton("Odstranit hřeb", IconUtils.getIcon(IconUtils.Icon.REMOVE));
			removeBtn.setEnabled(false);
			addBtn.addActionListener(this);
			removeBtn.addActionListener(this);
			toolBar.add(addBtn);
			toolBar.add(removeBtn);
			GridBagConstraintBuilder builder;
			builder = new GridBagConstraintBuilder().gridXY(0, 0).fill(GridBagConstraints.HORIZONTAL).anchor(GridBagConstraints.NORTHWEST).weightX(1);
			add(toolBar, builder.build());


			table = new JXTable(tableModel) {

				final int _rowHeight = getRowHeight();

				@Override
				public TableCellRenderer getCellRenderer(int row, int column) {
					if (column != SPIKE_WORDS_COLUMN) {
						return new SingleLineCellRenderer(_rowHeight);
					}
					return new MultilineCellRenderer(_rowHeight);
				}
			};

			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setColumnSelectionAllowed(false);
			table.setRowSelectionAllowed(true);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			table.setAutoCreateRowSorter(false);

			table.getModel().addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					ColumnsAutoSizer.sizeColumnsToFit(table, 10, SPIKE_WORDS_COLUMN);
				}
			});

			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						removeBtn.setEnabled(table.getSelectedRow() >= 0);
					}
				}
			});

			builder = new GridBagConstraintBuilder().gridXY(0, 1).fill(GridBagConstraints.BOTH).anchor(GridBagConstraints.NORTHWEST).weightX(1).weightY(1);
			add(new JScrollPane(table), builder.copy().build());

			ColumnsAutoSizer.sizeColumnsToFit(table, 10, SPIKE_WORDS_COLUMN);


		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source = e.getSource();
			if (source == addBtn) {
				model.createNewHreb();
			} else if (source == removeBtn) {
				Object selected = table.getValueAt(table.getSelectedRow(), 0);
				if (selected != null) {
					int lowestWordNumber = model.removeHreb(Integer.parseInt(selected.toString()));
					panel.refreshPoems(lowestWordNumber);
				}
			}
			tableModel.fireTableDataChanged();
		}

		public void refresh(int number) {
			int row = findRowForHreb(number);
			if (row >= 0) {
				tableModel.fireTableRowsUpdated(row, row);
			}
		}

		private int findRowForHreb(int number) {
			for (int row = 0; row < table.getRowCount(); row++) {
				Object valueAt = table.getValueAt(row, 0);
				if (valueAt != null) {
					int i = Integer.parseInt(valueAt.toString());
					if (i == number) {
						return row;
					}
				}
			}
			throw new IllegalStateException("No Hreb with number " + number + " found in the table");
		}

		public void refresh() {
			tableModel.fireTableDataChanged();
		}

		public boolean isAnyHrebInTheTable() {
			return model.isAnyHrebInTheTable();
		}

		private static class MultilineCellRenderer extends JTextArea implements TableCellRenderer {

			private final int rowHeight;

			public MultilineCellRenderer(int rowHeight) {
				this.rowHeight = rowHeight;
				setLineWrap(true);
				setWrapStyleWord(true);
				setOpaque(true);
			}

			public Component getTableCellRendererComponent(final JTable table, Object value, boolean isSelected, boolean hasFocus, final int row, int column) {
				if (isSelected) {
					setForeground(table.getSelectionBackground());
				} else {
					setForeground(table.getForeground());
					setBackground(table.getBackground());
				}

				setText((value == null) ? "" : value.toString());
				//..need to be called after painting. Very nice, Java, very nice
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (row >= 0 && row < table.getRowCount()) {
							table.setRowHeight(row, getWrappedLines() * rowHeight);
						}
					}
				});

				return this;
			}

			public int getWrappedLines() {
				View view = getUI().getRootView(this).getView(0);
				int preferredHeight = (int) view.getPreferredSpan(View.Y_AXIS);
				int lineHeight = getFontMetrics(getFont()).getHeight();
				return preferredHeight / lineHeight;
			}
		}

		private static class SingleLineCellRenderer extends JLabel implements TableCellRenderer {

			private final int rowHeight;

			private SingleLineCellRenderer(int rowHeight) {
				this.rowHeight = rowHeight;
			}

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				setOpaque(true);
				setText(value == null ? "" : value.toString());

				setAlignmentX(JLabel.LEFT_ALIGNMENT);
				setAlignmentY(JLabel.TOP_ALIGNMENT);

				if (isSelected) {
					setBackground(table.getSelectionBackground());
					setForeground(table.getSelectionForeground());
				} else {
					setBackground(table.getBackground());
					setForeground(table.getForeground());
				}
				setFont(table.getFont());
				Border border;
				int verticalMargin = table.getRowHeight(row) - table.getFont().getSize();
				int margin = (rowHeight -table.getFont().getSize()) / 2;

				int bottomMargin = verticalMargin - margin;
				border = new EmptyBorder(margin, margin, bottomMargin, margin);
				setBorder(border);

				return this;
			}
		}
	}

	private static class DenotationHrebsTableModel extends AbstractTableModel {

		private final GuiDenotationHrebsModel model;

		public DenotationHrebsTableModel(GuiDenotationHrebsModel model) {
			this.model = model;
		}

		@Override
		public int getRowCount() {
			return model.getHrebsCount();
		}

		@Override
		public int getColumnCount() {
			return Math.max(Math.max(SPIKE_NUMBER_COLUMN, SPIKE_SIZE_COLUMN), SPIKE_WORDS_COLUMN) + 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Hreb hreb = model.getHrebOnRow(rowIndex);
			if (hreb == null) {
				return null;
			}
			switch (columnIndex) {
				case SPIKE_NUMBER_COLUMN:
					return hreb.getNumber();
				case SPIKE_SIZE_COLUMN:
					return hreb.getWords().size();
				case SPIKE_WORDS_COLUMN:
					return model.toStringForHreb(hreb);
				default:
					return null;
			}
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
				case SPIKE_NUMBER_COLUMN:
					return "Číslo hřebu";
				case SPIKE_SIZE_COLUMN:
					return "Velikost hřebu";
				case SPIKE_WORDS_COLUMN:
					return "Slova ve hřebu";
				default:
					return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
				case SPIKE_NUMBER_COLUMN:
				case SPIKE_SIZE_COLUMN:
					return Integer.class;
				case SPIKE_WORDS_COLUMN:
					return String.class;
				default:
					return Object.class;
			}
		}


	}

	private static class MessagesUtils {

		private static boolean notifyIsInElement(Component parent, DenotationElement element) {
			final int yesNo = JOptionPane.showConfirmDialog(parent,
				"Denotační element " + element + " je ve hřebu č. " + element.getHreb() + ". Budete-li pokračovat, bude z tohoto hřebu odstraněn.\n\nChcete pokračovat?",
				"Odstranit denotační element?",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			return yesNo == JOptionPane.YES_OPTION;
		}

		private static  boolean notifyIsInHreb(Component parent, GuiDenotationWord word) {
			final int yesNo = JOptionPane.showConfirmDialog(parent,
				"Slovo '" + word + "' je ve hřebech č. " + word.getDenotationWord().getHrebs() + ". Budete-li pokračovat, bude z těchto hřebů odstraněno.\n\nChcete pokračovat?",
				"Odstranit slovo ze hřebů?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			return yesNo == JOptionPane.YES_OPTION;
		}


		private static String getDividedValue(Component parent, DenotationWord word, String input) {
			String msg = "Zadejte prosím [pomocí ( a )], které část slova '" + word.getWords() + "' nepatří do hřebu.";
			if (input != null) {
				msg += "\n\nDo textu vepiště pouze znak '(' a ')', tak aby v závorkách byla část slova, která do hřebu nepatří";
			}
			return JOptionPane.showInputDialog(parent, msg, word.getWords());
		}

		private static boolean checkInput(String input, String word) {
			if (word.length() >= input.length()) {
				return false;
			}

			StringBuilder  inputSb = new StringBuilder();
			StringBuilder wordSb = new StringBuilder();
			removeBraces(input, word, inputSb, wordSb);
			input = inputSb.toString();
			word = wordSb.toString();
			//..check if there is one (, one ) and ( is before )
			if (StringUtils.countMatches(input, ")") ==  1
				&& StringUtils.countMatches(input, "(") == 1) {

				int openingBracket = input.indexOf('(');
				int closingBracket = input.indexOf(')');
				if (openingBracket < closingBracket
					&& (closingBracket - openingBracket) > 1) {

					//..check if input without braces is the same as word
					return input.replace("(", "").replace(")", "").equals(word);
				}

			}
			return false;
		}

		/**
		 * Removes ( and ) from {@code input} and {@code word} if the brace is on the same position in both words.
		 *
		 * <p>
		 *     word=Succ(ess)
		 *     input=(S)ucc(ess)
		 *     wordSb=Success
		 *     inputSb=(S)uccess
		 */
		private static void removeBraces(String input, String word, StringBuilder inputSb, StringBuilder wordSb) {
			int wordDiff = 0;
			char inputChar, wordChar;
			final int wordLength = word.length();
			for (int i = 0; i < input.length(); i++) {
				inputChar = input.charAt(i);
				int wordCharIndex = i - wordDiff;
				if (wordCharIndex < wordLength) {
					wordChar = word.charAt(wordCharIndex);
				} else {
					wordChar = 0;
				}
				inputSb.append(inputChar);

				if (wordChar > 0) {
					if ((inputChar == '(' || inputChar == ')')
						&& (wordChar != '(' && wordChar != ')')) {
						//..new bracket in input
						wordDiff++;
					} else {
						wordSb.append(wordChar);
					}
				}
			}
		}
	}
}
