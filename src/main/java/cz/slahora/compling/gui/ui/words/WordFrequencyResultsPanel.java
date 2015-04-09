package cz.slahora.compling.gui.ui.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.analysis.ToggleHeader;
import cz.slahora.compling.gui.analysis.assonance.NonEditableTable;
import cz.slahora.compling.gui.analysis.words.WordTextAnalysisType;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.ui.*;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class WordFrequencyResultsPanel<T> extends AbstractResultsPanel implements ResultsPanel {

	private final IWordFrequenciesModel<T> model;
	private final WordFrequencyChartFactory<T> chartFactory;
	private final WordTextAnalysisType analysisType;

	private JComponent allWordsChartComponent;
	private JComponent compareChartComponent;
	private final JPanel allWordsChartParent = new JPanel(new BorderLayout());
	private final JPanel compareChartParent  = new JPanel(new BorderLayout());


	private final JSpinner lowerFreqBound;
	private ChangeListener boundChangedListener;

	public WordFrequencyResultsPanel(WordTextAnalysisType analysisType, Map<WorkingText, IWordFrequency> wordFrequencies, Factory<T> factory) {
		super(new ResultsScrollablePanel());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		this.analysisType = analysisType;
		switch (analysisType) {
			case WORD:
				this.model = factory.createModel(wordFrequencies);
				this.chartFactory = factory.createChartFactory(model);
				break;
			case WORD_LENGHT:
				this.model = factory.createModel(wordFrequencies);
				this.chartFactory = factory.createChartFactory(model);
				break;
			default:
				throw new IllegalStateException("Cannot create model for type " + analysisType);
		}


		SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, model.getFilterMaxValue(), 1);
		lowerFreqBound = new JSpinner(spinnerModel);
	}

	@Override
	public JPanel getPanel() {
		panel.removeAll();

		JLabel headline1 = new HtmlLabelBuilder().hx(1, "Frekvence výskytu slov").build();
		headline1.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(headline1);

		MultipleLinesLabel text = new MultipleLinesLabel(model.getMainParagraphText());
		text.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(text);

		//table with words occurrences
		JTable table = new NonEditableTable(model.getTableModel());
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setAutoCreateRowSorter(true);
		table.setBackground(Color.WHITE);
		table.getRowSorter().toggleSortOrder(1); //..sort by freq of all (or the only one if only for one text)
		table.getRowSorter().toggleSortOrder(1); //..sort by freq of all - desc

		final JTableHeader tableHeader = table.getTableHeader();
		tableHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

		table.setAlignmentX(Component.LEFT_ALIGNMENT);
		JXCollapsiblePane tablePanel = new JXCollapsiblePane(new BorderLayout());
		tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tablePanel.setCollapsed(true);
		tablePanel.setBackground(Color.white);

		tablePanel.add(tableHeader, BorderLayout.NORTH);
		tablePanel.add(table, BorderLayout.CENTER);

		ToggleHeader tableHeadline;
		if (analysisType == WordTextAnalysisType.WORD) {
			tableHeadline = new ToggleHeader(tablePanel, new HtmlLabelBuilder().hx(2, "Tabulka četností výskytu slov").build().getText());
		} else {
			tableHeadline = new ToggleHeader(tablePanel, new HtmlLabelBuilder().hx(2, "Výskyt slov dle délky slova").build().getText());
		}
		tableHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);

		panel.add(createVerticalSpace());
		panel.add(tableHeadline);
		panel.add(tablePanel);

		if (analysisType == WordTextAnalysisType.WORD_LENGHT) {

			final JXCollapsiblePane chiSquareTestsPanel = new JXCollapsiblePane();
			chiSquareTestsPanel.setBackground(Color.white);
			chiSquareTestsPanel.setLayout(new BoxLayout(chiSquareTestsPanel.getContentPane(), BoxLayout.Y_AXIS));

			chiSquareTestsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			ToggleHeader toggle = new ToggleHeader(chiSquareTestsPanel,
				new HtmlLabelBuilder().hx(2, "\u03C7<sup>2</sup> test").build().getText());
			toggle.setAlignmentX(Component.LEFT_ALIGNMENT);

			panel.add(createVerticalSpace());
			panel.add(toggle);

			final Set<WorkingText> allTexts = model.getAllTexts();
			if (allTexts.size() > 1) {
				WorkingText texts[] = allTexts.toArray(new WorkingText[allTexts.size()]);
				final JPanel selectTextPanel = new JPanel();
				selectTextPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				JComboBox<WorkingText> selectTextCombo = new JComboBox<WorkingText>(texts);
				selectTextCombo.setSelectedItem(null);
				selectTextCombo.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							WorkingText workingText = (WorkingText) e.getItem();
							createChiSquareTest(workingText, chiSquareTestsPanel, selectTextPanel);
							chiSquareTestsPanel.validate();
						}

					}
				});

				selectTextCombo.add(new JLabel("Zvolte text k testu:"));
				selectTextPanel.add(selectTextCombo);

				selectTextCombo.setSelectedIndex(0);
			} else {
				createChiSquareTest(allTexts.iterator().next(), chiSquareTestsPanel, null);
			}

			panel.add(chiSquareTestsPanel);
		}

		final JLabel headlineWordFreqs = new HtmlLabelBuilder().hx(2, "Graf četností jednotlivých slov").build();
		headlineWordFreqs.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(createVerticalSpace());
		panel.add(headlineWordFreqs);
		allWordsChartParent.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(allWordsChartParent);

		final ChartPanel plot = createPlot(ChartType.PIE);
		ResizableChartPanelWrapper allWordsChartPanel = createChartPanel(plot);
		allWordsChartComponent = changeChartPanel(null, allWordsChartParent, allWordsChartPanel);

		final JLabel headlineCompareWords = new HtmlLabelBuilder().hx(2, "Srovnání četností jednotlivých slov").build();
		headlineCompareWords.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(createVerticalSpace());
		panel.add(headlineCompareWords);

		final T[] words = model.getAllDomainElements();
		final Comparator<T> coll = model.getDomainElementsComparator();
		Arrays.sort(words, coll);
		final JPanel comboPanel = new JPanel(new GridBagLayout());

		JButton plusComboButton = new JButton("+");
		JButton minusComboButton = new JButton("-");
		PlusMinusButtonListener plusMinusButtonListener = new PlusMinusButtonListener(plusComboButton, minusComboButton, comboPanel, words);
		plusComboButton.addActionListener(plusMinusButtonListener);
		minusComboButton.addActionListener(plusMinusButtonListener);

		//..first add combo panel, then create comboBox which will create and display plot. Finally attach combo and buttons to comboPanel and validate it
		comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(comboPanel);

		compareChartParent.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(compareChartParent);

		final JComboBox comboBox = createWordComboBox(words);
		comboPanel.add(comboBox);
		comboPanel.add(plusComboButton);
		comboPanel.add(minusComboButton);
		minusComboButton.setVisible(false);
		comboPanel.validate();

		/*************************************/
		JPanel dummy = new JPanel();
		dummy.setBackground(Color.white);
		panel.add(dummy);
		return panel;
	}

	private void createChiSquareTest(final WorkingText workingText, JXCollapsiblePane chiSquareTestsPanel, JPanel selectTextPanel) {
		chiSquareTestsPanel.removeAll();

		if (selectTextPanel != null) {
			chiSquareTestsPanel.add(selectTextPanel);
//			createVerticalSpace(15) cannot be used because of background
			JPanel p = new JPanel();
			p.setBackground(Color.white);
			p.setAlignmentX(Component.LEFT_ALIGNMENT);
			p.setMinimumSize(new Dimension(1, 15));
			p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
			chiSquareTestsPanel.add(p);
		}

		final WordLengthFrequenciesModel.ChiSquare chiSquareModel = model.getChiSquareFor(workingText);
		//table with words occurrences
		JTable table = new NonEditableTable(chiSquareModel.tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
		table.setAutoCreateRowSorter(true);
		table.setBackground(Color.WHITE);

		for (int i = 0; i < table.getColumnCount(); i++) {
			if (Double.class == table.getColumnClass(i)) {
				table.getColumnModel().getColumn(i).setCellRenderer(new ChiSquareTableModel.DecimalFormatRenderer());
			}
		}

		table.invalidate();

		table.getTableHeader().setBackground(Color.white);
		table.getTableHeader().setAlignmentX(Component.LEFT_ALIGNMENT);
		table.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel resultPanel = new JPanel();
		resultPanel.setBackground(Color.white);
		resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

		final String chiSquareResultText = "Hodnota χ-kvadrátu pro %d stupňů volnosti je %.2f. Krititcká hodnota na hladině významnosti α=%.2f je %.2f.";
		final String chiSquareResultOkTemplate = chiSquareResultText + "\n\n" + "Proto zamítáme nulovou hypotézu o tom, že rozdělení četnosti slov dle délky odpovídá %s rozdělení.";
		final String chiSquareResultFailTemplate = chiSquareResultText + "\n\n" + "Proto nelze vyloučit nulovou hypotézu o tom, že rozdělení četnosti slov dle délky odpovídá %s rozdělení.";

		JLabel chiSquareResultHeadline = new JLabel("<html><h2>Výsledek χ<sup>2</sup> testu</h2></html>");
		chiSquareResultHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);

		final MultipleLinesLabel chiSquareResult = new MultipleLinesLabel();
		chiSquareResult.setAlignmentX(Component.LEFT_ALIGNMENT);
		String t = chiSquareModel.chiTest ? chiSquareResultOkTemplate : chiSquareResultFailTemplate;
		chiSquareResult.setText(String.format(t, chiSquareModel.degreesOfFreedom, chiSquareModel.chiSquareValue, chiSquareModel.alpha, chiSquareModel.chiSquareCriticalValue, chiSquareModel.distribution));

		final SpinnerNumberModel alphaSpinnerModel = new SpinnerNumberModel(chiSquareModel.alpha, 0.009, 0.5, 0.01);
		JSpinner alphaSpinner = new JSpinner(alphaSpinnerModel);
		alphaSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
		alphaSpinner.setMaximumSize(new Dimension(300, 100));
		alphaSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				chiSquareModel.alpha = new Double(alphaSpinnerModel.getValue().toString());
				model.getChiSquareFor(workingText, chiSquareModel);

				String t = chiSquareModel.chiTest ? chiSquareResultOkTemplate : chiSquareResultFailTemplate;
				chiSquareResult.setText(String.format(t, chiSquareModel.degreesOfFreedom, chiSquareModel.chiSquareValue, chiSquareModel.alpha, chiSquareModel.chiSquareCriticalValue, chiSquareModel.distribution));

			}
		});

		resultPanel.add(chiSquareResultHeadline);
		resultPanel.add(chiSquareResult);
		resultPanel.add(createVerticalSpace(15));

		JPanel spinnerPanel = new JPanel();
		spinnerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		final JLabel levelLabel = new JLabel("Hladina významnosti α ");
		levelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		spinnerPanel.add(levelLabel);
		spinnerPanel.add(alphaSpinner);
		resultPanel.add(spinnerPanel);

		chiSquareTestsPanel.add(table.getTableHeader());
		chiSquareTestsPanel.add(table);
		chiSquareTestsPanel.add(resultPanel);
	}

	private JComboBox createWordComboBox(T[] comboValues) {
		final JComboBox<T> comboBox = new JComboBox<T>(comboValues);
		comboBox.setSelectedItem(null); //..set to null to fire listener after new selected item is found
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				T item = (T) e.getItem();
				if (item == null) {
					return;
				}

				if (e.getStateChange() == ItemEvent.SELECTED) {
					model.addCompareChartCategory(item);
					refreshPlot();
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					model.removeComparePlotCategory(item);
				}
			}
		});

		for (T value : comboValues) {
			if (!model.isInCompareChartCategories(value)) {
				comboBox.setSelectedItem(value);
				break;

			}
		}

		return comboBox;
	}

	private void refreshPlot() {
		ChartPanel plot = chartFactory.createComparePlot(analysisType);
		ChartPanelWrapper wrap =  new ChartPanelWrapper(plot).addPlot();
		compareChartComponent = changeChartPanel(compareChartComponent, compareChartParent, wrap);
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
				allWordsChartComponent = changeChartPanel(allWordsChartComponent, allWordsChartParent, chartPanel);
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
		private final T[] words;

		public PlusMinusButtonListener(JButton plusComboButton, JButton minusComboButton, JPanel comboPanel, T[] words) {

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
			model.removeComparePlotCategory((T)cmbBox.getSelectedItem());
			comboPanel.remove(cmbBox);
		}
	}

	public interface Factory<T> {

		IWordFrequenciesModel<T> createModel(Map<WorkingText, IWordFrequency> wordFrequencies);

		WordFrequencyChartFactory<T> createChartFactory(IWordFrequenciesModel<T> model);
	}

	public static class WordFactory implements Factory<String> {
		@Override
		public IWordFrequenciesModel<String> createModel(Map<WorkingText, IWordFrequency> wordFrequencies) {
			return new WordFrequenciesModel(wordFrequencies);
		}

		@Override
		public WordFrequencyChartFactory<String> createChartFactory(IWordFrequenciesModel<String> model) {
			return new WordFrequencyChartFactory<String>(model, "Zastoužpení jednotlivých slov");
		}
	}

	public static class WordLengthFactory implements Factory<Integer> {
		@Override
		public IWordFrequenciesModel<Integer> createModel(Map<WorkingText, IWordFrequency> wordFrequencies) {
			return new WordLengthFrequenciesModel(wordFrequencies);
		}

		@Override
		public WordFrequencyChartFactory<Integer> createChartFactory(IWordFrequenciesModel<Integer> model) {
			return new WordFrequencyChartFactory<Integer>(model, "Zastoupení délek jednotlivých slov");
		}
	}
}
