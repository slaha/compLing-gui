package cz.slahora.compling.gui.ui.words;

import cz.slahora.compling.gui.analysis.words.WordTextAnalysisType;
import cz.slahora.compling.gui.ui.ChartType;
import cz.slahora.compling.gui.utils.ChartUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

import java.util.Locale;
import java.util.Set;

/**
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 3.8.14 10:57</dd>
 * </dl>
 */
public class WordFrequencyChartFactory<T> {
	private final String chartTitle;
	private final IWordFrequenciesModel<T> controller;

	public WordFrequencyChartFactory(IWordFrequenciesModel<T> controller, String chartTitle) {
		this.controller = controller;
		this.chartTitle = chartTitle;
	}

	public JFreeChart createChart(ChartType type, int lowerBound) {
		JFreeChart chart;
		switch (type) {
			case PIE:
				chart = ChartUtils.createPieChart(chartTitle, controller.getPieDataSet(lowerBound), false, true, false, Locale.getDefault());
				break;
			case XY_ABSOLUTE:
				chart = ChartUtils.createBarChart(chartTitle, "Jednotlivé znaky", "Absolutní četnost", controller.getAbsoluteBarDataSet(lowerBound), PlotOrientation.VERTICAL, true, true, true, true);
				break;
			case XY_RELATIVE:
				chart = ChartUtils.createBarChart(chartTitle, "Jednotlivé znaky", "Relativní četnost [%]", controller.getRelativeBarDataSet(lowerBound), PlotOrientation.VERTICAL, true, true, true, true);
				break;
			default:
				throw new IllegalArgumentException("WTF?? ChartType " + type + " not recognized");
		}
		if (chart.getPlot() instanceof CategoryPlot) {
			CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
			CategoryAxis xAxis = categoryPlot.getDomainAxis();
			xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
			xAxis.setLowerMargin(0);
			xAxis.setUpperMargin(0);
		}
		return chart;

	}

	public ChartPanel createPanel(JFreeChart chart, ChartType type, WordFrequencyResultsPanel panel) {
		final ChartPanel newChartPanel = ChartUtils.createPanel(chart);

		newChartPanel.putClientProperty("type", type);
		newChartPanel.addChartMouseListener(panel.createChartMouseListener(newChartPanel));

		newChartPanel.setMouseZoomable(true);
		newChartPanel.setRangeZoomable(true);
		newChartPanel.setDomainZoomable(true);
		newChartPanel.setMouseZoomable(true, true);
		return newChartPanel;
	}


	public void setDatasetMoreThan(ChartPanel chartPanel, int lowerBound) {
		final ChartType type = (ChartType)chartPanel.getClientProperty("type");
		chartPanel.setChart(createChart(type, lowerBound));

	}

	public ChartPanel createComparePlot(WordTextAnalysisType analysisType) {
		final Set<T> categories = controller.getAllCompareChartCategories();
		String chartTitle;
		switch (analysisType) {

			case WORD:
				chartTitle = "Srovnání zastoupení slov " + categories.toString();
				break;
			case WORD_LENGHT:
				chartTitle = "Srovnání zastoupení délek slov " + categories.toString();
				break;
			default:
				throw new IllegalArgumentException("Unknown analysisType '" + analysisType + '\'');
		}

		JFreeChart chart = ChartUtils.createBarChart(chartTitle, "Texty", "Četnost", controller.getBarDataSetFor(categories), PlotOrientation.VERTICAL, false, true, true, true);
		return ChartUtils.createPanel(chart);
	}
}
