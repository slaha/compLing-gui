package cz.slahora.compling.gui.analysis.words;

import cz.compling.analysis.analysator.frequency.words.WordFrequencyRule;
import cz.compling.text.Text;
import cz.compling.text.TextModificationRule;
import cz.compling.utils.Reference;
import cz.slahora.compling.gui.analysis.RulesTable;
import cz.slahora.compling.gui.analysis.assonance.NonEditableTable;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 2.8.14 12:51</dd>
 * </dl>
 */
abstract class WordTextAnalysis {

	protected static class OptionsPanel extends JPanel {

		private final JCheckBox caseSensitive;
		private final JComboBox type;
		private final List<RulesTable.RuleHolder<WordFrequencyRule>> replaceRules = new ArrayList<RulesTable.RuleHolder<WordFrequencyRule>>();

		public OptionsPanel() {
			super(new GridBagLayout());

			type = new JComboBox(WordTextAnalysisType.values());
			add(new JLabel("Typ analýzy"), new GridBagConstraintBuilder().gridXY(0,0).build());
			add(type, new GridBagConstraintBuilder().gridXY(1,0).build());

			caseSensitive = new JCheckBox("Ignorovat rozdílnou velikost písmen ('SloVo' bude stejné jako 'slovo'");
			add(caseSensitive, new GridBagConstraintBuilder().gridXY(0, 1).gridWidth(2).anchor(GridBagConstraints.LINE_START).build());

			JPanel buttonsPanel = new JPanel(new GridBagLayout());
			final JButton plusBtn = new JButton("+");
			buttonsPanel.add(plusBtn, new GridBagConstraintBuilder().gridXY(0, 0).build());
			final JButton minusBtn = new JButton("-");
			minusBtn.setEnabled(false);
			buttonsPanel.add(minusBtn, new GridBagConstraintBuilder().gridXY(0, 1).build());

			final RulesTable.RulesTableModel model = new RulesTable.RulesTableModel<WordFrequencyRule>(replaceRules);
			final JTable jTable = new NonEditableTable(model);
			jTable.setColumnSelectionAllowed(false);
			jTable.setCellSelectionEnabled(false);
			jTable.setRowSelectionAllowed(true);
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					minusBtn.setEnabled(e.getFirstIndex() >= 0);
				}
			});
			JScrollPane jsp = new JScrollPane(jTable) {
				@Override
				public Dimension getPreferredSize() {
					return new Dimension(500, 100);
				}
			};

			ActionListener plusMinusListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == plusBtn) {
						RulesTable.RuleHolder<WordFrequencyRule> ruleHolder = createRuleHolder();
						if (ruleHolder == null) {
							return;
						}
						replaceRules.add(ruleHolder);
					} else {
						replaceRules.remove(jTable.getSelectedRow());
						jTable.invalidate();
						minusBtn.setEnabled(!replaceRules.isEmpty());
					}
					model.fireTableDataChanged();
				}
			};
			plusBtn.addActionListener(plusMinusListener);
			minusBtn.addActionListener(plusMinusListener);

			JPanel replaceRulesPanel = new JPanel(new GridBagLayout());
			replaceRulesPanel.add(jsp, new GridBagConstraintBuilder().gridXY(0, 0).anchor(GridBagConstraints.LINE_START).fill(GridBagConstraints.HORIZONTAL).weightX(1).build());
			replaceRulesPanel.add(buttonsPanel, new GridBagConstraintBuilder().gridXY(1, 0).anchor(GridBagConstraints.LINE_END).build());

			add(replaceRulesPanel, new GridBagConstraintBuilder().gridXY(0, 2).gridWidth(2).anchor(GridBagConstraints.LINE_END).build());
		}

		private RulesTable.RuleHolder<WordFrequencyRule> createRuleHolder() {
			WordFrequencyRulePanel panel = new WordFrequencyRulePanel();
			int i = JOptionPane.showConfirmDialog(this, panel, "Parametry nového pravidla", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (i != JOptionPane.OK_OPTION) {
				return null;
			}
			else if (StringUtils.isBlank(panel.mWhatToFind)
				|| (panel.mRuleType == RulesTable.RuleType.REPLACE && StringUtils.isBlank(panel.mReplaceWith)))
			{
				JOptionPane.showMessageDialog(this, "Potřebné položky pro vytvoření pravidla nebyly správně vyplněny", "Chybné zadání", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			return new RuleHolderFactory().create(panel.mRuleType, panel.mWhatToFind, panel.mReplaceWith);
		}

		public boolean applyCaseInsensitiveRule() {
			return caseSensitive.isSelected();
		}

		public WordTextAnalysisType getAnalysisType() {
			return (WordTextAnalysisType)type.getSelectedItem();
		}

		public Iterable<? extends WordFrequencyRule> replaceRules() {
			if (replaceRules.isEmpty()) {
				return Collections.emptyList();
			}
			List<WordFrequencyRule> list = new ArrayList<WordFrequencyRule>(replaceRules.size());
			for (RulesTable.RuleHolder<WordFrequencyRule> rule : replaceRules) {
				list.add(rule.getRule());
			}
			return list;
		}


		private class RuleHolderFactory {

			public RulesTable.RuleHolder<WordFrequencyRule> create(RulesTable.RuleType ruleType, String whatToFind, String replaceWith) {
				WordFrequencyRule rule;
				String whatToFindDesc, replaceWithDesc;
				switch (ruleType) {
					case IGNORE:
						rule = createIgnore(whatToFind);
						replaceWithDesc = "Ignorovat";
						break;
					case REPLACE:
						rule = createReplace(whatToFind, replaceWith);
						replaceWithDesc = replaceWith;
						break;
					case AS_ONE_CHAR:
					default:
						throw new IllegalArgumentException("RuleType " + ruleType + " is unknown. No rule can be created");
				}
				whatToFindDesc = whatToFind;
				return new RulesTable.RuleHolder<WordFrequencyRule>(rule, whatToFindDesc, replaceWithDesc);
			}

			private WordFrequencyRule createReplace(final String whatToFind, final String replaceWith) {
				return new WordFrequencyRule() {
					@Override
					public boolean modify(Reference<String> word, Reference<Integer> length) {
						String v = StringUtils.replace(word.value, whatToFind, replaceWith);
						if (!v.equals(word.value)) {
							length.value = v.length();
							word.value = v;
							return true;
						}
						return false;
					}
				};
			}

			private WordFrequencyRule createIgnore(final String whatToFind) {
				return new WordFrequencyRule() {
					@Override
					public boolean modify(Reference<String> word, Reference<Integer> length) {
						if (word.value.equals(whatToFind)) {
							word.value = null;
							length.value = 0;
							return true;
						}
						return false;
					}
				};
			}
		}
	}

	private static class WordFrequencyRulePanel extends JPanel {
		public String mWhatToFind;
		public String mReplaceWith;
		public RulesTable.RuleType mRuleType;

		private WordFrequencyRulePanel() {
			super(new GridBagLayout());

			JLabel whatToFindLbl = new JLabel("Nahrazovaný znak:");
			JLabel whatWithItLbl = new JLabel("Nahrazený znak:");

			final JRadioButton ignore = new JRadioButton("Ignorovat");
//			final JRadioButton asOneCharacter = new JRadioButton("Jako jeden znak");
			final JRadioButton replaceWithButton = new JRadioButton("Nahradit za");

			ButtonGroup whatToDoGroup = new ButtonGroup();
			whatToDoGroup.add(ignore);
//			whatToDoGroup.add(asOneCharacter);
			whatToDoGroup.add(replaceWithButton);

			JTextField whatToFind = new JTextField();
			whatToFind.getDocument().addDocumentListener(new DocumentListener() {

				private void set(Document document, int length) {
					try {
						mWhatToFind = document.getText(0, length);
					} catch (BadLocationException e) {
						throw new RuntimeException("mWhatToFind - Location that does not exist in the document. Length: " + length );
					}
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					set(e.getDocument(), e.getDocument().getLength());
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					set(e.getDocument(), e.getDocument().getLength());
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					set(e.getDocument(), e.getDocument().getLength());
				}
			});

			final JTextField replaceWith = new JTextField();
			replaceWith.setEnabled(replaceWithButton.isSelected());
			replaceWith.getDocument().addDocumentListener(new DocumentListener() {


				private void set(Document document, int length) {
					try {
						mReplaceWith = document.getText(0, length);
					} catch (BadLocationException e) {
						throw new RuntimeException("mReplaceWith - Location that does not exist in the document. Length: " + length );
					}
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					set(e.getDocument(), e.getDocument().getLength());
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					set(e.getDocument(), e.getDocument().getLength());
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					set(e.getDocument(), e.getDocument().getLength());
				}
			});

			ItemListener groupItemListener = new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (e.getSource() == ignore) {
							mRuleType = RulesTable.RuleType.IGNORE;
//						} else if (e.getSource() == asOneCharacter) {
//							mRuleType = RulesTable.RuleType.AS_ONE_CHAR;
						} else {
							mRuleType = RulesTable.RuleType.REPLACE;
						}
						replaceWith.setEnabled(e.getSource() == replaceWithButton);
					}
				}
			};
			ignore.addItemListener(groupItemListener);
//			asOneCharacter.addItemListener(groupItemListener);
			replaceWithButton.addItemListener(groupItemListener);

			Insets insets = new Insets(0, 20, 0, 0);

			JPanel firstLine = new JPanel(new GridBagLayout());
			firstLine.add(whatToFindLbl, new GridBagConstraintBuilder().gridXY(0, 0).anchor(GridBagConstraints.LINE_START).build());
			firstLine.add(whatToFind, new GridBagConstraintBuilder().gridXY(1, 0).fill(GridBagConstraints.HORIZONTAL).weightX(1).insets(insets).anchor(GridBagConstraints.LINE_START).build());

			int y = 0;
			add(firstLine, new GridBagConstraintBuilder().gridXY(0, y).anchor(GridBagConstraints.LINE_START).fill(GridBagConstraints.HORIZONTAL).weightX(1).build());
			y++;
			add(whatWithItLbl, new GridBagConstraintBuilder().gridXY(0, y).anchor(GridBagConstraints.LINE_START).build());
			y++;
			add(ignore, new GridBagConstraintBuilder().gridXY(0, y++).insets(insets).anchor(GridBagConstraints.LINE_START).build());
//			add(asOneCharacter, new GridBagConstraintBuilder().gridXY(0, y++).insets(insets).anchor(GridBagConstraints.LINE_START).build());
			add(replaceWithButton, new GridBagConstraintBuilder().gridXY(0, y++).insets(insets).anchor(GridBagConstraints.LINE_START).build());

			add(replaceWith, new GridBagConstraintBuilder().gridXY(0, y).insets(insets).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.LINE_START).build());

			ignore.setSelected(true);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(500, 150);
		}
	}

	protected static class CaseInsensitiveRule implements TextModificationRule {

		@Override
		public String modify(Text text) {
			return text.getPlainText().toLowerCase(Locale.getDefault());
		}
	}
}
