package cz.slahora.compling.gui.analysis.character;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.frequency.character.CharacterFrequencyRule;
import cz.compling.analysis.analysator.frequency.character.ICharacterFrequency;
import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.panels.characters.CharacterFrequencyPanel;
import cz.slahora.compling.gui.utils.MapUtils;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.util.Collections;
import java.util.Map;

public class CharacterSingleTextAnalysis extends AbstractCharacterAnalysis implements SingleTextAnalysis<CharacterFrequency> {

	private CharacterFrequency characterFrequency;
	private WorkingText text;

	@Override
	public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {

		this.text = MapUtils.getFirstKey(texts);
		CompLing compLing = MapUtils.getFirstValue(texts);
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

		handler.handleResult(this);
	}

	@Override
	public Results getResults() {
		return new CharacterSingleTextAnalysisResults(text, characterFrequency);
	}

	private static class CharacterSingleTextAnalysisResults implements Results {
		private final String name;
		private Map<WorkingText, CharacterFrequency> map;

		public CharacterSingleTextAnalysisResults(WorkingText text, CharacterFrequency characterFrequency) {
			name = text.getName();
			map = Collections.singletonMap(text, characterFrequency);
		}

		@Override
		public boolean resultsOk() {
			//..always ok
			return true;
		}

		@Override
		public ResultsPanel getResultPanel() {
			return new CharacterFrequencyPanel(map);
		}

		@Override
		public String getAnalysisName() {
			return "Analýza četnosti znaků textu " + name;
		}
	}
}
