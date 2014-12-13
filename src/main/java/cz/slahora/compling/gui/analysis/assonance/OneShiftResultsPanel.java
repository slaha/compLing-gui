package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.CompLing;
import cz.slahora.compling.gui.analysis.ToggleHeader;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.ui.ResultsPanel;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class OneShiftResultsPanel extends AbsAssonanceResultsPanel implements ResultsPanel {

	private static final double ALPHA = 0.95;
	private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.####");
	private static final GridBagConstraints C = new GridBagConstraintBuilder()
		.fill(GridBagConstraints.HORIZONTAL)
		.gridX(0)
		.weightX(1)
		.build();

	public OneShiftResultsPanel(final OneShiftModel model) {
		super(new JPanel(new GridBagLayout()), ALPHA);

		JLabel topHeadline = new HtmlLabelBuilder().hx(1, "Asonance pro posun o %d %s", model.getShift(), model.getVocalText(model.getShift()) ).build();

		panel.add(topHeadline, C);

		JTable resultsTable = createResultsTable(model, model.getGroupsNames());

		JXCollapsiblePane resultsPanel = new JXCollapsiblePane();
		resultsPanel.setLayout(new GridBagLayout());
		resultsPanel.setCollapsed(true);

		ToggleHeader resultsPanelHeader = new ToggleHeader(resultsPanel, new HtmlLabelBuilder().hx(2, "Výsledky asonance").build().getText());

		resultsPanel.add(resultsTable.getTableHeader(), C);
		resultsPanel.add(resultsTable, C);

		panel.add(resultsPanelHeader, C);
		panel.add(resultsPanel, C);

		if (model.isTestingPossible()) {
			final BartlettResult bartlettResult = model.getBartlettResult(ALPHA);

			List<JComponent> components;
			JLabel bartlettHeadline = new HtmlLabelBuilder().hx(2, "Bartlettův test").build();

			JLabel bartlettValueB = new HtmlLabelBuilder().text("Veličina B Bartlettova testu: B=%s", DECIMAL_FORMAT.format(bartlettResult.getBartlettB())).build();
			JLabel chiSquareCriticalValue = new HtmlLabelBuilder().text("Kritická hodnota χ").sup("2").sub("" + ALPHA).text("(%d - 1) = %s", bartlettResult.getBartlettK(), DECIMAL_FORMAT.format(bartlettResult.getCriticalValue())).build();

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

						TableLabels scheffeLabel = new TableLabels() {
							@Override
							public String labelFor(int index) {
								return model.getGroupsNames()[index - 1];
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
							return model.getGroupsNames()[index - 1];
						}
					};
					components = createKruskalWallisLayout(kwResult, labels);

					if (kwResult.getTestMethodResult() == TestMethodResult.H0_REJECTED) {

						final int n = (int) kwResult.getN();
						final int k = (int) kwResult.getK();
						final ScheffeResult scheffeResult = model.getScheffeResult(ALPHA, n, k);

						TableLabels scheffeLabel = new TableLabels() {
							@Override
							public String labelFor(int index) {
								return model.getGroupsNames()[index-1];
							}
						};
						components.addAll(doScheffe(scheffeResult,scheffeLabel));
					}

					label = new HtmlLabelBuilder().b("Na hladině významnosti α=" + ALPHA +  " zamítáme nulovou hypotézu o rovnosti rozptylů.").build();
					components.add(0, label);
					break;
				default:
					components = Collections.emptyList();
					break;
			}


			panel.add(bartlettHeadline, C);
			panel.add(bartlettValueB, C);
			panel.add(chiSquareCriticalValue, C);
			for (JComponent component : components) {
				panel.add(component, C);
			}


		} else {
			JLabel noTesting = new HtmlLabelBuilder().text("Pro testování shody rozptylů je nutné, aby velikost jednotlivých výběrů ").i("n<sub>i</sub>").text(" byla větší než 6.").build();
			panel.add(noTesting, C);
		}

		panel.add(new JLabel(), new GridBagConstraintBuilder().fill(GridBagConstraints.VERTICAL).weightY(1).gridX(0).build());
	}

	private JTable createResultsTable(OneShiftModel model, String[] groupNames) {
		Object[][] table = new Object[groupNames.length][];

		final int shift = model.getShift();

		int maxGroupSize = 0;

		for (int row = 0; row < table.length; row++) {
			final String groupName = groupNames[row];
			final Selections.Selection group = model.getGroup(groupName);
			final int groupSize = group.getSize();
			final Object[] rowData = new Object[1 + groupSize];

			maxGroupSize = Math.max(maxGroupSize, groupSize);

			rowData[0] = groupName;

			final Iterator<Map.Entry<WorkingText, CompLing>> it = group.iterator();
			int index = 1;
			while (it.hasNext()) {
				Map.Entry<WorkingText, CompLing> entry = it.next();

				WorkingText uid = entry.getKey();
				double value = model.getAssonanceFor(uid, shift);
				Object formattedValue = DECIMAL_FORMAT.format(value);
				rowData[index++] = formattedValue;
			}
			table[row] = rowData;
		}

		Object[] columnNames = new Object[maxGroupSize + 1];
		columnNames[0] = "";
		for (int i = 1; i < columnNames.length; i++) {
			columnNames[i] = "Báseň " + i;
		}
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
		header.setDefaultRenderer(new HeaderCellRenderer(t));

		t.getColumnModel().getColumn(0).setCellRenderer(new FirstColumnRenderer());

		return t;
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public CsvData getCsvData() {
		//TODO implement
		return null;
	}
}
