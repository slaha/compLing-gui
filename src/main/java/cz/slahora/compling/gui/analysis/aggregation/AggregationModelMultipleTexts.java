package cz.slahora.compling.gui.analysis.aggregation;

import cz.compling.analysis.analysator.poems.aggregation.IAggregation;
import cz.compling.model.Aggregation;
import cz.compling.model.AggregationMath;
import cz.slahora.compling.gui.model.WorkingText;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Collection;
import java.util.Map;

public class AggregationModelMultipleTexts implements AggregationModel {
	private final String name;
	private final Map<WorkingText, IAggregation> aggregations;
	private AggregationMath math;
	private final int N;

	public AggregationModelMultipleTexts(String name, Map<WorkingText, IAggregation> aggregations) {
		super();
		this.name = name;
		this.aggregations = aggregations;
		final Collection<IAggregation> iAggregations = aggregations.values();
		
		final Aggregation[] agg = new Aggregation[iAggregations.size()];
		int i = 0;
		int n = AGGREGATION_N;
		for (IAggregation iAggregation : iAggregations) {
			agg[i++] = iAggregation.getAggregation();
			n = Math.min(n, iAggregation.getAggregation().getMaxDistance());
		}
		this.math = new AggregationMath(agg);
		this.N = n;
	}

	@Override
	public int getCountOfTexts() {
		return aggregations.size();
	}

	@Override
	public int getAggregationShifts() {
		return N;
	}

	@Override
	public String getTextsName() {
		return name;
	}


	@Override
	public double getAvgSimilarity(int shift) {
		return math.computeAvgSimilarity(shift);
	}

	@Override
	public double getApproximatedSimilarity(int shift) {
		return math.computeApproximatedSimilarity(AGGREGATION_N, shift);
	}

	@Override
	public double getCoefficientD() {
		return math.computeCoefficientD(AGGREGATION_N);
	}

	@Override
	public XYDataset getChartDataset() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries avg = new XYSeries("avg");
		XYSeries approx = new XYSeries("approx");

		for (int shift = 1; shift <= AGGREGATION_N; shift++) {
			avg.add(shift, math.computeAvgSimilarity(shift));
			approx.add(shift, math.computeApproximatedSimilarity(AGGREGATION_N, shift));
		}

		dataset.addSeries(avg);
		dataset.addSeries(approx);

		return dataset;
	}
}
