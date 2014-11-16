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

	private double maxChartValue;
	private double minChartValue;

	public AggregationModelSingleText(String textName, IAggregation aggregation) {
		this.textName = textName;
		this.N = Math.min(AGGREGATION_N, aggregation.getAggregations().getMaxDistance());
		this.math = new AggregationMath(N, aggregation.getAggregations());
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

		maxChartValue = Double.MIN_VALUE;
		minChartValue = Double.MAX_VALUE;

		for (int shift = 1; shift <= N; shift++) {
			final double value = math.computeAvgSimilarity(shift);
			final double approximation = math.computeApproximatedSimilarity(N, shift);
			avg.add(shift, value);
			approx.add(shift, approximation);

			maxChartValue = Math.max(maxChartValue, Math.max(value, approximation));
			minChartValue = Math.min(minChartValue, Math.min(value, approximation));
		}

		dataset.addSeries(avg);
		dataset.addSeries(approx);

		return dataset;
	}

	@Override
	public double getMaxChartValue() {
		return maxChartValue;
	}

	@Override
	public double getMinChartValue() {
		return minChartValue;
	}
}
