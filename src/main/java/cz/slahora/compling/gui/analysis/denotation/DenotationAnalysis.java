package cz.slahora.compling.gui.analysis.denotation;

import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
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

	public static class DenotationPanel extends JPanel {

		public DenotationPanel(WorkingText workingText) {
			super(new GridBagLayout());

			DenotationPoemModel model = new DenotationPoemModel(workingText);
			GridBagConstraints gbc = new GridBagConstraintBuilder().gridxy(0, 0).fill(GridBagConstraints.BOTH).weightx(1).weighty(1).build();
			add(new JScrollPane(new DenotationPoemPanel(model)), gbc);
		}

	}

	private static class DenotationPoemPanel extends JPanel {

		private final TIntObjectMap<WordPanel> wordPanels;

		public DenotationPoemPanel(DenotationPoemModel model) {
			super(new GridBagLayout());

			this.wordPanels = new TIntObjectHashMap<WordPanel>();

			final int countOfStrophes = model.getCountOfStrophes();
			for (int i = 1; i <= countOfStrophes; i++) {
				DenotationPoemModel.DenotationStrophe strophe = model.getStrophe(i);
				JPanel strophePanel = new JPanel(new GridBagLayout());
				int verseNumber = 0;
				for (DenotationPoemModel.DenotationVerse verse : strophe.verses) {
					JPanel versePanel = new JPanel(new GridBagLayout());

					for (DenotationPoemModel.DenotationWord word : verse.words) {
						WordPanel wordPanel = new WordPanel(word, this);
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
			final int _number = wordPanel.word.getNumber();

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
	}
	private static class WordPanel extends JPanel {

		private static final Insets INSETS = new Insets(1, 7, 1, 1);
		private static final Insets IGNORED_INSETS = new Insets(1, 0, 1, 0);

		final JLabel[] labels;
		final JLabel numberLabel, wordLabel;
		final DenotationPoemPanel panel;
		final Font _font;

		private final DenotationPoemModel.DenotationWord word;

		public WordPanel(DenotationPoemModel.DenotationWord word, DenotationPoemPanel panel) {
			super(new GridBagLayout());

			setBorder(new EmptyBorder(INSETS));

			this.word = word;
			this.panel = panel;

			numberLabel = new JLabel(word.getElements().toString());
			wordLabel = new JLabel(word.getWords().toString());
			labels = new JLabel[]{wordLabel, numberLabel};
			_font = wordLabel.getFont();

			add(numberLabel, new GridBagConstraintBuilder().gridxy(0, 0).anchor(GridBagConstraints.NORTH).build());
			add(wordLabel, new GridBagConstraintBuilder().gridxy(0, 1).anchor(GridBagConstraints.SOUTH).build());

			addMouseListener(new MouseAdapter());

			setComponentPopupMenu(new WordPanelPopup());
		}

		private void changeFont(boolean hovered) {
			final Font font;
			if (hovered) {
				font = _font.deriveFont(Font.BOLD);
			} else {
				font = _font.deriveFont(Font.PLAIN);
			}
			Map attributes;
			if (word.getWords().size() > 1) {
				attributes = new HashMap();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			} else {
				attributes = null;
			}
			final Font underlineFont =  font.deriveFont(attributes);
			final Color color;
			if (word.isIgnored()) {
				color = Color.gray;
			} else {
				color = Color.black;
			}
			for (JLabel label : labels) {
				if (label == wordLabel) {
					label.setFont(underlineFont);
				} else {
					label.setFont(font);
				}
				label.setForeground(color);
			}
		}

		public void refresh() {
			numberLabel.setText(word.getElements().toString());
			wordLabel.setText(word.getWords().toString());
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
			final JMenuItem ignore, addElement, removeElement, join, split;
			private WordPanelPopup() {
				ignore = new JMenuItem("Ignorovat");
				addElement = new JMenuItem("Přidat denotační element");
				removeElement = new JMenuItem("Odebrat denotační element");
				join = new JMenuItem("Sloučit s " + (word.getNextWord() != null ? word.getNextWord().getWords() : ""));
				split = new JMenuItem("Oddělit " + (word.hasJoined() ? word.getLastJoined() :""));

				ignore.addActionListener(this);
				addElement.addActionListener(this);
				removeElement.addActionListener(this);
				join.addActionListener(this);
				split.addActionListener(this);

				onWordIgnored(word.isIgnored());
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (source == ignore) {
					word.setIgnored(!word.isIgnored());
					changeFont(false); //..set to gray or black
					onWordIgnored(word.isIgnored());
				} else if (source == addElement) {
					word.addElement();
					onElementChanged();

				} else if (source == removeElement) {
					word.removeElement();
					onElementChanged();

				} else if (source == join) {
					if (word.joinNext()) {
						onWordJoined(word.getNextWord());
					}
				} else if (source == split) {
					word.splitLast();
					onWordJoined(word.getNextWord());
				}
				panel.refresh(WordPanel.this);
			}

			private void onElementChanged() {
				if (word.canRemoveElement()) {
					add(removeElement, 2);
				} else {
					remove(removeElement);
				}

			}

			private void onWordJoined(DenotationPoemModel.DenotationWord nextWord) {
				if (nextWord == null)  {
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
}
