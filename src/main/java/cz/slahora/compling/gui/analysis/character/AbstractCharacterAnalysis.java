package cz.slahora.compling.gui.analysis.character;

import cz.compling.analysis.analysator.frequency.character.CharacterFrequencyRule;
import cz.compling.text.Text;
import cz.compling.text.TextModificationRule;
import cz.compling.utils.Reference;
import cz.slahora.compling.gui.analysis.RulesTable;
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

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 27.3.14 20:54</dd>
 * </dl>
 */
public abstract class AbstractCharacterAnalysis {

	protected static class OptionPanel extends JPanel {

		private List<RulesTable.RuleHolder<CharacterFrequencyRule>> replaceRules;
		private JCheckBox caseSensitive;
		private JCheckBox onlyLetters;

		protected OptionPanel() {
			super(new GridBagLayout());
			replaceRules = new ArrayList<RulesTable.RuleHolder<CharacterFrequencyRule>>();
			onlyLetters = new JCheckBox("Analyzovat pouze písmena");
			add(onlyLetters, new GridBagConstraintBuilder().gridY(0).anchor(GridBagConstraints.LINE_START).build());

			caseSensitive = new JCheckBox("Rozlišovat malá a VELKÁ písmena");
			add(caseSensitive, new GridBagConstraintBuilder().gridY(1).anchor(GridBagConstraints.LINE_START).build());

			JPanel buttonsPanel = new JPanel(new GridBagLayout());
			final JButton plusBtn = new JButton("+");
			buttonsPanel.add(plusBtn, new GridBagConstraintBuilder().gridXY(0, 0).build());
			final JButton minusBtn = new JButton("-");
			minusBtn.setEnabled(false);
			buttonsPanel.add(minusBtn, new GridBagConstraintBuilder().gridXY(0, 1).build());

			final RulesTable.RulesTableModel model = new RulesTable.RulesTableModel<CharacterFrequencyRule>(replaceRules);
			final JTable jTable = new JTable(model);
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
						RulesTable.RuleHolder ruleHolder = createRuleHolder();
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

			add(replaceRulesPanel, new GridBagConstraintBuilder().gridXY(0, 2).anchor(GridBagConstraints.LINE_END).build());
		}

		private RulesTable.RuleHolder createRuleHolder() {
			CharacterFrequencyRulePanel panel = new CharacterFrequencyRulePanel();
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

		protected boolean caseSensitive() {
			return caseSensitive.isSelected();
		}

		protected boolean lettersOnly() {
			return onlyLetters.isSelected();
		}

		protected List<CharacterFrequencyRule> replaceRules() {
			List<CharacterFrequencyRule> rules = new ArrayList<CharacterFrequencyRule>(replaceRules.size());
			for (RulesTable.RuleHolder<CharacterFrequencyRule> replaceRule : replaceRules) {
				rules.add(replaceRule.getRule());
			}
			return Collections.unmodifiableList(rules);
		}

	}

	private static class CharacterFrequencyRulePanel extends JPanel {

		public String mWhatToFind;
		public String mReplaceWith;
		public RulesTable.RuleType mRuleType;

		private CharacterFrequencyRulePanel() {
			super(new GridBagLayout());

			JLabel whatToFindLbl = new JLabel("Nahrazovaný znak:");
			JLabel whatWithItLbl = new JLabel("Nahrazený znak:");

			final JRadioButton ignore = new JRadioButton("Ignorovat");
			final JRadioButton asOneCharacter = new JRadioButton("Jako jeden znak");
			final JRadioButton replaceWithButton = new JRadioButton("Nahradit za");

			ButtonGroup whatToDoGroup = new ButtonGroup();
			whatToDoGroup.add(ignore);
			whatToDoGroup.add(asOneCharacter);
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
						} else if (e.getSource() == asOneCharacter) {
							mRuleType = RulesTable.RuleType.AS_ONE_CHAR;
						} else {
							mRuleType = RulesTable.RuleType.REPLACE;
						}
						replaceWith.setEnabled(e.getSource() == replaceWithButton);
					}
				}
			};
			ignore.addItemListener(groupItemListener);
			asOneCharacter.addItemListener(groupItemListener);
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
			add(asOneCharacter, new GridBagConstraintBuilder().gridXY(0, y++).insets(insets).anchor(GridBagConstraints.LINE_START).build());
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
			return text.getPlainText().toUpperCase();
		}
	}

	protected static class OnlyLettersRule implements TextModificationRule {

		@Override
		public String modify(Text text) {
			String plainText = text.getPlainText();
			StringBuilder stringBuilder = new StringBuilder(plainText.length());
			for (char c : plainText.toCharArray()) {
				if (Character.isLetter(c)) {
					stringBuilder.append(c);
				}
			}
			return stringBuilder.toString();
		}
	}

	protected static class RuleHolderFactory {

		public RulesTable.RuleHolder create(RulesTable.RuleType ruleType, String whatToFind, String replaceWith) {
			CharacterFrequencyRule rule;
			String whatToFindDesc, replaceWithDesc;
			switch (ruleType) {
				case IGNORE:
					rule = createIgnore(whatToFind);
					replaceWithDesc = "Ignorovat";
					break;
				case AS_ONE_CHAR:
					rule = createOneChar(whatToFind);
					replaceWithDesc = "Jako jeden znak";
					break;
				case REPLACE:
					rule = createReplace(whatToFind, replaceWith);
					replaceWithDesc = replaceWith;
					break;
				default:
					throw new IllegalArgumentException("RuleType " + ruleType + " is unknown. No rule can be created");
			}
			whatToFindDesc = whatToFind;
			return new RulesTable.RuleHolder<CharacterFrequencyRule>(rule, whatToFindDesc, replaceWithDesc);
		}

		private CharacterFrequencyRule createReplace(final String whatToFind, final String replaceWith) {
			return new CharacterFrequencyRule() {
				@Override
				public boolean modify(String plainText, Reference<String> putToMap, Reference<Integer> position) {
					String substring = substring(plainText, position.value, whatToFind);
					if (whatToFind.equalsIgnoreCase(substring)) {
						putToMap.value = replaceWith;
						position.value += substring.length() - 1;
						return true;
					}
					return false;
				}
			};
		}

		private CharacterFrequencyRule createOneChar(final String whatToFind) {
			return new CharacterFrequencyRule() {
				@Override
				public boolean modify(String plainText, Reference<String> putToMap, Reference<Integer> position) {
					String substring = substring(plainText, position.value, whatToFind);
					if (whatToFind.equalsIgnoreCase(substring)) {
						putToMap.value = substring;
						position.value += substring.length() - 1;
						return true;
					}
					return false;
				}
			};
		}

		private CharacterFrequencyRule createIgnore(final String whatToFind) {
			return new CharacterFrequencyRule() {
				@Override
				public boolean modify(String plainText, Reference<String> putToMap, Reference<Integer> position) {
					String substring = substring(plainText, position.value, whatToFind);
					if (whatToFind.equalsIgnoreCase(substring)) {
						putToMap.value = null;
						position.value += substring.length() - 1;
						return true;
					}
					return false;
				}
			};
		}

		private String substring(String plainText, int position, String whatToFind) {
			int whatToFindEnd = position + whatToFind.length();
			if (whatToFindEnd > plainText.length()) {
				return null;
			}
			return plainText.substring(position, whatToFindEnd);
		}
	}
}
