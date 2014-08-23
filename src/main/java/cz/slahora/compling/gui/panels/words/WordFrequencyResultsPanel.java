package cz.slahora.compling.gui.panels.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.analysis.words.WordTextAnalysisType;
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
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WordFrequencyResultsPanel extends AbstractResultsPanel implements ResultsPanel {


	private final IWordFrequenciesModel model;
	private final WordTextAnalysisType analysisType;

	private JComponent allWordsChartComponent;
	private JComponent compareChartComponent;

	private final WordFrequencyChartFactory chartFactory;
	private final JSpinner lowerFreqBound;
	private int y;
	private ChangeListener boundChangedListener;

	public WordFrequencyResultsPanel(WordTextAnalysisType analysisType, Map<WorkingText, IWordFrequency> wordFrequencies) {
		super(new JPanel(new GridBagLayout()));
		this.analysisType = analysisType;
		switch (analysisType) {
			case WORD:
				this.model = new WordFrequenciesModel(wordFrequencies);
				break;
			case WORD_LENGHT:
				this.model = new WordLengthFrequenciesModel(wordFrequencies);
				break;
			default:
				throw new IllegalStateException("Cannot create model for type " + analysisType);
		}

		final String chartTitle = "Zastoupení jednotlivých slov";
		this.chartFactory = new WordFrequencyChartFactory(model, chartTitle);
		SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, model.getFilterMaxValue(), 1);
		lowerFreqBound = new JSpinner(spinnerModel);
	}

	@Override
	public JPanel getPanel() {
		panel.removeAll();

		y = 0;

		JLabel headline1 = new HtmlLabelBuilder().hx(1, "Frekvence výskytu slov").build();
		panel.add(headline1, new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).fill(GridBagConstraints.HORIZONTAL).build());

		JLabel text = new HtmlLabelBuilder().p(model.getMainParagraphText()).build();
		panel.add(text, new GridBagConstraintBuilder().gridXY(0, y++).build());


		//table with words occurrences
		JTable table = new JTable(model.getTableModel());
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
				.gridXY(0, y++)
				.fill(GridBagConstraints.HORIZONTAL)
				.weightX(1)
				.build()
		);
		panel.add(
			table,
			new GridBagConstraintBuilder()
				.gridXY(0, y++)
				.fill(GridBagConstraints.BOTH)
				.weightX(0)
				.weightY(1)
				.build()
		);


		panel.add(
			new HtmlLabelBuilder().hx(2, "Graf četností jednotlivých slov").build(),
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(0).build()
		);

		final ChartPanel plot = createPlot(ChartType.PIE);
		ResizableChartPanelWrapper allWordsChartPanel = createChartPanel(plot);
		allWordsChartComponent = changeChartPanel(null, new JPanel(), allWordsChartPanel); //TODO
		y++;

		panel.add(
			new HtmlLabelBuilder().hx(2, "Srovnání četností jednotlivých slov").build(),
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).build()
		);

		Set<String> allWords = model.getAllDomainElements();
		final String[] words = allWords.toArray(new String[allWords.size()]);
		Collator coll = Collator.getInstance(Locale.getDefault());
		coll.setStrength(Collator.PRIMARY);
		Arrays.sort(words, coll);
		final JPanel comboPanel = new JPanel(new GridBagLayout());

		JButton plusComboButton = new JButton("+");
		JButton minusComboButton = new JButton("-");
		PlusMinusButtonListener plusMinusButtonListener = new PlusMinusButtonListener(plusComboButton, minusComboButton, comboPanel, words);
		plusComboButton.addActionListener(plusMinusButtonListener);
		minusComboButton.addActionListener(plusMinusButtonListener);

		//..first add combo panel, then create comboBox which will create and display plot. Finally attach combo and buttons to comboPanel and validate it
		panel.add(comboPanel, new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).fill(GridBagConstraints.HORIZONTAL).anchor(GridBagConstraints.LINE_START).build());
		final JComboBox comboBox = createWordComboBox(words);
		comboPanel.add(comboBox);
		comboPanel.add(plusComboButton);
		comboPanel.add(minusComboButton);
		minusComboButton.setVisible(false);
		comboPanel.validate();

		/*************************************/
		JPanel dummy = new JPanel();
		dummy.setBackground(Color.white);
		panel.add(dummy, new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).weightY(1).fill(GridBagConstraints.BOTH).build());
		return panel;
	}

	private JComboBox createWordComboBox(String[] words) {
		JComboBox comboBox = new JComboBox(words);
		comboBox.setSelectedItem(null); //..set to null to fire listener after new selected item is found
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				String item = e.getItem().toString();
				if (e.getStateChange() == ItemEvent.SELECTED) {
					model.addCompareChartCategory(item);
					refreshPlot();
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					model.removeComparePlotCategory(item);
				}
			}
		});

		for (String word : words) {
			if (!model.isInCompareChartCategories(word)) {
				comboBox.setSelectedItem(word);
				break;

			}
		}

		return comboBox;
	}

	private void refreshPlot() {
		ChartPanel plot = chartFactory.createComparePlot();
		ChartPanelWrapper wrap =  new ChartPanelWrapper(plot).addPlot();
		if (compareChartComponent == null) {
			compareChartComponent = changeChartPanel(null, new JPanel(), wrap); //TODO
			y++;
		} else {
			compareChartComponent = changeChartPanel(compareChartComponent, new JPanel(), wrap); //TODO
		}
	}

	private ResizableChartPanelWrapper createChartPanel(final ChartPanel chartPanel) {
		final ResizableChartPanelWrapper panel = new ResizableChartPanelWrapper(chartPanel);

		lowerFreqBound.removeChangeListener(boundChangedListener);

		boundChangedListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				chartFactory.setDatasetMoreThan(chartPanel, getLowerOccurrenceBound());
			}
		};
		lowerFreqBound.addChangeListener(boundChangedListener);

		JPanel footer1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		footer1.setBackground(Color.white);
		footer1.add(new JLabel("Zobrazovat pouze slova, která se vyskytla alespoň "));
		footer1.add(lowerFreqBound);
		footer1.add(new JLabel(" ×"));

		JPanel footer2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		footer2.setBackground(Color.white);
		final JButton big = new JButton("Zvětšit graf");
		final JButton small = new JButton("Zmenšit graf");
		footer2.add(big);
		footer2.add(small);
		ActionListener l = new ActionListener() {

			Plot plot = chartPanel.getChart().getPlot();

			@Override
			public void actionPerformed(ActionEvent e) {

				int width = 0;
				int height = 0;
				if (e.getSource() == big) {
					width += 100;
					if (plot instanceof PiePlot) {
						height += 100;
					}
				} else if (e.getSource() == small) {
					width -= 100;
					if (plot instanceof PiePlot) {
						height -= 100;
					}
				}
				panel.resizeChart(width, height);

			}
		};
		big.addActionListener(l);
		small.addActionListener(l);

		panel.addPlot(new GridBagConstraintBuilder().fill(GridBagConstraints.BOTH).weightX(1).weightY(1).gridY(0).build());

		panel.addFooter(footer1, new GridBagConstraintBuilder().gridY(0).build());
		panel.addFooter(footer2, new GridBagConstraintBuilder().gridY(1).build());

		return panel;
	}

	public ChartMouseListener createChartMouseListener(final ChartPanel newChartPanel) {

		return new ChartMouseListener() {
			@Override
			public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {

				final ChartType type = (ChartType) newChartPanel.getClientProperty("type");
				final ChartType nextType = ChartType.values()[(type.ordinal() + 1) % ChartType.values().length];
				ChartPanel newOne = createPlot(nextType);
				final ResizableChartPanelWrapper chartPanel = createChartPanel(newOne);
				allWordsChartComponent = changeChartPanel(allWordsChartComponent, new JPanel(), chartPanel); //TODO

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
	}

	@Override
	public CsvData getCsvData() {
		return new WordFrequenciesCsvSaver().saveToCsv(model);
	}

	private class PlusMinusButtonListener implements ActionListener {

		private final JButton plusComboButton;
		private final JButton minusComboButton;
		private final JPanel comboPanel;
		private static final int HIDE_MINUS = 3; //..one combo + plus btn + minus btn
		private final int HIDE_PLUS;
		private final String[] words;

		public PlusMinusButtonListener(JButton plusComboButton, JButton minusComboButton, JPanel comboPanel, String[] words) {

			this.plusComboButton = plusComboButton;
			this.minusComboButton = minusComboButton;
			this.comboPanel = comboPanel;
			this.words = words;

			HIDE_PLUS = words.length + 2;//..combo for each character + plus btn + minus btn
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//..remove plus minus buttons
			comboPanel.remove(plusComboButton);
			comboPanel.remove(minusComboButton);
			if (e.getSource() == plusComboButton) {
				plus();
			} else {
				minus();
			}
			comboPanel.add(plusComboButton);
			comboPanel.add(minusComboButton);

			minusComboButton.setVisible(comboPanel.getComponentCount() > HIDE_MINUS);
			plusComboButton.setVisible(comboPanel.getComponentCount() < HIDE_PLUS);
			comboPanel.validate();

			refreshPlot();
		}

		void plus() {
			comboPanel.add(createWordComboBox(words));
		}

		void minus() {
			int lastComponent = comboPanel.getComponentCount() - 1;
			JComboBox cmbBox = (JComboBox) comboPanel.getComponent(lastComponent);//..last combo
			model.removeComparePlotCategory(cmbBox.getSelectedItem().toString());
			comboPanel.remove(cmbBox);
		}
	}
}
