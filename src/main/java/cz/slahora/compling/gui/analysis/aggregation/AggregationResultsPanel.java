package cz.slahora.compling.gui.analysis.aggregation;

import cz.slahora.compling.gui.analysis.assonance.NonEditableTable;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.ui.AbstractResultsPanel;
import cz.slahora.compling.gui.ui.ResultsPanel;
import cz.slahora.compling.gui.ui.ResultsScrollablePanel;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.jdesktop.swingx.JXTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;

class AggregationResultsPanel extends AbstractResultsPanel implements ResultsPanel {

	private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.##");
	private final AggregationModel model;

	public AggregationResultsPanel(final AggregationModel model) {
		super(new ResultsScrollablePanel());
		this.model = model;

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JLabel headline = new HtmlLabelBuilder().hx(1, "Agregace %s %s",
			(model.getCountOfTexts() == 1 ? "textu" : "textů"),
			model.getTextsName())
			.build();

		addToPanel(headline);

		final double coefficientD = model.getCoefficientD();
		if (Double.isNaN(coefficientD)) {
			JLabel text;
			String s = model.getCountOfTexts() == 1 ? "Text je příliš krátký." : "Texty jsou příliš krátké.";
			text =  new HtmlLabelBuilder().p(s + " Agregaci nebylo možné provést.")
				.build();

			text.setBorder(new EmptyBorder(30, 0, 30, 0));
			addToPanel(text);
		}



		JXTable aggregationTable = new NonEditableTable(new AggregationTableModel(model));
		aggregationTable.setBorder(BorderFactory.createLineBorder(Color.black));
		aggregationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		aggregationTable.setColumnSelectionAllowed(true);
		aggregationTable.setRowSelectionAllowed(false);
		aggregationTable.setRowHeight((int) (aggregationTable.getRowHeight() * 1.5));
		aggregationTable.setDefaultRenderer(String.class, new FirstColumnTableCellRenderer());
		aggregationTable.setDefaultRenderer(Number.class, new NumberColumnTableCellRenderer());
		addToPanel(aggregationTable);

		JLabel equation = new HtmlLabelBuilder().p("Aproximační funkce S = %.4f × L <sup>−%.4f</sup>", model.getApproxA(), model.getApproxB()).build();

		equation.setBorder(new EmptyBorder(30, 0, 10, 0));
		addToPanel(equation);

		JLabel text = new HtmlLabelBuilder().p("Koeficient determinace D = %f", coefficientD).build();
		addToPanel(text);

		JFreeChart chart = createChart(model);
		final ChartPanel chartPanel = new ChartPanel(chart) {
			@Override
			protected JPopupMenu createPopupMenu(boolean properties, boolean save, boolean print, boolean zoom) {
				return super.createPopupMenu(false, save, print, zoom);
			}

			@Override
			protected JPopupMenu createPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) {
				return super.createPopupMenu(false, copy, save, print, zoom);
			}

		};

		chartPanel.setBackground(Color.white);
		chartPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
		addToPanel(chartPanel);
	}

	private JFreeChart createChart(AggregationModel model) {

		String title = String.format("Agregace %s %s",
			(model.getCountOfTexts() == 1 ? "textu" : "textů"),
			model.getTextsName());

		String xAxisLabel, yAxisLabel;
		xAxisLabel = yAxisLabel = "";
		XYDataset dataset = model.getChartDataset();
		final JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, false, true, false);
		chart.setBackgroundPaint(Color.white);

		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesShapesFilled(0, false);
		renderer.setSeriesShapesVisible(1, false);
		plot.setRenderer(renderer);

		final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		domainAxis.setRange(new Range(1, model.getAggregationShifts() + 1));

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		double min = Math.floor(model.getMinChartValue()) - 1;
		double max = Math.ceil(model.getMaxChartValue()) + 1;
		rangeAxis.setRange(new Range(min, max));

		StandardXYToolTipGenerator avgToolTipGenerator = new StandardXYToolTipGenerator("Agregace pro vzdálenost {1}. Průměrná podobnost Si = {2}", NUMBER_FORMAT, NUMBER_FORMAT);
		renderer.setSeriesToolTipGenerator(0, avgToolTipGenerator);

		StandardXYToolTipGenerator approxToolTipGenerator = new StandardXYToolTipGenerator("Agregace pro vzdálenost {1}. Aproximovaná podobnost Ŝi = {2}", NUMBER_FORMAT, NUMBER_FORMAT);
		renderer.setSeriesToolTipGenerator(1, approxToolTipGenerator);

		return chart;
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public CsvData getCsvData() {
		return model.getCsvData();
	}

	private void addSections(CsvData.CsvDataSection section) {
		section.addHeader("Velikost Ai");
		section.addHeader("Velikost Aj");
		section.addHeader("Velikost průniku Ai s Aj");
		section.addHeader("Velikost Bi");
		section.addHeader("Velikost Bj");
		section.addHeader("Velikost průniku Bi s Bj");
	}

	private static class FirstColumnTableCellRenderer extends DefaultTableCellRenderer {


		static final Border BORDER = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.black);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			c.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			if (row == 2 && column == 0) {
				c.setBorder(BORDER);
				JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				panel.add(c);
				panel.setBackground(Color.white);
				return panel;
			}

			return c;
		}
	}

	private static class NumberColumnTableCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			c.setText(NUMBER_FORMAT.format(value));
			return c;
		}
	}
}
