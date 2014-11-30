package cz.slahora.compling.gui.analysis.assonance;

import cz.slahora.compling.gui.analysis.ToggleHeader;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collections;
import java.util.List;

class DifferentShiftsResultsPanel extends AbsAssonanceResultsPanel implements ResultsPanel {


	private static final double ALPHA = 0.95;
	private final DifferentShiftsModel model;

	public DifferentShiftsResultsPanel(DifferentShiftsModel model) {
		super(new JPanel(), ALPHA);

		this.model = model;

		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		WorkingText[] poemNames = model.getPoemNames();

		int maxStep = model.getLowestMaxStep();

		JTable resultsTable = createResultsTable(poemNames, maxStep);

		JXCollapsiblePane resultsPanel = new JXCollapsiblePane();
		resultsPanel.setLayout(new GridBagLayout());
		resultsPanel.setCollapsed(true);

		ToggleHeader resultsPanelHeader = new ToggleHeader(resultsPanel, new HtmlLabelBuilder().hx(2, "Výsledky asonance").build().getText());

		resultsPanel.add(resultsTable.getTableHeader(), c);
		resultsPanel.add(resultsTable, c);

		panel.add(resultsPanelHeader, c);
		panel.add(resultsPanel, c);

		if (model.isTestingPossible()) {

			final BartlettResult bartlettResult = model.getBartlettResult(ALPHA);

			JLabel bartlettHeadline = new HtmlLabelBuilder().hx(2, "Bartlettův test").build();

			JLabel bartlettValueB = new HtmlLabelBuilder().text("Veličina B Bartlettova testu: B=%s", DECIMAL_FORMAT.format(bartlettResult.getBartlettB())).build();
			JLabel chiSquareCriticalValue = new HtmlLabelBuilder().text("Kritická hodnota χ").sup("2").sub("" + ALPHA).text("(%d - 1) = %s", bartlettResult.getBartlettK(), DECIMAL_FORMAT.format(bartlettResult.getCriticalValue())).build();

			List<JComponent> components;
			switch (bartlettResult.getTestMethodResult()) {

				case H0_NOT_REJECTED:

					AnovaResult anovaResult = model.getAnovaResult(ALPHA);
					components = createAnovaLayout(anovaResult);
					JLabel label = new HtmlLabelBuilder().b("Nulovou hypotézu o rovnosti rozptylů nezamítáme.").build();
					components.add(0, label);

					if (anovaResult.getTestMethodResult() == TestMethodResult.H0_REJECTED) {

						final int n = (int) anovaResult.getSeDegreesOfFreedom();
						final int k = (int) anovaResult.getSaDegreesOfFreedom();
						final ScheffeResult scheffeResult = model.getScheffeResult(ALPHA, n, k);

						final String lbl = "Posun o %d";
						TableLabels scheffeLabel = new TableLabels() {
							@Override
							public String labelFor(int index) {
								return String.format(lbl, index);
							}
						};
						components.addAll(doScheffe(scheffeResult, scheffeLabel));
					}

					break;
				case H0_REJECTED:

					KruskalWallisResult kwResult = model.getKruskalWallisResult(ALPHA);
					TableLabels labels = new TableLabels() {
						@Override
						public String labelFor(int index) {
							return index + ". výběr";
						}
					};
					components = createKruskalWallisLayout(kwResult, labels);

					if (kwResult.getTestMethodResult() == TestMethodResult.H0_REJECTED) {

						final int n = (int) kwResult.getN();
						final int k = (int) kwResult.getK();
						final ScheffeResult scheffeResult = model.getScheffeResult(ALPHA, n, k);

						final String lbl = "Posun o %d";
						TableLabels scheffeLabel = new TableLabels() {
							@Override
							public String labelFor(int index) {
								return String.format(lbl, index);
							}
						};
						components.addAll(doScheffe(scheffeResult, scheffeLabel));
					}

					label = new HtmlLabelBuilder().b("Na hladině významnosti α=" + ALPHA +  " zamítáme nulovou hypotézu o rovnosti rozptylů.").build();
					components.add(0, label);
					break;
				default:
					components = Collections.emptyList();
					break;
			}

			panel.add(bartlettHeadline, c);
			panel.add(bartlettValueB, c);
			panel.add(chiSquareCriticalValue, c);
			for (JComponent component : components) {
				panel.add(component, c);
			}

		} else {
			JLabel noTesting = new HtmlLabelBuilder().text("Pro testování shody rozptylů je nutné, aby velikost jednotlivých výběrů ").i("n<sub>i</sub>").text(" byla větší než 6.").build();
			resultsPanel.add(noTesting, c);
		}

		GridBagConstraints cc = (GridBagConstraints) c.clone();
		cc.weighty = 1;
		cc.fill = GridBagConstraints.BOTH;
		final JPanel dummy = new JPanel();
		dummy.setBackground(Color.white);
		panel.add(dummy, cc);
	}

	private JTable createResultsTable(WorkingText[] poemNames, int maxStep) {
		Object[][] table = new Object[maxStep][poemNames.length + 1];

		for (int row = 0; row < table.length; row++) {
			for (int column = 0; column < table[row].length; column++) {

				Object o;
				if (column == 0) {
					o = "Posun o " + (row + 1);

				} else {
					WorkingText uid = poemNames[column - 1];
					final int shift = row + 1;
					double value = model.getAssonanceFor(uid, shift);
					o = DECIMAL_FORMAT.format(value);
				}
				table[row][column] = o;
			}
		}

		Object[] columnNames = new Object[table[0].length];
		columnNames[0] = "";
		System.arraycopy(poemNames, 0, columnNames, 1, poemNames.length);
		JTable t = new NonEditableTable(table, columnNames);

		JTableHeader header = new JTableHeader(t.getColumnModel()) {
			@Override public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = (int) (d.height * 1.4);
				return d;
			}
		};

		t.setTableHeader(header);
		t.setRowHeight((int) (t.getRowHeight() * 1.4));
		header.setDefaultRenderer(new HeaderCellRenderer());

		t.getColumnModel().getColumn(0).setCellRenderer(new FirstColumnRenderer());

		return t;
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public CsvData getCsvData() {
		return null;
	}

}
