package cz.slahora.compling.gui.analysis.words;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.analysis.MultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.model.WorkingText;

import javax.swing.JPanel;
import java.util.HashMap;
import java.util.Map;

public class WordMultipleTextsAnalysis implements MultipleTextsAnalysis<Object> {

	private Map<WorkingText, IWordFrequency> frequencies;

	@Override
	public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {
		frequencies = new HashMap<WorkingText, IWordFrequency>(texts.size());
		for (Map.Entry<WorkingText, CompLing> entry : texts.entrySet()) {
			frequencies.put(entry.getKey(), entry.getValue().generalAnalysis().wordFrequency());
		}

		handler.handleResult(this);
	}

	@Override
	public Results getResults() {
		return new WordResults(frequencies);
	}
}
