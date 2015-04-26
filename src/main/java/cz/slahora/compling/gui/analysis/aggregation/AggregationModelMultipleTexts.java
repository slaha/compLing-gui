package cz.slahora.compling.gui.analysis.aggregation;

import cz.compling.analysis.analysator.poems.aggregation.IAggregation;
import cz.compling.model.AggregationMath;
import cz.compling.model.Aggregations;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.*;

public class AggregationModelMultipleTexts implements AggregationModel {
	private final String name;
	private final Map<WorkingText, IAggregation> aggregations;
	private AggregationMath math;
	private final int N;

	private double maxChartValue;
	private double minChartValue;
	private final Aggregations[] aggregationses;

	public AggregationModelMultipleTexts(String name, Map<WorkingText, IAggregation> aggregations) {
		super();
		this.name = name;
		this.aggregations = aggregations;
		final Collection<IAggregation> iAggregations = aggregations.values();

		aggregationses = new Aggregations[iAggregations.size()];
		int i = 0;
		int n = AGGREGATION_N;
		for (IAggregation iAggregation : iAggregations) {
			aggregationses[i++] = iAggregation.getAggregations();
			n = Math.min(n, iAggregation.getAggregations().getMaxDistance());
		}
		this.N = n;
		this.math = new AggregationMath(N, aggregationses);
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

		maxChartValue = Double.MIN_VALUE;
		minChartValue = Double.MAX_VALUE;

		for (int shift = 1; shift <= AGGREGATION_N; shift++) {
			final double value = math.computeAvgSimilarity(shift);
			avg.add(shift, value);
			final double approximation = math.computeApproximatedSimilarity(AGGREGATION_N, shift);
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

		Comparator<Aggregations.Aggregation.LineAggregation> comparator = new Comparator<Aggregations.Aggregation.LineAggregation>() {
			@Override
			public int compare(Aggregations.Aggregation.LineAggregation o1, Aggregations.Aggregation.LineAggregation o2) {
				return o1.baseLine - o2.baseLine;
			}
		};
		for (Map.Entry<WorkingText, IAggregation> e : aggregations.entrySet()) {

			d.addSection();
			CsvData.CsvDataSection section = d.getCurrentSection();

			section.setHeadline(e.getKey().getName());

			section.addHeader("Základní verš");
			section.addHeader("Vzdálenost");
			section.addHeader("A1");
			section.addHeader("A2");
			section.addHeader("Průnik A1 s A2");
			section.addHeader("B1");
			section.addHeader("B2");
			section.addHeader("Průnik B1 s B2");

			for (int shift = 1; shift <= N; shift++) {

				final List<Aggregations.Aggregation.LineAggregation> aggs = e.getValue().getAggregations().getAggregationsForShift(shift, N);
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
