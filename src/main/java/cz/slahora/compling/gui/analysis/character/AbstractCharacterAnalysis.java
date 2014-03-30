package cz.slahora.compling.gui.analysis.character;

import cz.compling.analysis.analysator.frequency.character.CharacterFrequencyRule;
import cz.compling.text.Text;
import cz.compling.text.TextModificationRule;
import cz.compling.utils.Reference;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;

import javax.swing.*;
import java.awt.*;

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

		JCheckBox caseSensitive;
		JCheckBox onlyLetters;

		protected OptionPanel() {
			super(new GridBagLayout());
			onlyLetters = new JCheckBox("Analyzovat pouze písmena");
			add(onlyLetters, new GridBagConstraintBuilder().gridy(0).anchor(GridBagConstraints.LINE_START).build());

			caseSensitive = new JCheckBox("Rozlišovat malá a VELKÁ písmena");
			add(caseSensitive, new GridBagConstraintBuilder().gridy(1).anchor(GridBagConstraints.LINE_START).build());
		}

		protected boolean caseSensitive() {
			return caseSensitive.isSelected();
		}

		protected boolean lettersOnly() {
			return onlyLetters.isSelected();
		}
	}


	protected static class CaseInsensitiveRule implements CharacterFrequencyRule {

		@Override
		public boolean modify(String plainText, Reference<String> putToMap, Reference<Integer> position) {
			char c = plainText.charAt(position.value);
			if (Character.isLowerCase(c)) {
				putToMap.value = String.valueOf(Character.toUpperCase(c));
				return true;
			}
			return false;
		}
	}

	protected class OnlyLettersRule implements TextModificationRule {

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
}
