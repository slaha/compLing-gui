package cz.slahora.compling.gui.analysis.words;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.compling.analysis.analysator.frequency.words.WordFrequencyRule;
import cz.slahora.compling.gui.analysis.MultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.model.WorkingText;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.util.HashMap;
import java.util.Map;

public class WordMultipleTextsAnalysis extends WordTextAnalysis implements MultipleTextsAnalysis {

	private Map<WorkingText, IWordFrequency> frequencies;
	private WordTextAnalysisType analysisType;

	@Override
	public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {
		OptionsPanel optionPanel = new OptionsPanel();
		int result = JOptionPane.showConfirmDialog(mainPanel, optionPanel, "Nastavení analýzy četnosti znaků", JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		analysisType = optionPanel.getAnalysisType();

		frequencies = new HashMap<WorkingText, IWordFrequency>(texts.size());

		for (Map.Entry<WorkingText, CompLing> entry : texts.entrySet()) {
			if (optionPanel.applyCaseInsensitiveRule()) {
				entry.getValue().registerRule(new CaseInsensitiveRule());
			}
			frequencies.put(entry.getKey(), entry.getValue().generalAnalysis().wordFrequency());
		}

		for (IWordFrequency wordFrequency : frequencies.values()) {
			for (WordFrequencyRule rule : optionPanel.replaceRules()) {
				wordFrequency.registerRule(rule);
			}
		}

		handler.handleResult(this);
	}

	@Override
	public Results getResults() {
		return new WordResults(analysisType, frequencies);
	}
}
