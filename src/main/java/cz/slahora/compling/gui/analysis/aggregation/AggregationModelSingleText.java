package cz.slahora.compling.gui.analysis.aggregation;

import cz.compling.analysis.analysator.poems.aggregation.IAggregation;
import cz.compling.model.AggregationMath;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

class AggregationModelSingleText implements AggregationModel {

	private final String textName;
	private final AggregationMath math;
	private final int N;

	public AggregationModelSingleText(String textName, IAggregation aggregation) {
		this.textName = textName;
		this.math = new AggregationMath(aggregation.getAggregation());
		this.N = Math.min(AGGREGATION_N, aggregation.getAggregation().getMaxDistance());
	}

	public int getCountOfTexts() {
		return 1;
	}

	@Override
	public int getAggregationShifts() {
		return N;
	}

	public String getTextsName() {
		return textName;
	}

	@Override
	public double getAvgSimilarity(int shift) {
		return math.computeAvgSimilarity(shift);
	}

	@Override
	public double getApproximatedSimilarity(int shift) {
		return math.computeApproximatedSimilarity(N, shift);
	}

	@Override
	public double getCoefficientD() {
		return math.computeCoefficientD(N);
	}

	@Override
	public XYDataset getChartDataset() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries avg = new XYSeries("avg");
		XYSeries approx = new XYSeries("approx");

		for (int shift = 1; shift <= N; shift++) {
			avg.add(shift, math.computeAvgSimilarity(shift));
			approx.add(shift, math.computeApproximatedSimilarity(N, shift));
		}

		dataset.addSeries(avg);
		dataset.addSeries(approx);

		return dataset;
	}
}
