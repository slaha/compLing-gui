package cz.slahora.compling.gui.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;

import java.awt.Color;
import java.util.Locale;

/**
 *
 * Utilities for working with graphs from JFreeChart library
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 30.3.14 20:32</dd>
 * </dl>
 */
public class ChartUtils {

	private static final int MINIMUM_SIZE = 100;
	private static final int CHART_WIDTH = 400;
	private static final int CHART_HEIGHT = 800;

	/**
	 * Create pie chart.
	 *
	 * @param chartTitle the chart title
	 * @param pieDataSet the pie data set
	 * @param legend the legend
	 * @param tooltips the tooltips
	 * @param drawLabels
	 *@param locale the locale  @return created chart
	 */
	public static JFreeChart createPieChart(String chartTitle, PieDataset pieDataSet, boolean legend, boolean tooltips, boolean drawLabels, Locale locale) {
		JFreeChart chart = ChartFactory.createPieChart(chartTitle, pieDataSet, legend, tooltips, locale);
		//..remove shadows
		PiePlot piePlot = (PiePlot) chart.getPlot();
		piePlot.setShadowPaint(null);
		if (!drawLabels) {
			piePlot.setLabelGenerator(null);
		}
		return prepareChart(chart);
	}

	/**
	 * Create bar chart.
	 *
	 * @param title the title
	 * @param categoryLabel the category label
	 * @param valueAxisLabel the value axis label
	 * @param dataSet the data set
	 * @param orientation the orientation
	 * @param drawBlack the draw black
	 * @param legend the legend
	 * @param tooltips the tooltips
	 * @param urls the urls
	 * @return created chart
	 */
	public static JFreeChart createBarChart(String title, String categoryLabel, String valueAxisLabel, CategoryDataset dataSet, PlotOrientation orientation, boolean drawBlack, boolean legend, boolean tooltips, boolean urls) {
		JFreeChart chart = ChartFactory.createBarChart(title, categoryLabel, valueAxisLabel, dataSet, orientation, legend, tooltips, urls);
		BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
		if (drawBlack) {
			//..black bars ...
			renderer.setSeriesPaint(0, Color.black);
		}
		//..without shadow and gradient
		renderer.setShadowVisible(false);
		renderer.setBarPainter(new StandardBarPainter());
		return prepareChart(chart);
	}


	/**
	 * Set colors to chart
	 *
	 * @param chart the chart to set color
	 * @return the chart {@code chart}
	 */
	private static JFreeChart prepareChart(JFreeChart chart) {
		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setBackgroundPaint(Color.white);
		return chart;
	}

	/**
	 * Create panel for chart {@code chart}.
	 *
	 * @param chart the chart to display on panel
	 * @return the panel with the chart
	 */
	public static ChartPanel createPanel(JFreeChart chart) {
		ChartPanel panel = new ChartPanel(chart, CHART_WIDTH, CHART_HEIGHT, MINIMUM_SIZE, MINIMUM_SIZE, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false, true, true, true, true, true);
		panel.setBackground(Color.white);
		return panel;
	}
}
