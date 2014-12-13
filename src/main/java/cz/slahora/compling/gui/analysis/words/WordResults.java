package cz.slahora.compling.gui.analysis.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.ui.ResultsPanel;
import cz.slahora.compling.gui.ui.words.WordFrequencyResultsPanel;
import cz.slahora.compling.gui.utils.MapUtils;

import java.util.Map;


class WordResults implements Results {
	private final WordTextAnalysisType analysisType;
	private final Map<WorkingText, IWordFrequency> frequencies;

	public WordResults(WordTextAnalysisType analysisType, Map<WorkingText, IWordFrequency> frequencies) {
		this.analysisType = analysisType;
		this.frequencies = frequencies;
	}

	@Override
	public boolean resultsOk() {
		return true;
	}

	@Override
	public ResultsPanel getResultPanel() {
		return analysisType == WordTextAnalysisType.WORD ?
			new WordFrequencyResultsPanel(analysisType, frequencies, new WordFrequencyResultsPanel.WordFactory())
			 :
			new WordFrequencyResultsPanel(analysisType, frequencies, new WordFrequencyResultsPanel.WordLengthFactory());
	}

	@Override
	public String getAnalysisName() {
		String type = (analysisType == WordTextAnalysisType.WORD) ? "obsahu slova" : "délky slova";
		String text = frequencies.size() == 1 ?  "textu" : "textů";
		String textNames = MapUtils.getAllTextNames(frequencies);

		return "Analýza četnosti slov (dle " + type + ") " + text + textNames;
	}
}
