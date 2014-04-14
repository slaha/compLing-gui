package cz.slahora.compling.gui.analysis.denotation;

import cz.slahora.compling.gui.analysis.CsvExporter;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.LastDirectory;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.ColumnsAutoSizer;
import cz.slahora.compling.gui.utils.FileChooserUtils;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
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
public class DenotationAnalysis {

	public static final int SPIKE_NUMBER_COLUMN = 0;
	public static final int SPIKE_SIZE_COLUMN = 1;
	public static final int SPIKE_WORDS_COLUMN = 2;

	public static class DenotationPanel extends JPanel implements ActionListener {

		private final JButton exportBtn;
		private final JButton importBtn;
		private final WorkingText workingText;

		private DenotationModel model;
		private DenotationSpikesPanel denotationSpikesPanel;
		private DenotationPoemPanel denotationPoemPanel;

		public DenotationPanel(WorkingText workingText) {
			super(new GridBagLayout());

			this.workingText = workingText;

			JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
			toolBar.setFloatable(false);
			exportBtn = new JButton("Export");
			importBtn = new JButton("Import");
			importBtn.addActionListener(this);
			exportBtn.addActionListener(this);
			toolBar.add(importBtn);
			toolBar.add(exportBtn);
			GridBagConstraints gbc = new GridBagConstraintBuilder().gridxy(0, 0).fill(GridBagConstraints.HORIZONTAL).weightx(1).build();
			add(toolBar, gbc);

			setModel(new DenotationModel(workingText));
		}

		private void setModel(DenotationModel denotationModel) {
			this.model = denotationModel;
			remove(denotationPoemPanel);
			remove(denotationSpikesPanel);

			denotationPoemPanel = new DenotationPoemPanel(model, this);
			denotationSpikesPanel = new DenotationSpikesPanel(model.getSpikesModel(), this);

			final JSplitPane bottomPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(denotationPoemPanel), new JScrollPane(denotationSpikesPanel));


