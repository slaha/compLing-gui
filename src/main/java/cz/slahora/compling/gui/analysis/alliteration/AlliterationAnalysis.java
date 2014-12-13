package cz.slahora.compling.gui.analysis.alliteration;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.frequency.character.ICharacterFrequency;
import cz.compling.analysis.analysator.poems.alliteration.IAlliteration;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.ui.ResultsPanel;
import cz.slahora.compling.gui.utils.MapUtils;

import javax.swing.JPanel;
import java.util.Map;

public class AlliterationAnalysis implements SingleTextAnalysis {

	private String textName;
	private IAlliteration alliteration;
	private ICharacterFrequency characterFrequency;

	@Override
	public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {

		final WorkingText workingText = MapUtils.getFirstKey(texts);
		final CompLing compLing = texts.get(workingText);

		alliteration = compLing.poemAnalysis().alliteration();
		characterFrequency = compLing.generalAnalysis().characterFrequency();
		textName = workingText.getName();

		handler.handleResult(this);
	}

	@Override
	public Results getResults() {
		return new AlliterationResults();
	}

	private class AlliterationResults implements Results {

		@Override
		public boolean resultsOk() {
			return true;
		}

		@Override
		public ResultsPanel getResultPanel() {
			AlliterationModel model = new AlliterationModel(textName, alliteration, characterFrequency);
			return new AlliterationResultsPanel(model);
		}

		@Override
		public String getAnalysisName() {
			return "Aliterace textu " + textName;
		}
	}
}
