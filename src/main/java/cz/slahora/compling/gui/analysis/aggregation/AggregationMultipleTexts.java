package cz.slahora.compling.gui.analysis.aggregation;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.poems.aggregation.IAggregation;
import cz.slahora.compling.gui.analysis.MultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.MapUtils;
import org.apache.commons.lang3.text.StrBuilder;

import javax.swing.JPanel;
import java.util.HashMap;
import java.util.Map;

public class AggregationMultipleTexts implements MultipleTextsAnalysis {
	private String name;
	private Map<WorkingText, IAggregation> aggregations;

	@Override
	public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {
		final WorkingText workingText = MapUtils.getFirstKey(texts);
		final CompLing compLing = texts.get(workingText);

		this.name = new StrBuilder().appendWithSeparators(texts.keySet(), ", ").build();

		aggregations = new HashMap<WorkingText, IAggregation>(texts.size());
		for (Map.Entry<WorkingText, CompLing> entry : texts.entrySet()) {
			aggregations.put(entry.getKey(), entry.getValue().poemAnalysis().aggregation());
		}

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
			AggregationModel model = new AggregationModelMultipleTexts(name, aggregations);
			return new AggregationResultsPanel(model);
		}

		@Override
		public String getAnalysisName() {
			return "Agregace textů " + name;
		}
	}
}
