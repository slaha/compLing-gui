package cz.slahora.compling.gui.analysis.aggregation;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.poems.aggregation.IAggregation;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.MapUtils;

import javax.swing.JPanel;
import java.util.Map;

public class AggregationSingleText implements SingleTextAnalysis {
	private String name;
	private IAggregation aggregation;

	@Override
	public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {
		final WorkingText workingText = MapUtils.getFirstKey(texts);
		final CompLing compLing = texts.get(workingText);

		this.name = workingText.getName();

		aggregation = compLing.poemAnalysis().aggregation();

		handler.handleResult(this);
	}

	@Override
	public Results getResults() {
		return new AggregationResults();
	}

	private class AggregationResults implements Results {
		@Override
		public boolean resultsOk() {
			return true;
		}

		@Override
		public ResultsPanel getResultPanel() {

			AggregationModel model = new AggregationModelSingleText(name, aggregation);
			return new AggregationResultsPanel(model);
		}

		@Override
		public String getAnalysisName() {
			return "Agregace textu " + name;
		}
	}
}
