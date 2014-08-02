package cz.slahora.compling.gui.analysis.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.panels.words.WordFrequencyResultsPanel;

import java.util.Map;


class WordResults implements Results {
	private final Map<WorkingText, IWordFrequency> frequencies;

	public WordResults(Map<WorkingText, IWordFrequency> frequencies) {
		this.frequencies = frequencies;
	}

	@Override
	public boolean resultsOk() {
		return true;
	}

	@Override
	public ResultsPanel getResultPanel() {
		return new WordFrequencyResultsPanel(frequencies);
	}
}
