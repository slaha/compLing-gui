package cz.slahora.compling.gui.analysis.words;

import cz.compling.text.Text;
import cz.compling.text.TextModificationRule;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

		public OptionsPanel() {
			super(new GridBagLayout());

			type = new JComboBox(WordTextAnalysisType.values());
			add(new JLabel("Typ analýzy"), new GridBagConstraintBuilder().gridXY(0,0).build());
			add(type, new GridBagConstraintBuilder().gridXY(1,0).build());

			caseSensitive = new JCheckBox("Ignorovat rozdílnou velikost písmen ('SloVo' bude stejné jako 'slovo'");
			add(caseSensitive, new GridBagConstraintBuilder().gridXY(0, 1).gridWidth(2).anchor(GridBagConstraints.LINE_START).build());

		}

		public boolean applyCaseInsensitiveRule() {
			return caseSensitive.isSelected();
		}

		public WordTextAnalysisType getAnalysisType() {
			return (WordTextAnalysisType)type.getSelectedItem();
		}
	}

	protected static class CaseInsensitiveRule implements TextModificationRule {

		@Override
		public String modify(Text text) {
			return text.getPlainText().toLowerCase(Locale.getDefault());
		}
	}
}
