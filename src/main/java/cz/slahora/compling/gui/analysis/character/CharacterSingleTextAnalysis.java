package cz.slahora.compling.gui.analysis.character;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.frequency.character.CharacterFrequencyRule;
import cz.compling.analysis.analysator.frequency.character.ICharacterFrequency;
import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.panels.characters.CharacterFrequencyPanel;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.util.Collections;
import java.util.Map;

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
	private WorkingText text;

	@Override
	public void analyse(JPanel mainPanel, CompLing compLing, WorkingText text) {
		this.text = text;

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
	public Results getResults() {
		return new CharacterSingleTextAnalysisResults(text, characterFrequency);
	}

	private static class CharacterSingleTextAnalysisResults implements Results {
		private Map<WorkingText, CharacterFrequency> map;

		public CharacterSingleTextAnalysisResults(WorkingText text, CharacterFrequency characterFrequency) {
			map = Collections.singletonMap(text, characterFrequency);
		}

		@Override
		public ResultsPanel getResultPanel() {
			return new CharacterFrequencyPanel(map);
		}
	}
}
