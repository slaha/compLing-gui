package cz.slahora.compling.gui.analysis.aggregation;

import org.jfree.data.xy.XYDataset;

interface AggregationModel {

	static final int AGGREGATION_N = 10;

	int getCountOfTexts();
	int getAggregationShifts();

	String getTextsName();

	double getAvgSimilarity(int columnIndex);
	double getApproximatedSimilarity(int columnIndex);
	double getCoefficientD();

	XYDataset getChartDataset();

	double getMaxChartValue();

	double getMinChartValue();
}
