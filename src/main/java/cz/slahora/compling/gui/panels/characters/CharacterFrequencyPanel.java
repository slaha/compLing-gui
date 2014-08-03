package cz.slahora.compling.gui.panels.characters;

import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.model.CharacterFrequencyModel;
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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class CharacterFrequencyPanel extends AbstractResultsPanel implements ResultsPanel {

	/** Just model */
	private final CharacterFrequencyModel model;

	/** Current y axis value for GB layout */
	private int y = 0;

	/** Panel for displaying pie or bar plot with sums of characters occurrences */
	private ChartPanelWrapper allCharactersChartPanel;

	/** Panel for displaying bar plot for selected characters */
	private ChartPanelWrapper compareChartPanel;

	private final CharacterFrequencyChartFactory chartFactory;

	public CharacterFrequencyPanel(Map<WorkingText, CharacterFrequency> characterFrequency) {
		super(new JPanel(new GridBagLayout()));

		this.model = new CharacterFrequencyModel(characterFrequency);
		final String chartTitle = "Zastoupení jednotlivých znaků";
		chartFactory = new CharacterFrequencyChartFactory(model, chartTitle);

		//..top headline
		panel.add(
			new HtmlLabelBuilder().hx(1, "Analýza četností znaků").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL)
				.weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		//..some info text
		panel.add(
			new JLabel(model.getIntroLabelText()),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL)
				.weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		//table with character occurrences
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

		//pie or bar plot
		panel.add(
			new HtmlLabelBuilder().hx(2, "Graf četností jednotlivých znaků").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).build()
		);

		final ChartPanel plot = createPlot(ChartType.PIE);
		allCharactersChartPanel = wrap(plot);
		putChartPanel(y, allCharactersChartPanel);
		y++;

		//bar plot with selected characters
		panel.add(
			new HtmlLabelBuilder().hx(2, "Porovnání jednotlivých básní").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL)
				.weightx(1).build()
		);

		Set<String> allCharacters = model.getAllCharacters();
		final String[] characters = allCharacters.toArray(new String[allCharacters.size()]);
		Arrays.sort(characters, CharacterFrequencyModel.CHARACTERS_FIRST_COMPARATOR);
		final JPanel comboPanel = new JPanel(new GridBagLayout());

		JButton plusComboButton = new JButton("+");
		JButton minusComboButton = new JButton("-");
		PlusMinusButtonListener plusMinusButtonListener = new PlusMinusButtonListener(plusComboButton, minusComboButton, comboPanel, characters);
		plusComboButton.addActionListener(plusMinusButtonListener);
		minusComboButton.addActionListener(plusMinusButtonListener);

		//..first add combo panel, then create comboBox which will create and display plot. Finally attach combo and buttons to comboPanel and validate it
		panel.add(comboPanel, new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).fill(GridBagConstraints.HORIZONTAL).anchor(GridBagConstraints.LINE_START).build());
		final JComboBox comboBox = createCharacterComboBox(characters);
		comboPanel.add(comboBox);
		comboPanel.add(plusComboButton);
		comboPanel.add(minusComboButton);
		minusComboButton.setVisible(false);
		comboPanel.validate();
	}

	private ChartPanelWrapper wrap(ChartPanel plot) {
		final ChartPanelWrapper wrapper = new ChartPanelWrapper(plot);
		wrapper.add(plot);
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
		if (compareChartPanel == null) {
			putChartPanel(y, wrap);
			y++;
		} else {
			changeChartPanel(compareChartPanel, wrap);
		}
		compareChartPanel = wrap;
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
				ChartPanelWrapper old = allCharactersChartPanel;
				final ChartType type = (ChartType)newChartPanel.getClientProperty("type");
				final ChartType nextType = ChartType.values()[ (type.ordinal() + 1) % ChartType.values().length  ];
				ChartPanel newOne = createPlot(nextType);
				ChartPanelWrapper wrapper = wrap(newOne);
				changeChartPanel(old, wrapper);
				allCharactersChartPanel = wrapper;
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