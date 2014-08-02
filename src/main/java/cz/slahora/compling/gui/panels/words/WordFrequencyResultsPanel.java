package cz.slahora.compling.gui.panels.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;

public class WordFrequencyResultsPanel implements ResultsPanel {

	private final JPanel panel;
	private final WordFrequenciesController controller;

	public WordFrequencyResultsPanel(Map<WorkingText, IWordFrequency> wordFrequencies) {
		this.controller = new WordFrequenciesController(wordFrequencies);

		this.panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
	}

	@Override
	public JPanel getPanel() {
		panel.removeAll();

		int y = 0;

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

		/*************************************/
		JPanel dummy = new JPanel();
		dummy.setBackground(Color.white);
		panel.add(dummy, new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).weighty(1).fill(GridBagConstraints.BOTH).build());
		return panel;
	}

	@Override
	public CsvData getCsvData() {
		return new CsvData();
	}
}
