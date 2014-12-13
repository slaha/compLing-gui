package cz.slahora.compling.gui.ui.characters;

import cz.slahora.compling.gui.model.CharacterFrequencyModel;
import cz.slahora.compling.gui.ui.ChartType;
import cz.slahora.compling.gui.utils.ChartUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import java.util.Locale;
import java.util.Set;

public class CharacterFrequencyChartFactory {
	private final CharacterFrequencyModel model;
	private final String chartTitle;

	public CharacterFrequencyChartFactory(CharacterFrequencyModel model, String chartTitle) {
		this.model = model;
		this.chartTitle = chartTitle;
	}

	public JFreeChart createChart(ChartType type) {
		JFreeChart chart;
		switch (type) {
			case PIE:
				chart = ChartUtils.createPieChart(chartTitle, model.getPieDataSet(), true, true, true, Locale.getDefault());
				break;
			case XY_ABSOLUTE:
				chart = ChartUtils.createBarChart(chartTitle, "Jednotlivé znaky", "Absolutní četnost", model.getAbsoluteBarDataSet(), PlotOrientation.VERTICAL, true, true, true, true);
				break;
			case XY_RELATIVE:
				chart = ChartUtils.createBarChart(chartTitle, "Jednotlivé znaky", "Relativní četnost [%]", model.getRelativeBarDataSet(), PlotOrientation.VERTICAL, true, true, true, true);
				break;
			default:
				throw new IllegalArgumentException("WTF?? ChartType " + type + " not recognized");
		}
		return chart;
	}

	public ChartPanel createChartPanel(JFreeChart chart, ChartType type, CharacterFrequencyPanel resPanel) {
		final ChartPanel newChartPanel = ChartUtils.createPanel(chart);
		newChartPanel.putClientProperty("type", type);
		newChartPanel.addChartMouseListener(resPanel.createChartMouseListener(newChartPanel));

		return newChartPanel;
	}

	public ChartPanel createComparePlot() {
		final Set<String> categories = model.getAllCompareChartCategories();
		String chartTitle = "Srovnání zastoupení znaku " + categories.toString();
		JFreeChart chart = ChartUtils.createBarChart(chartTitle, "Četnost", "Texty", model.getBarDataSetFor(categories.toArray(new String[categories.size()])), PlotOrientation.VERTICAL, false, true, true, true);
		return ChartUtils.createPanel(chart);
	}
}
