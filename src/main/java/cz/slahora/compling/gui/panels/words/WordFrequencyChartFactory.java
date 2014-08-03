package cz.slahora.compling.gui.panels.words;

import cz.slahora.compling.gui.panels.ChartType;
import cz.slahora.compling.gui.utils.ChartUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import java.util.Locale;
import java.util.Set;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 3.8.14 10:57</dd>
 * </dl>
 */
public class WordFrequencyChartFactory {
	private final String chartTitle;
	private final WordFrequenciesModel controller;

	public WordFrequencyChartFactory(WordFrequenciesModel controller, String chartTitle) {
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


	public void setDatasetMoreThanOne(ChartPanel chartPanel, int lowerBound) {
		final ChartType type = (ChartType)chartPanel.getClientProperty("type");
		chartPanel.setChart(createChart(type, lowerBound));

	}

	public ChartPanel createComparePlot() {
		final Set<String> categories = controller.getAllCompareChartCategories();
		String chartTitle = "Srovnání zastoupení znaku " + categories.toString();
		JFreeChart chart = ChartUtils.createBarChart(chartTitle, "Četnost", "Texty", controller.getBarDataSetFor(categories.toArray(new String[categories.size()])), PlotOrientation.VERTICAL, false, true, true, true);
		return ChartUtils.createPanel(chart);
	}
}
