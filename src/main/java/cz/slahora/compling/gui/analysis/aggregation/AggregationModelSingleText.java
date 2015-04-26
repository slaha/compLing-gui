package cz.slahora.compling.gui.analysis.aggregation;

import cz.compling.analysis.analysator.poems.aggregation.IAggregation;
import cz.compling.model.AggregationMath;
import cz.compling.model.Aggregations;
import cz.slahora.compling.gui.model.CsvData;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class AggregationModelSingleText implements AggregationModel {

	private final String textName;
	private final AggregationMath math;
	private final int N;
	private final Aggregations aggregation;

	private double maxChartValue;
	private double minChartValue;

	public AggregationModelSingleText(String textName, IAggregation aggregation) {
		this.textName = textName;
		this.aggregation = aggregation.getAggregations();
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

	@Override
	public CsvData getCsvData() {
		CsvData d = new CsvData();
		d.addSection();
		CsvData.CsvDataSection section = d.getCurrentSection();

		section.setHeadline(textName);

		section.addHeader("Základní verš");
		section.addHeader("Vzdálenost");
		section.addHeader("A1");
		section.addHeader("A2");
		section.addHeader("Průnik A1 s A2");
		section.addHeader("B1");
		section.addHeader("B2");
		section.addHeader("Průnik B1 s B2");

		Comparator<Aggregations.Aggregation.LineAggregation> comparator = new Comparator<Aggregations.Aggregation.LineAggregation>() {
			@Override
			public int compare(Aggregations.Aggregation.LineAggregation o1, Aggregations.Aggregation.LineAggregation o2) {
				return o1.baseLine - o2.baseLine;
			}
		};
		for (int shift = 1; shift <= N; shift++) {

			final List<Aggregations.Aggregation.LineAggregation> aggs = aggregation.getAggregationsForShift(shift, N);
			Collections.sort(aggs, comparator);
			for (Aggregations.Aggregation.LineAggregation a : aggs) {
				section.startNewLine();
				section.addData(a.baseLine);
				section.addData(shift);

				section.addData(a.getSingleSet1Size());
				section.addData(a.getSingleSet2Size());
				section.addData(a.getSingleSetsIntersectionSize());

				section.addData(a.getDoubleSet1Size());
				section.addData(a.getDoubleSet2Size());
				section.addData(a.getDoubleSetsIntersectionSize());
			}
		}

		return d;
	}

	@Override
	public double getApproxA() {
		return math.getApproxA(AGGREGATION_N);
	}

	@Override
	public double getApproxB() {
		return math.getApproxB(AGGREGATION_N);
	}
}
