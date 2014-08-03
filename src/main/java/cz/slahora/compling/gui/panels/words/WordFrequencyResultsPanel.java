package cz.slahora.compling.gui.panels.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.AbstractResultsPanel;
import cz.slahora.compling.gui.panels.ChartPanelWrapper;
import cz.slahora.compling.gui.panels.ChartType;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;

public class WordFrequencyResultsPanel extends AbstractResultsPanel implements ResultsPanel {


	private final WordFrequenciesController controller;

	private ChartPanelWrapper allWordsChartPanel;
	private final WordFrequencyChartFactory chartFactory;
	private final JSpinner lowerFreqBound;
	private int y;
	private ChangeListener boundChangedListener;

	public WordFrequencyResultsPanel(Map<WorkingText, IWordFrequency> wordFrequencies) {
		super(new JPanel(new GridBagLayout()));
		this.controller = new WordFrequenciesController(wordFrequencies);
		final String chartTitle = "Zastoupení jednotlivých slov";
		this.chartFactory = new WordFrequencyChartFactory(controller, chartTitle);
		SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, controller.getMaxOccurence(), 1);
		lowerFreqBound = new JSpinner(spinnerModel);
	}

	@Override
	public JPanel getPanel() {
		panel.removeAll();

		y = 0;

		JLabel headline1 = new HtmlLabelBuilder().hx(1, "Frekvence výskytu slov").build();
		panel.add(headline1, new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).fill(GridBagConstraints.HORIZONTAL).build());

		JLabel text = new HtmlLabelBuilder().p(controller.getMainParagraphText()).build();
		panel.add(text, new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).fill(GridBagConstraints.HORIZONTAL).build());


		final WordFrequencyTableModel tableModel = controller.getTableModel();
		//table with words occurrences
		JTable table = new JTable(controller.getTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
		table.setAutoCreateRowSorter(true);
		table.setBackground(Color.WHITE);
		table.invalidate();

		panel.add(
			table.getTableHeader(),
			new GridBagConstraintBuilder()
				.gridxy(0, y++)
				.fill(GridBagConstraints.HORIZONTAL)
				.weightx(1)
				.build()
		);
		panel.add(
			table,
			new GridBagConstraintBuilder()
				.gridxy(0, y++)
				.fill(GridBagConstraints.BOTH)
				.weightx(1)
				.weighty(1)
				.build()
		);


		panel.add(
			new HtmlLabelBuilder().hx(2, "Graf četností jednotlivých slov").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).build()
		);

		final ChartPanel plot = createPlot(ChartType.PIE);
		allWordsChartPanel = createChartPanel(plot);
		putChartPanel(y, allWordsChartPanel);
		y++;
		/*************************************/
		JPanel dummy = new JPanel();
		dummy.setBackground(Color.white);
		panel.add(dummy, new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).weighty(1).fill(GridBagConstraints.BOTH).build());
		return panel;
	}

	private ChartPanelWrapper createChartPanel(final ChartPanel chartPanel) {
		ChartPanelWrapper panel = new ChartPanelWrapper(chartPanel);

		lowerFreqBound.removeChangeListener(boundChangedListener);

		boundChangedListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				chartFactory.setDatasetMoreThanOne(chartPanel, getLowerOccurrenceBound());
			}
		};
		lowerFreqBound.addChangeListener(boundChangedListener);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
		footer.setBackground(Color.white);
		footer.add(new JLabel("Zobrazovat pouze slova, která se vyskytla alespoň "));
		footer.add(lowerFreqBound);
		footer.add(new JLabel(" ×"));

		panel.add(chartPanel, new GridBagConstraintBuilder().gridy(0).build());
		panel.add(footer, new GridBagConstraintBuilder().gridy(1).build());

		return panel;
	}

	public ChartMouseListener createChartMouseListener(final ChartPanel newChartPanel) {

		return new ChartMouseListener() {
			@Override
			public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
				ChartPanelWrapper old = allWordsChartPanel;
				final ChartType type = (ChartType)newChartPanel.getClientProperty("type");
				final ChartType nextType = ChartType.values()[ (type.ordinal() + 1) % ChartType.values().length  ];
				ChartPanel newOne = createPlot(nextType);
				final ChartPanelWrapper chartPanel = createChartPanel(newOne);
				changeChartPanel(old, chartPanel);
				allWordsChartPanel = chartPanel;
			}

			@Override
			public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
			}
		};
	}

	private ChartPanel createPlot(ChartType type) {
		final JFreeChart chart = chartFactory.createChart(type, getLowerOccurrenceBound());

		return chartFactory.createPanel(chart, type, this);
	}

	private int getLowerOccurrenceBound() {
		final Object value = lowerFreqBound.getModel().getValue();
		return ((Number)value).intValue();
//		return ((Double)lowerFreqBound.getModel().getValue()).intValue();
	}

	@Override
	public CsvData getCsvData() {
		return new CsvData();
	}
}
