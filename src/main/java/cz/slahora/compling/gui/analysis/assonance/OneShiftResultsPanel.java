package cz.slahora.compling.gui.analysis.assonance;

import cz.slahora.compling.gui.analysis.ToggleHeader;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.ui.ResultsPanel;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

class OneShiftResultsPanel extends AbsAssonanceResultsPanel implements ResultsPanel {

	private static final double ALPHA = 0.95;
	private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.####");
	private static final GridBagConstraints C = new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL)
			.gridX(0).weightX(1).build();
	private final ResultsTableModel resultsTableModel;

	public OneShiftResultsPanel(final OneShiftModel model) {
		super(new JPanel(new GridBagLayout()), ALPHA);

		JLabel topHeadline = new HtmlLabelBuilder().hx(1, "Asonance pro posun o %d %s", model.getShift(),
				model.getVocalText(model.getShift())).build();

		panel.add(topHeadline, C);

		resultsTableModel = createResultsTableModel(model, model.getGroupsNames());
		JTable resultsTable = createResultsTable(resultsTableModel);

		JXCollapsiblePane resultsPanel = new JXCollapsiblePane();
		resultsPanel.setLayout(new GridBagLayout());
		resultsPanel.setCollapsed(true);

		ToggleHeader resultsPanelHeader = new ToggleHeader(resultsPanel, new HtmlLabelBuilder()
				.hx(2, "Výsledky asonance").build().getText());

		resultsPanel.add(resultsTable.getTableHeader(), C);
		resultsPanel.add(resultsTable, C);

		panel.add(resultsPanelHeader, C);
		panel.add(resultsPanel, C);

		if (model.isTestingPossible()) {
			final BartlettResult bartlettResult = model.getBartlettResult(ALPHA);

			List<JComponent> components;
			JLabel bartlettHeadline = new HtmlLabelBuilder().hx(2, "Bartlettův test").build();

			JLabel bartlettValueB = new HtmlLabelBuilder().text("Veličina B Bartlettova testu: B=%s",
					DECIMAL_FORMAT.format(bartlettResult.getBartlettB())).build();
			JLabel chiSquareCriticalValue = new HtmlLabelBuilder()
					.text("Kritická hodnota χ")
					.sup("2")
					.sub("" + ALPHA)
					.text("(%d - 1) = %s", bartlettResult.getBartlettK(),
							DECIMAL_FORMAT.format(bartlettResult.getCriticalValue())).build();

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
							return model.getGroupsNames()[index - 1];
						}
					};
					components.addAll(doScheffe(scheffeResult, scheffeLabel));
				}

				label = new HtmlLabelBuilder().b(
						"Na hladině významnosti α=" + ALPHA + " zamítáme nulovou hypotézu o rovnosti rozptylů.")
						.build();
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
			JLabel noTesting = new HtmlLabelBuilder()
					.text("Pro testování shody rozptylů je nutné, aby velikost jednotlivých výběrů ")
					.i("n<sub>i</sub>").text(" byla větší než 6.").build();
			panel.add(noTesting, C);
		}

		panel.add(new JLabel(), new GridBagConstraintBuilder().fill(GridBagConstraints.VERTICAL).weightY(1).gridX(0)
				.build());
	}

	private ResultsTableModel createResultsTableModel(OneShiftModel model, String[] groupNames) {
		return new OneShiftResultsTableModel(model, groupNames, DECIMAL_FORMAT);
	}

	private JTable createResultsTable(ResultsTableModel model) {
		JTable t = new NonEditableTable(model);

		JTableHeader header = new JTableHeader(t.getColumnModel()) {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = (int) (d.height * 1.4);
				return d;
			}
		};

		t.setTableHeader(header);
		t.setRowHeight((int) (t.getRowHeight() * 1.4));

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(t.getModel());
		sorter.setComparator(0, TEXT_WITH_NUMBER_COMPARATOR);
		t.setRowSorter(sorter);

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
		CsvData data = new CsvData();
		data.addSection();
		final CsvData.CsvDataSection currentSection = data.getCurrentSection();
		currentSection.addHeader(resultsTableModel.getHeader());
		for (Object[] row : resultsTableModel) {
			currentSection.startNewLine();
			for (Object value : row) {
				currentSection.addData(value);
			}
		}
		return data;
	}
}
