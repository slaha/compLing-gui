package cz.slahora.compling.gui.panels.characters;

import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.MultipleLinesLabel;
import cz.slahora.compling.gui.model.CharacterFrequencyModel;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.*;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class CharacterFrequencyPanel extends AbstractResultsPanel implements ResultsPanel {

	private static final double MINIMUM = 0.001d;
	private static final double MAXIMUM = 1d;
	private static final double STEP_SIZE = 0.005d;
	/** Just model */
	private final CharacterFrequencyModel model;
	private final JPanel changeChartPanel;
	private final JPanel compareChartPanel;
	/** Current y axis value for GB layout */
	/** Panel for displaying pie or bar plot with sums of characters occurrences */
//	private ChartPanelWrapper allCharactersChartPanel;
	private JComponent allCharactersChartComponent;

	/** Panel for displaying bar plot for selected characters */
	private JComponent compareChartComponent;

	private final CharacterFrequencyChartFactory chartFactory;

	public CharacterFrequencyPanel(Map<WorkingText, CharacterFrequency> characterFrequency) {
		super(new ResultsScrollablePanel());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		this.model = new CharacterFrequencyModel(characterFrequency);
		final String chartTitle = "Zastoupení jednotlivých znaků";
		chartFactory = new CharacterFrequencyChartFactory(model, chartTitle);

		//..top headline
		final JLabel mainHeadlineLabel = new HtmlLabelBuilder().hx(1, "Analýza četností znaků").build();
		mainHeadlineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(mainHeadlineLabel);

		final MultipleLinesLabel introArea = new MultipleLinesLabel();
		introArea.setText(model.getIntroLabelText());
		introArea.setAlignmentX(Component.LEFT_ALIGNMENT);
		//..some info text
		panel.add(introArea);

		final JSpinner jSpinner = new JSpinner(new SpinnerNumberModel(model.getOdchylka(), MINIMUM, MAXIMUM, STEP_SIZE));
		jSpinner.setEditor(new JSpinner.NumberEditor(jSpinner, "0.000"));
		jSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final double value = (Double)jSpinner.getModel().getValue();
				model.setOdchylka(value);
				introArea.setText(model.getIntroLabelText());
			}
		});

		JPanel spinnerPanel = new JPanel(new FlowLayout());
		spinnerPanel.setBackground(Color.white);
		spinnerPanel.add(new JLabel("Směrodatná odchylka: "));
		jSpinner.setPreferredSize(new Dimension(150, 30));
		spinnerPanel.add(jSpinner);
		spinnerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(spinnerPanel);


		//table with character occurrences
		JTable table = new JTable(model.getTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
		table.setAutoCreateRowSorter(true);
		table.setBackground(Color.WHITE);

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(Color.white);
		tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(table, BorderLayout.CENTER);

		tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(tablePanel);

		//pie or bar plot
		final JLabel chartLabelHeadline = new HtmlLabelBuilder().hx(2, "Graf četností jednotlivých znaků").build();
		chartLabelHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(chartLabelHeadline);

		final ChartPanel plot = createPlot(ChartType.PIE);
		ChartPanelWrapper allCharactersChartPanel = wrap(plot);
		changeChartPanel = new JPanel(new BorderLayout());
		changeChartPanel.setBackground(Color.white);
		changeChartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(changeChartPanel);
		allCharactersChartComponent = changeChartPanel(null, changeChartPanel, allCharactersChartPanel);

		//bar plot with selected characters
		final JLabel chartCompareLabelHeadline = new HtmlLabelBuilder().hx(2, "Porovnání jednotlivých básní").build();
		chartCompareLabelHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(chartCompareLabelHeadline);

		Set<String> allCharacters = model.getAllCharacters();
		final String[] characters = allCharacters.toArray(new String[allCharacters.size()]);
		Arrays.sort(characters, CharacterFrequencyModel.CHARACTERS_FIRST_COMPARATOR);
		final JPanel comboPanel = new JPanel(new GridBagLayout());
		comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JButton plusComboButton = new JButton("+");
		JButton minusComboButton = new JButton("-");
		PlusMinusButtonListener plusMinusButtonListener = new PlusMinusButtonListener(plusComboButton, minusComboButton, comboPanel, characters);
		plusComboButton.addActionListener(plusMinusButtonListener);
		minusComboButton.addActionListener(plusMinusButtonListener);

		//..first add combo panel, then create comboBox which will create and display plot. Finally attach combo and buttons to comboPanel and validate it
		panel.add(comboPanel);

		compareChartPanel = new JPanel(new BorderLayout());
		compareChartPanel.setBackground(Color.white);
		compareChartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(compareChartPanel);

		final JComboBox comboBox = createCharacterComboBox(characters);
		comboPanel.add(comboBox);
		comboPanel.add(plusComboButton);
		comboPanel.add(minusComboButton);
		minusComboButton.setVisible(false);
		comboPanel.validate();
	}



	private ChartPanelWrapper wrap(ChartPanel plot) {
		final ChartPanelWrapper wrapper = new ChartPanelWrapper(plot);
		wrapper.addPlot();
		return wrapper;
	}

	private JComboBox createCharacterComboBox(String[] characters) {
		JComboBox comboBox = new JComboBox(characters);
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

		for (String character : characters) {
			if (!model.isInCompareChartCategories(character)) {
				comboBox.setSelectedItem(character);
				break;

			}
		}

		return comboBox;
	}

	private void refreshPlot() {
		ChartPanel plot = chartFactory.createComparePlot();
		ChartPanelWrapper wrap = wrap(plot);
		compareChartComponent = changeChartPanel(compareChartComponent, compareChartPanel, wrap);
	}

	private ChartPanel createPlot(ChartType type) {
		JFreeChart chart = chartFactory.createChart(type);

		return chartFactory.createChartPanel(chart, type, this);
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public CsvData getCsvData() {
		return model.getCsvSaver().saveToCsv(model);
	}

	public ChartMouseListener createChartMouseListener(final ChartPanel newChartPanel) {
		return new ChartMouseListener() {
			@Override
			public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
				final ChartType type = (ChartType)newChartPanel.getClientProperty("type");
				final ChartType nextType = ChartType.values()[ (type.ordinal() + 1) % ChartType.values().length  ];
				ChartPanel newOne = createPlot(nextType);
				ChartPanelWrapper wrapper = wrap(newOne);
				allCharactersChartComponent = changeChartPanel(allCharactersChartComponent, changeChartPanel, wrapper);
			}

			@Override
			public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
			}
		};
	}

	private class PlusMinusButtonListener implements ActionListener {

		private final JButton plusComboButton;
		private final JButton minusComboButton;
		private final JPanel comboPanel;
		private static final int HIDE_MINUS = 3; //..one combo + plus btn + minus btn
		private final int HIDE_PLUS;

		private final String[] characters;

		public PlusMinusButtonListener(JButton plusComboButton, JButton minusComboButton, JPanel comboPanel, String[] characters) {
			this.plusComboButton = plusComboButton;
			this.minusComboButton = minusComboButton;
			this.comboPanel = comboPanel;
			this.characters = characters;

			HIDE_PLUS = characters.length + 2;//..combo for each character + plus btn + minus btn
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
			comboPanel.add(createCharacterComboBox(characters));
		}

		void minus() {
			int lastComponent = comboPanel.getComponentCount() - 1;
			JComboBox cmbBox = (JComboBox) comboPanel.getComponent(lastComponent);//..last combo
			model.removeComparePlotCategory(cmbBox.getSelectedItem().toString());
			comboPanel.remove(cmbBox);
		}
	}

}