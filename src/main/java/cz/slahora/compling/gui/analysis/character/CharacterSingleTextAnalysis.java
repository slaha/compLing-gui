package cz.slahora.compling.gui.analysis.character;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.frequency.character.CharacterFrequencyRule;
import cz.compling.analysis.analysator.frequency.character.ICharacterFrequency;
import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingText;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 8:39</dd>
 * </dl>
 */
public class CharacterSingleTextAnalysis extends AbstractCharacterAnalysis implements SingleTextAnalysis<CharacterFrequency> {

	private CharacterFrequency characterFrequency;

	@Override
	public void analyse(JPanel mainPanel, CompLing compLing, WorkingText text) {
		OptionPanel optionPanel = new OptionPanel();
		int result = JOptionPane.showConfirmDialog(mainPanel, optionPanel, "Nastavení analýzy četnosti znaků pro " + text.getName(), JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		ICharacterFrequency characterFrequency = compLing.generalAnalysis().characterFrequency();
		if (optionPanel.lettersOnly()) {
			compLing.registerRule(new OnlyLettersRule());
		}
		if (!optionPanel.caseSensitive()) {
			compLing.registerRule(new CaseInsensitiveRule());
		}
		for (CharacterFrequencyRule rule : optionPanel.replaceRules()) {
			characterFrequency.registerRule(rule);
		}
		this.characterFrequency = characterFrequency.getCharacterFrequency();
	}

	@Override
	public CharacterFrequency getResults() {
		return characterFrequency;
	}
}
