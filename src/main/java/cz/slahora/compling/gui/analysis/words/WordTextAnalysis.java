package cz.slahora.compling.gui.analysis.words;

import cz.compling.text.Text;
import cz.compling.text.TextModificationRule;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;

import javax.swing.JCheckBox;
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

		public OptionsPanel() {
			super(new GridBagLayout());

			caseSensitive = new JCheckBox("Ignorovat rozdílnou velikost písmen ('SloVo' bude stejné jako 'slovo'");
			add(caseSensitive, new GridBagConstraintBuilder().gridy(1).anchor(GridBagConstraints.LINE_START).build());

		}

		public boolean applyCaseInsensitiveRule() {
			return caseSensitive.isSelected();
		}
	}

	protected static class CaseInsensitiveRule implements TextModificationRule {

		@Override
		public String modify(Text text) {
			return text.getPlainText().toLowerCase(Locale.getDefault());
		}
	}
}
