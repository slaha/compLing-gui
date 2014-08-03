package cz.slahora.compling.gui.analysis.words;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.MapUtils;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.util.Collections;
import java.util.Map;

public class WordSingleTextAnalysis extends WordTextAnalysis implements SingleTextAnalysis<Object> {

	private IWordFrequency wordFrequency;
	private WorkingText text;

	@Override
	public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {
		text = MapUtils.getFirstKey(texts);
		CompLing compLing = MapUtils.getFirstValue(texts);

		OptionsPanel optionPanel = new OptionsPanel();
		int result = JOptionPane.showConfirmDialog(mainPanel, optionPanel, "Nastavení analýzy četnosti znaků pro " + text.getName(), JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}



		if (optionPanel.applyCaseInsensitiveRule()) {
			compLing.registerRule(new CaseInsensitiveRule());
		}
		/*
		for (CharacterFrequencyRule rule : optionPanel.replaceRules()) {
			characterFrequency.registerRule(rule);
		}
		*/

		this.wordFrequency = compLing.generalAnalysis().wordFrequency();

		handler.handleResult(this);
	}

	@Override
	public Results getResults() {
		return new WordResults(Collections.singletonMap(text, wordFrequency));
	}

}
