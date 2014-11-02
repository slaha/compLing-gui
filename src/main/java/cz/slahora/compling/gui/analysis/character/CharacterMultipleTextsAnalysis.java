package cz.slahora.compling.gui.analysis.character;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.frequency.character.CharacterFrequencyRule;
import cz.compling.analysis.analysator.frequency.character.ICharacterFrequency;
import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.analysis.MultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.panels.characters.CharacterFrequencyPanel;
import cz.slahora.compling.gui.utils.MapUtils;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
 * <dd> 25.3.14 8:39</dd>
 * </dl>
 */
public class CharacterMultipleTextsAnalysis extends AbstractCharacterAnalysis implements MultipleTextsAnalysis {

	private Map<WorkingText, CharacterFrequency> characterFrequencies;

	@Override
	public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {
		OptionPanel optionPanel = new OptionPanel();
		int result = JOptionPane.showConfirmDialog(mainPanel, optionPanel, "Nastavení analýzy četnosti znaků", JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}
		characterFrequencies = new HashMap<WorkingText, CharacterFrequency>(texts.size());
		for (Map.Entry<WorkingText, CompLing> entry : texts.entrySet()) {
			CompLing compLing = entry.getValue();
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
			this.characterFrequencies.put(entry.getKey(), characterFrequency.getCharacterFrequency());
		}

		handler.handleResult(this);
	}

	@Override
	public Results getResults() {
		return new CharacterMultipleTextsAnalysisResults(characterFrequencies);
	}


	private static class CharacterMultipleTextsAnalysisResults implements Results {
		private Map<WorkingText, CharacterFrequency> characterFrequencies;

		public CharacterMultipleTextsAnalysisResults(Map<WorkingText, CharacterFrequency> characterFrequencies) {
			this.characterFrequencies = characterFrequencies;
		}

		@Override
		public boolean resultsOk() {
			//..always ok
			return true;
		}

		@Override
		public ResultsPanel getResultPanel() {
			return new CharacterFrequencyPanel(characterFrequencies);
		}

		@Override
		public String getAnalysisName() {

			String names = MapUtils.getAllTextNames(characterFrequencies);

			return "Analýza četnosti znaků textů " + names;
		}
	}
}