			GridBagConstraints gbc = new GridBagConstraintBuilder().gridxy(0, 1).fill(GridBagConstraints.BOTH).weightx(1).weighty(1).build();
			add(bottomPanel, gbc);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					bottomPanel.setDividerLocation(denotationPoemPanel.getWidth() + 50);
				}
			});
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source = e.getSource();
			LastDirectory lastDir = LastDirectory.getInstance();
			File lastDirectory = lastDir.getLastDirectory();
			if (source == exportBtn) {
				CsvExporter exporter = new CsvExporter(model.getCsvSaver().saveToCsv(model));
				File csvFile = FileChooserUtils.getFileToSave(lastDirectory, this, "csv");
				if (csvFile == null) {
					return;
				} else if (csvFile.exists()) {
					int i = JOptionPane.showConfirmDialog(getParent(), "Soubor " + csvFile.getName() + " již existuje. Chcete jej přepsat?", "Soubor již existuje", JOptionPane.YES_NO_OPTION);
					if (i != JOptionPane.YES_OPTION) {
						return;
					}
				}
				try {
					exporter.export(csvFile);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(getParent(), "Chyba při ukládání souboru " + csvFile.getName(), "Chyba", JOptionPane.ERROR_MESSAGE);
				} finally {
					lastDir.setLastDirectory(csvFile.getParentFile());
				}
			}
			else if (source == importBtn) {
				File file = FileChooserUtils.getFileToOpen(lastDirectory, this, "csv");
				if (file == null) {
					return;
				}
				try {
					List<String> lines = IOUtils.readLines(new FileInputStream(file));
					CsvData csvData = new CsvData(lines);
					Csv.CsvLoader<DenotationModel> csvLoader = this.model.getCsvLoader();
					DenotationModel newModel = new DenotationModel(workingText);
					csvLoader.loadFromCsv(csvData, newModel);
					setModel(newModel);

				} catch (IOException ex) {
					JOptionPane.showMessageDialog(getParent(), "Chyba při čtení souboru " + file.getName(), "Chyba", JOptionPane.ERROR_MESSAGE);
				} catch (ParseException pe) {
					JOptionPane.showMessageDialog(getParent(), "Chyba při parsování souboru " + file.getName(), "Chyba", JOptionPane.ERROR_MESSAGE);
					pe.printStackTrace();
				} finally {
					lastDir.setLastDirectory(file.getParentFile());
				}
			}

		}

		public void refreshSpikes(int number) {
			denotationSpikesPanel.refresh(number);

		}

		public void refreshPoems(int number) {
			denotationPoemPanel.refresh(number);
		}
	}

	private static class DenotationPoemPanel extends JPanel {

		private final TIntObjectMap<WordPanel> wordPanels;

		private final DenotationPanel denotationPanel;
		private final DenotationSpikesModel spikesModel;

		public DenotationPoemPanel(DenotationModel denotationModel, DenotationPanel denotationPanel) {
			super(new GridBagLayout());

			setBackground(Color.white);

			this.denotationPanel = denotationPanel;
			this.spikesModel = denotationModel.getSpikesModel();
			DenotationPoemModel poemModel = denotationModel.getPoemModel();

			this.wordPanels = new TIntObjectHashMap<WordPanel>();

			final int countOfStrophes = poemModel.getCountOfStrophes();
			for (int i = 1; i <= countOfStrophes; i++) {
				DenotationPoemModel.DenotationStrophe strophe = poemModel.getStrophe(i);
				JPanel strophePanel = new JPanel(new GridBagLayout());
				strophePanel.setBackground(Color.white);
				int verseNumber = 0;
				for (DenotationPoemModel.DenotationVerse verse : strophe.verses) {
					JPanel versePanel = new JPanel(new GridBagLayout());

					for (DenotationPoemModel.DenotationWord word : verse.words) {
						WordPanel wordPanel = new WordPanel(poemModel, word.getNumber(), this, spikesModel);
						wordPanels.put(word.getNumber(), wordPanel);
						versePanel.add(wordPanel, BUILDER.copy().build());
					}
					strophePanel.add(versePanel, BUILDER.copy().gridy(verseNumber++).build());
				}
				add(strophePanel, BUILDER.copy().insets(STROPHE_INSETS).gridxy(0, i - 1).build());
			}
		}

		private static final Insets STROPHE_INSETS = new Insets(1, 1, 25, 1);
		private static final GridBagConstraintBuilder BUILDER = new GridBagConstraintBuilder().anchor(GridBagConstraints.LINE_START);

		public void refresh(WordPanel wordPanel) {
			refresh(wordPanel.word.getNumber());
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

		public void refreshSpikes(int number) {
			denotationPanel.refreshSpikes(number);
		}
	}
	private static class WordPanel extends JPanel {

		private static final Insets INSETS = new Insets(1, 7, 1, 1);
		private static final Insets IGNORED_INSETS = new Insets(1, 0, 1, 0);

		final JLabel numberLabel, wordLabel;
		final DenotationPoemPanel panel;
		final Font _font;

		private DenotationPoemModel.DenotationWord word;
		private final DenotationPoemModel model;
		private final int wordNumber;

		public WordPanel(DenotationPoemModel model, int number, DenotationPoemPanel panel, DenotationSpikesModel spikesModel) {
			super(new GridBagLayout());
			setBackground(Color.white);
			setBorder(new EmptyBorder(INSETS));

			this.model = model;
			this.wordNumber = number;
			this.word = model.getWord(number);
			this.panel = panel;

			numberLabel = new JLabel(word.getElements().toString());
			wordLabel = new JLabel(word.getWords().toString());
			_font = wordLabel.getFont();

			add(numberLabel, new GridBagConstraintBuilder().gridxy(0, 0).anchor(GridBagConstraints.NORTH).build());
			add(wordLabel, new GridBagConstraintBuilder().gridxy(0, 1).anchor(GridBagConstraints.SOUTH).build());

			addMouseListener(new MouseAdapter());

			setComponentPopupMenu(new WordPanelPopup(spikesModel));
		}

		private void changeFont(boolean hovered) {
			final Font font;
			if (hovered) {
				font = _font.deriveFont(Font.BOLD);
			} else {
				font = _font.deriveFont(Font.PLAIN);
			}
			Map<TextAttribute, Object> attributes;
			if (word.getWords().size() > 1) {
				attributes = new HashMap<TextAttribute, Object>();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			} else {
				attributes = null;
			}
			final Font underlineFont =  font.deriveFont(attributes);
			final Color wordColor, numberColor;
			if (word.isIgnored()) {
				numberColor = wordColor = Color.gray;

			} else if (!word.hasFreeElement()) {
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
			DenotationPoemModel.DenotationWord denotationWord = model.getWord(wordNumber);
			if (word != denotationWord) {
				word = denotationWord;

			}
			String txt;
			txt = word.getElements().toString();
			numberLabel.setText(StringUtils.isEmpty(txt) ? " " : txt);

			txt = word.getWords().toString();
			wordLabel.setText(StringUtils.isEmpty(txt) ? " " : txt);
			setBorder(new EmptyBorder(word.isJoined() ? IGNORED_INSETS: INSETS));
			changeFont(false);
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
			public static final String SPIKES_ADD_SUBMENU = "spikes_add_submenu";
			public static final String SPIKES_REMOVE_SUBMENU = "spikes_remove_submenu";
			public static final String SPIKE_KEY = "spike";

			private final DenotationSpikesModel spikesModel;

			private final JMenuItem ignore, addElement, removeElement, join, split;
			private final JMenu spikesAddMenu, spikesRemoveMenu;

			private WordPanelPopup(DenotationSpikesModel spikesModel) {
				this.spikesModel = spikesModel;

				ignore = new JMenuItem("Ignorovat");
				addElement = new JMenuItem("Přidat denotační element");
				removeElement = new JMenuItem("Odebrat denotační element");
				join = new JMenuItem("Sloučit s " + (word.getNextWord() != null ? word.getNextWord().getWords() : ""));
				split = new JMenuItem("Oddělit " + (word.hasJoined() ? word.getLastJoined() :""));

				spikesAddMenu = new JMenu("Přidat do hřebu");
				spikesRemoveMenu = new JMenu("Odebrat z hřebu");

				ignore.addActionListener(this);
				addElement.addActionListener(this);
				removeElement.addActionListener(this);
				join.addActionListener(this);
				split.addActionListener(this);

				onWordIgnored(word.isIgnored());

				addPopupMenuListener(new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						onWordIgnored(word.isIgnored());
						onElementChanged();
						onWordJoined(word.getNextWord());
						loadSpikes();
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
					word.setIgnored(!word.isIgnored());

				} else if (source == addElement) {
					word.addElement();

				} else if (source == removeElement) {
					word.removeElement();

				} else if (source == join) {
					word.joinNext();

				} else if (source == split) {
					word.splitLast();

				} else if (source instanceof JMenuItem) {
					JMenuItem menuItem = (JMenuItem) source;
					if (SPIKES_ADD_SUBMENU.equals(menuItem.getName())) {

						DenotationSpikesModel.Spike spike = (DenotationSpikesModel.Spike) menuItem.getClientProperty(SPIKE_KEY);
						spike.add(word);
						DenotationPoemModel.DenotationSpikeNumber spikeNumber = word.getFreeElement();
						spikeNumber.onAddToSpike(spike);
						WordPanel.this.setToolTipText("Patří do hřebu č. " + word.getSpikes());
						panel.refreshSpikes(spike.getNumber());
					} else if (SPIKES_REMOVE_SUBMENU.equals(menuItem.getName())) {
						DenotationSpikesModel.Spike spike = (DenotationSpikesModel.Spike) menuItem.getClientProperty(SPIKE_KEY);
						spike.remove(word);
						DenotationPoemModel.DenotationSpikeNumber spikeNumber = word.getElementInSpike(spike);
						spikeNumber.onRemoveFromSpike(spike);
						if (word.isInSpike()) {
							WordPanel.this.setToolTipText("Patří do hřebu č. " + word.getSpikes());
						} else {
							WordPanel.this.setToolTipText(null);
						}
						panel.refreshSpikes(spike.getNumber());
					}
				}
				panel.refresh(WordPanel.this);
			}

			private void loadSpikes() {
				spikesAddMenu.removeAll();
				spikesRemoveMenu.removeAll();
				int menuPosition = 0;
				if (spikesModel.hasSpikes() && !word.isIgnored() && word.hasFreeElement()) {
					for (DenotationSpikesModel.Spike spike : spikesModel.getSpikes()) {
						JMenuItem spikeItem = new JMenuItem("Přidat do hřebu č. " + spike.getNumber());
						spikeItem.setName(SPIKES_ADD_SUBMENU);
						spikeItem.putClientProperty(SPIKE_KEY, spike);
						spikeItem.addActionListener(this);
						spikesAddMenu.add(spikeItem);
					}
					add(spikesAddMenu, menuPosition++);
				} else {
					remove(spikesAddMenu);
				}
				if (spikesModel.hasSpikes() && !word.isIgnored() && word.isInSpike()) {
					for (DenotationSpikesModel.Spike spike : word.getSpikes()) {
						JMenuItem spikeItem = new JMenuItem("Odebrat z hřebu č. " + spike.getNumber());
						spikeItem.setName(SPIKES_REMOVE_SUBMENU);
						spikeItem.putClientProperty(SPIKE_KEY, spike);
						spikeItem.addActionListener(this);
						spikesRemoveMenu.add(spikeItem);
					}
					add(spikesRemoveMenu, menuPosition);
				} else {
					remove(spikesRemoveMenu);
				}
			}

			private void onElementChanged() {
				if (word.canRemoveElement()) {
					add(removeElement, 2);
				} else {
					remove(removeElement);
				}
			}

			private void onWordJoined(DenotationPoemModel.DenotationWord nextWord) {
				if (nextWord == null || word.isIgnored())  {
					remove(join);
				} else {
					join.setText("Sloučit s " + (word.getNextWord() != null ? word.getNextWord().getWords() : ""));
					add(join);

					onElementChanged();
				}
				if (word.hasJoined()) {
					split.setText("Oddělit " + (word.hasJoined() ? word.getLastJoined() :""));
					add(split);
				}
			}

			private void onWordIgnored(boolean ignored) {
				add(ignore, 0);
				if (ignored) {
					ignore.setText("Brát v potaz");
					remove(addElement);
					remove(removeElement);
					remove(join);
				} else {
					ignore.setText("Ignorovat");
					add(addElement, 1);
					onElementChanged();
					onWordJoined(word.getNextWord());
				}
			}
		}
	}

	private static class DenotationSpikesPanel extends JPanel implements ActionListener {
		private final DenotationSpikesModel model;
		private final DenotationSpikesTableModel tableModel;

		private final JButton addBtn;
		private final JButton removeBtn;
		private final JTable table;
		private final DenotationPanel panel;

		public DenotationSpikesPanel(DenotationSpikesModel model, DenotationPanel denotationPanel) {
			super(new GridBagLayout());

			this.panel = denotationPanel;
			this.model = model;
			this.tableModel = new DenotationSpikesTableModel(model);

			JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
			toolBar.setFloatable(false);
			addBtn = new JButton("Nový hřeb");
			removeBtn = new JButton("Odstranit hřeb");
			removeBtn.setEnabled(false);
			addBtn.addActionListener(this);
			removeBtn.addActionListener(this);
			toolBar.add(addBtn);
			toolBar.add(removeBtn);
			GridBagConstraintBuilder builder;
			builder = new GridBagConstraintBuilder().gridxy(0, 0).fill(GridBagConstraints.HORIZONTAL).anchor(GridBagConstraints.NORTHWEST).weightx(1);
			add(toolBar, builder.build());


			table = new JTable(tableModel) {

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

			builder = new GridBagConstraintBuilder().gridxy(0, 1).fill(GridBagConstraints.HORIZONTAL).anchor(GridBagConstraints.NORTHWEST).weightx(1);
			add(table.getTableHeader(), builder.build());

			builder = new GridBagConstraintBuilder().gridxy(0, 2).fill(GridBagConstraints.HORIZONTAL).anchor(GridBagConstraints.NORTHWEST).weightx(1).weighty(1);
			add(table, builder.copy().build());

			ColumnsAutoSizer.sizeColumnsToFit(table, 10, SPIKE_WORDS_COLUMN);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source = e.getSource();
			if (source == addBtn) {
				model.addNewSpike();
			} else if (source == removeBtn) {
				Object selected = table.getValueAt(table.getSelectedRow(), 0);
				if (selected != null) {
					int lowestWordNumber = model.removeSpike(Integer.parseInt(selected.toString()));
					panel.refreshPoems(lowestWordNumber);
				}
			}
			tableModel.fireTableDataChanged();
		}

		public void refresh(int number) {
			int row = findRowForSpike(number);
			if (row >= 0) {
				tableModel.fireTableRowsUpdated(row, row);
			}
		}

		private int findRowForSpike(int number) {
			for (int row = 0; row < table.getRowCount(); row++) {
				Object valueAt = table.getValueAt(row, 0);
				if (valueAt != null) {
					int i = Integer.parseInt(valueAt.toString());
					if (i == number) {
						return row;
					}
				}
			}
			throw new IllegalStateException("No Spike with number " + number + " found in the table");
		}

		private class MultilineCellRenderer extends JTextArea implements TableCellRenderer {

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
						table.setRowHeight(row, getWrappedLines() * rowHeight);
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

		private class SingleLineCellRenderer extends JLabel implements TableCellRenderer {

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

	private static class DenotationSpikesTableModel extends AbstractTableModel {

		private final DenotationSpikesModel model;

		public DenotationSpikesTableModel(DenotationSpikesModel model) {
			this.model = model;
		}

		@Override
		public int getRowCount() {
			return model.getSpikesCount();
		}

		@Override
		public int getColumnCount() {
			return Math.max(Math.max(SPIKE_NUMBER_COLUMN, SPIKE_SIZE_COLUMN), SPIKE_WORDS_COLUMN) + 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			DenotationSpikesModel.Spike spike = model.getSpikeOnRow(rowIndex);
			if (spike == null) {
				return null;
			}
			switch (columnIndex) {
				case SPIKE_NUMBER_COLUMN:
					return spike.getNumber();
				case SPIKE_SIZE_COLUMN:
					return spike.getWords().size();
				case SPIKE_WORDS_COLUMN:
					return spike.getWords();
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
}
