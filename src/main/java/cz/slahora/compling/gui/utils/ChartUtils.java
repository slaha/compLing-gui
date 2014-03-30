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

import java.awt.*;
import java.util.Locale;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 30.3.14 20:32</dd>
 * </dl>
 */
public class ChartUtils {

	public static JFreeChart createPieChart(String chartTitle, PieDataset pieDataSet, boolean legend, boolean tooltips, Locale locale) {
		JFreeChart chart = ChartFactory.createPieChart(chartTitle, pieDataSet, legend, tooltips,locale);
		//..remove shadows
		PiePlot piePlot = (PiePlot) chart.getPlot();
		piePlot.setShadowPaint(null);
		return prepareChart(chart);
	}

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

	private static JFreeChart prepareChart(JFreeChart chart) {
		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setBackgroundPaint(Color.white);
		return chart;
	}

	public static ChartPanel createPanel(JFreeChart chart) {
		ChartPanel panel = new ChartPanel(chart, 800, 800, 500, 500, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false, true, true, true, true, true);
		panel.setBackground(Color.white);
		return panel;
	}
}
