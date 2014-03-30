package cz.slahora.compling.gui.panels.characters;

import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.model.CharacterFrequencyModel;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.*;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

public class CharacterFrequencyPanel implements ResultsPanel {



	private enum ChartType {
		PIE, XY
	}

	/** The whole panel */
	private final JPanel panel;

	/** Just model */
	private final CharacterFrequencyModel model;

	/** Current y axis value for GB layout */
	private int y = 0;

	/** Panel for displaying pie or bar plot with sums of characters occurrences */
	private ChartPanel allCharactersChartPanel;

	/** Panel for displaying bar plot for selected characters */
	private ChartPanel compareChartPanel;

	public CharacterFrequencyPanel(Map<WorkingText, CharacterFrequency> characterFrequency) {
		this.panel = new JPanel(new GridBagLayout());
		panel.setBorder(new EmptyBorder(10, 10, 10, 10)); //..padding

		this.model = new CharacterFrequencyModel(characterFrequency);

		panel.setBackground(Color.WHITE);
		//..top headline
		panel.add(
			new HtmlLabelBuilder().hx(1, "Analýza četností znaků").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL)
				.weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		//..some info text
		panel.add(
			createIntroLabel(),
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
				.anchor(GridBagConstraints.NORTH)
				.weightx(1)
				.weighty(1)
				.build()
		);

		//pie or bar plot
		panel.add(
			new HtmlLabelBuilder().hx(2, "Graf četností jednotlivých znaků").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).build()
		);

		allCharactersChartPanel = createPlot(ChartType.PIE);
		changeChartPanel(null, allCharactersChartPanel);

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
		ChartPanel plot = createPlot(model.getAllCompareChartCategories());
		changeChartPanel(compareChartPanel, plot);
		compareChartPanel = plot;
	}

	private void changeChartPanel(ChartPanel old, final ChartPanel chartPanel) {
		int currentY;
		if (old == null) {
			currentY = y++;
 		} else {
			currentY = (Integer)old.getClientProperty("y");
			panel.remove(old);
		}

		chartPanel.putClientProperty("y", currentY);
		panel.add(
			chartPanel,
			new GridBagConstraintBuilder().gridxy(0, currentY).fill(GridBagConstraints.BOTH).anchor(GridBagConstraints.NORTH)
				.weightx(1).weighty(1).build()
		);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				panel.invalidate();
				panel.validate();
				panel.repaint();
			}
		});

	}

	private ChartPanel createPlot(ChartType type) {
		final String chartTitle = "Zastoupení jednotlivých znaků";
		JFreeChart chart;
		switch (type) {
			case PIE:
				chart = ChartFactory.createPieChart(chartTitle, model.getPieDataSet(), true, true, Locale.getDefault());
				//..remove shadows
				PiePlot piePlot = (PiePlot) chart.getPlot();
				piePlot.setShadowPaint(null);
				break;
			case XY:
				chart = ChartFactory.createBarChart(chartTitle, "Jednotlivé znaky", "Četnost", model.getBarDataSet(), PlotOrientation.VERTICAL, true, true, true);
				BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
				//..black bars without shadow and gradient
				renderer.setSeriesPaint(0, Color.black);
				renderer.setShadowVisible(false);
				renderer.setBarPainter(new StandardBarPainter());
				break;
			default:
				throw new IllegalArgumentException("WTF?? ChartType " + type + " not recognized");
		}

		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setBackgroundPaint(Color.white);

		final ChartPanel newChartPanel = new ChartPanel(chart, 800, 800, 500, 500, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false, true, true, true, true, true);
		newChartPanel.setBackground(Color.white);
		newChartPanel.putClientProperty("type", type);
		newChartPanel.addChartMouseListener(new ChartMouseListener() {
			@Override
			public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
				ChartPanel old = allCharactersChartPanel;
				final ChartType type = (ChartType)newChartPanel.getClientProperty("type");
				final ChartType nextType = ChartType.values()[ (type.ordinal() + 1) % ChartType.values().length  ];
				ChartPanel newOne = createPlot(nextType);
				changeChartPanel(old, newOne);
				allCharactersChartPanel = newOne;
			}

			@Override
			public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
			}
		});

		return newChartPanel;
	}

	private ChartPanel createPlot(Set<String> set) {
		final String chartTitle = "Srovnání zastoupení znaku " + set.toString();
		JFreeChart chart = ChartFactory.createBarChart(chartTitle, "Četnost", "Texty", model.getBarDataSetFor(set.toArray(new String[set.size()])), PlotOrientation.VERTICAL, true, true, true);
		BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
		//..without shadow and gradient
		renderer.setShadowVisible(false);
		renderer.setBarPainter(new StandardBarPainter());
		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setBackgroundPaint(Color.white);
		final ChartPanel newChartPanel = new ChartPanel(chart, 800, 800, 500, 500, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false, true, true, true, true, true);
		newChartPanel.setBackground(Color.white);
		return newChartPanel;
	}

	private JLabel createIntroLabel() {
		String str = model.getTextsCount() == 1 ? "Byl analyzován %d text" :
			model.getTextsCount() >= 5 ? "Bylo analyzováno %d textů" : "Byly analyzovány %d texty";

		str += ":";
		HtmlLabelBuilder builder = new HtmlLabelBuilder()
			.p(str, model.getTextsCount())
			.startBulletList();

		for (WorkingText wt : model.getWorkingTexts()) {
			builder.li(wt.getName());
		}
		builder.stopBulletList();

		str = "V " +  (model.getTextsCount() == 1 ? "tomto textu" : "těchto textech");
		str += (model.getCharactersCount() == 1 ? " byl nalezen"
				: model.getCharactersCount() >= 5 ? " bylo nalezeno" : " byly nalezeny"
		) + " %d" + (model.getCharactersCount() == 1 ? " znak"
				: model.getCharactersCount() >= 5 ? " různých znaků" : " různé znaky");

		builder.p(str, model.getCharactersCount());


		java.util.List<String> mostOftenCharacters = model.getMostOftenCharacter();
		StringBuilder sb = new StringBuilder();
		for (String s : mostOftenCharacters) {
			sb.append('\'').append(s).append("', ");
		}
		if (sb.length() >= 2) {
			sb.setLength(sb.length() - 2);
		}
		str = "Nejčastěji nalezeným znakem " + (mostOftenCharacters.size() == 1 ? "byl znak" : "byly znaky") + " %s";
		str += ", " + (mostOftenCharacters.size() == 1 ? "který byl nalezen celkem %d×." : "které se vyskytly celkem  %d×.");
		builder.p(str, sb.toString(), model.getMaxOccurrence());

		return builder.margin(20).build();
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public CsvData getCsvData() {
		return model.getCsvData();
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
			JComboBox cmbBox = createCharacterComboBox(characters);
			comboPanel.add(cmbBox);
		}

		void minus() {
			int lastComponent = comboPanel.getComponentCount() - 1;
			JComboBox cmbBox = (JComboBox) comboPanel.getComponent(lastComponent);//last combo
			model.removeComparePlotCategory(cmbBox.getSelectedItem().toString());
			comboPanel.remove(cmbBox);
		}
	}

}