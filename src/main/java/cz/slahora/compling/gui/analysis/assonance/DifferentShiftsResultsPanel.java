package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.model.ScheffeTest;
import cz.slahora.compling.gui.analysis.ToggleHeader;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.AbstractResultsPanel;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.jdesktop.swingx.JXCollapsiblePane;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DifferentShiftsResultsPanel extends AbstractResultsPanel implements ResultsPanel {


	private static final Color LIGHT_GRAY = new Color(232, 232, 232);
	private static final double ALPHA = 0.95;
	private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.####");

	private final DifferentShiftsModel model;

	public DifferentShiftsResultsPanel(DifferentShiftsModel model) {
		super(new JPanel());

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

					AnovaResult anovaResult = model.getAnovaResult(DECIMAL_FORMAT, ALPHA);
					components = createAnovaLayout(anovaResult);
					JLabel label = new HtmlLabelBuilder().b("Nulovou hypotézu o rovnosti rozptylů nezamítáme.").build();
					components.add(0, label);

					if (anovaResult.getTestMethodResult() == TestMethodResult.H0_REJECTED) {

						final int n = (int) anovaResult.getSeDegreesOfFreedom();
						final int k = (int) anovaResult.getSaDegreesOfFreedom();
						final ScheffeResult scheffeResult = model.getScheffeResult(ALPHA, n, k);

						components.addAll(doScheffe(scheffeResult));
					}

					break;
				case H0_REJECTED:

					KruskalWallisResult kwResult = model.getKruskalWallisResult(ALPHA);
					components = createKruskalWallisLayout(kwResult);

					if (kwResult.getTestMethodResult() == TestMethodResult.H0_REJECTED) {

						final int n = (int) kwResult.getN();
						final int k = (int) kwResult.getK();
						final ScheffeResult scheffeResult = model.getScheffeResult(ALPHA, n, k);

						components.addAll(doScheffe(scheffeResult));
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

	private List<JComponent> createKruskalWallisLayout(KruskalWallisResult kwResult) {
		 List<JComponent> components = new ArrayList<JComponent>();

		Object[][] values = kwResult.getRankedTable();

		final int length = values[0].length;
		Object[] headline = new Object[length];
		headline[headline.length - 1] = "<html>t<sub>i</sub></html>";
		headline[headline.length - 2] = "<html>n<sub>i</sub></html>";
		headline[headline.length - 3] = "<html>T<sub>i</sub></html>";
		for (int i = 0; i < headline.length - 3; i++) {
			headline[i] = "";
		}
		final JTable jTable = new JTable(values, headline);

		TableCellRenderer decimalRenderer = new DecimalFormatRenderer();
		for (int i = 1; i < length; i++) {
			jTable.getColumnModel().getColumn(i).setCellRenderer(decimalRenderer);
		}

		jTable.getColumnModel().getColumn(0).setCellRenderer(new FirstColumnRenderer());

		JTableHeader header = new JTableHeader(jTable.getColumnModel()) {
			@Override public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height *= 1.5;
				return d;
			}
		};
		header.setDefaultRenderer(new HeaderCellRenderer());
		jTable.setTableHeader(header);
		jTable.setRowHeight((int) (jTable.getRowHeight() * 1.4));

		JLabel anovaResultsTableHeadline = new HtmlLabelBuilder().hx(2, "Kruskalův-Wallisův test").build();
		anovaResultsTableHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);

		components.add(anovaResultsTableHeadline);
		components.add(jTable.getTableHeader());
		components.add(jTable);

		JLabel lbl;
		switch (kwResult.getTestMethodResult()) {

			case H0_NOT_REJECTED:
				lbl = new HtmlLabelBuilder().text("Na hladině významnosti α=%d%% nezamítáme nulovou hypotézu. Asonance pro různé posuny se významě neliší", (int)(100-100*ALPHA)).build();
				break;
			case H0_REJECTED:
				lbl = new HtmlLabelBuilder().text("Na hladině významnosti α=%d%% zamítáme nulovou hypotézu. Asonance se liší", (int)(100-100*ALPHA)).build();
				break;
			default:
				lbl = null;
				break;
		}

		if (lbl != null) {
			components.add(lbl);
		}

		return components;
	}

	private List<JComponent> createAnovaLayout(AnovaResult anovaResult) {
		 List<JComponent> components = new ArrayList<JComponent>();

		JTable anovaResultsTable = createAnovaResultsTable(anovaResult);
		JLabel anovaResultsTableHeadline = new HtmlLabelBuilder().hx(2, "Anova").build();
		anovaResultsTableHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);

		components.add(anovaResultsTableHeadline);
		components.add(anovaResultsTable.getTableHeader());
		components.add(anovaResultsTable);

		JTable anovaTable = createAnovaTable(anovaResult);
		JLabel anovaTableHeadline = new HtmlLabelBuilder().hx(2, "Anova table").build();
		anovaResultsTableHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);

		components.add(anovaTableHeadline);
		components.add(anovaTable.getTableHeader());
		components.add(anovaTable);

		JLabel fLabel = new HtmlLabelBuilder().text("F").sub("k - 1, n - k").text("(α) = F").sub(anovaResult.getSaDegreesOfFreedom() + ", " + anovaResult.getSeDegreesOfFreedom()).text("(" + ALPHA + ") = " + DECIMAL_FORMAT.format(anovaResult.getCriticalValue())).build();
		components.add(fLabel);

		JLabel lbl;
		switch (anovaResult.getTestMethodResult()) {

			case H0_NOT_REJECTED:
				lbl = new HtmlLabelBuilder().text("Na hladině významnosti α=%d%% nezamítáme nulovou hypotézu. Asonance pro různé posuny se významě neliší", (int)(100-100*ALPHA)).build();
				break;
			case H0_REJECTED:
				lbl = new HtmlLabelBuilder().text("Na hladině významnosti α=%d%% zamítáme nulovou hypotézu. Asonance se liší", (int)(100-100*ALPHA)).build();
				break;
			default:
				lbl = null;
				break;
		}

		if (lbl != null) {
			components.add(lbl);
		}

		return components;
	}

	private List<JComponent> doScheffe(ScheffeResult scheffeResult) {
		List<JComponent> components = new ArrayList<JComponent>();

		final String lbl = "Posun o %d";
		ScheffeResult.ScheffeLabel scheffeLabel = new ScheffeResult.ScheffeLabel() {
			@Override
			public String labelFor(int column) {
				return String.format(lbl, column);
			}
		};
		Object[][] values = scheffeResult.getValues(scheffeLabel);

		final int length = values[0].length;
		Object[] headline = new Object[length];
		headline[0] = "";
		for (int i = 1; i < length; i++) {
			headline[i] = scheffeLabel.labelFor(i + 1);
		}
		final JTable jTable = new JTable(values, headline);

		final List<ScheffeTest.Difference> differences = scheffeResult.getDifferences();
		TableCellRenderer scheffeRenderer = new ScheffeRenderer(differences);
		for (int i = 1; i < length; i++) {
			jTable.getColumnModel().getColumn(i).setCellRenderer(scheffeRenderer);
		}

		jTable.getColumnModel().getColumn(0).setCellRenderer(new FirstColumnRenderer());

		JTableHeader header = new JTableHeader(jTable.getColumnModel()) {
			@Override public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height *= 1.5;
				return d;
			}
		};
		header.setDefaultRenderer(new HeaderCellRenderer());
		jTable.setTableHeader(header);
		jTable.setRowHeight((int) (jTable.getRowHeight() * 1.4));

		JLabel scheffeResultsTableHeadline = new HtmlLabelBuilder().hx(2, "Schéffeho metoda").build();


		components.add(scheffeResultsTableHeadline);
		components.add(jTable.getTableHeader());
		components.add(jTable);

		if (!differences.isEmpty()) {
			JLabel differentPoems = new HtmlLabelBuilder().hx(3, "Lišící se výběry").build();
			components.add(differentPoems);

			for (ScheffeTest.Difference d : differences) {
				String name1 = values[d.getRowIndex()][0].toString();
				String name2 = headline[d.getColumnIndex() + 1].toString();

				JLabel l = new JLabel("    • " + name1 + " a " + name2);

				components.add(l);
			}


		}
		return components;
	}

	private JTable createAnovaTable(AnovaResult anovaResult) {
		Object[][] tableModel = new Object[3][5];
		Object[] columnNames = new Object[5];
		columnNames[0] = "Source";
		columnNames[1] = "SS";
		columnNames[2] = "df";
		columnNames[3] = "MS";
		columnNames[4] = "F";

		tableModel[0][0] = "<html>Rows <i>S<sub>A</sub></i></html>";
		tableModel[1][0] = "<html>Error <i>S<sub>e</sub></i></html>";
		tableModel[2][0] = "<html>Total <i>S<sub>T</sub></i></html>";

		double sa = anovaResult.getSa();
		double se = anovaResult.getSe();

		double saDf = anovaResult.getSaDegreesOfFreedom();
		double seDf = anovaResult.getSeDegreesOfFreedom();

		double saMs = anovaResult.getSaVariance();
		double seMs = anovaResult.getSeVariance();

		double f = anovaResult.getF();

		tableModel[0][1] = sa;
		tableModel[1][1] = se;

		tableModel[0][2] = saDf;
		tableModel[1][2] = seDf;

		tableModel[0][3] = saMs;
		tableModel[1][3] = seMs;

		tableModel[0][4] = f;

		tableModel[2][1] = anovaResult.getTotalS();
		tableModel[2][2] = anovaResult.getTotalDegreesOfFreedom();

		final JTable jTable = new JTable(tableModel, columnNames);
		JTableHeader header = new JTableHeader(jTable.getColumnModel()) {
			@Override public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height *= 1.5;
				return d;
			}
		};
		jTable.getColumnModel().getColumn(0).setCellRenderer(new FirstColumnRenderer());
		jTable.setTableHeader(header);
		jTable.setRowHeight((int) (jTable.getRowHeight() * 1.4));
		header.setDefaultRenderer(new HeaderCellRenderer());

		return jTable;
	}

	private JTable createAnovaResultsTable(AnovaResult anovaResult) {

		Object[] columnNames = new Object[7];
		columnNames[0] = "i";
		columnNames[1] = "<html>n<sub>i</sub></html>";
		columnNames[2] = "<html>X<sub>i∙</sub></html>";
		columnNames[3] = "<html>x\u0304<sub>i∙</sub></html>";
		columnNames[4] = "<html><big>\u03A3</big>X<sup>2</sup><sub>ij</sub></html>";
		columnNames[5] = "<html>s<sup>2</sup><sub>i</sub></html>";
		columnNames[6] = "<html>ln s<sup>2</sup><sub>i</sub></html>";

		final JTable jTable = new JTable(anovaResult.getAnovaSourceValues(), columnNames);
		DefaultTableCellRenderer rightRenderer = new DecimalFormatRenderer();

		for (int i = 1; i < columnNames.length; i++) {
			jTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
		}

		JTableHeader header = new JTableHeader(jTable.getColumnModel()) {
			@Override public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height *= 2;
				return d;
			}
		};

		jTable.getColumnModel().getColumn(0).setCellRenderer(new FirstColumnRenderer());
		jTable.setTableHeader(header);
		jTable.setRowHeight((int) (jTable.getRowHeight() * 1.4));
		header.setDefaultRenderer(new HeaderCellRenderer());

		return jTable;
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
		JTable t = new JTable(table, columnNames);

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

	private class HeaderCellRenderer extends DefaultTableCellHeaderRenderer {
		@Override
		public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			DefaultTableCellHeaderRenderer rendererComponent = (DefaultTableCellHeaderRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			rendererComponent.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
			rendererComponent.setOpaque(true);
			rendererComponent.setBackground(LIGHT_GRAY);
			final Font f = rendererComponent.getFont();
			rendererComponent.setFont(f.deriveFont(f.getStyle() | Font.BOLD));

			return rendererComponent;
		}
	}

	private class FirstColumnRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			DefaultTableCellRenderer rendererComponent = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			rendererComponent.setOpaque(true);
			rendererComponent.setBackground(LIGHT_GRAY);
			final Font f = rendererComponent.getFont();
			rendererComponent.setFont(f.deriveFont(f.getStyle() | Font.BOLD));

			return rendererComponent;
		}
	}

	private class DecimalFormatRenderer extends DefaultTableCellRenderer {

		public DecimalFormatRenderer() {
			setHorizontalAlignment(SwingConstants.RIGHT);
		}

		protected Object formatValue(Object value) {
			String v = DECIMAL_FORMAT.format(value);
			if ("0".equals(v)) {
				return "–";
			}
			return v;
		}

		public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			value = formatValue(value);

			return getSuperTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

		protected Component getSuperTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );
		}
	}

	private class ScheffeRenderer extends DecimalFormatRenderer {

		private final List<ScheffeTest.Difference> differences;

		public ScheffeRenderer(List<ScheffeTest.Difference> differences) {
			super();
			this.differences = differences;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			value = super.formatValue(value);

			for (ScheffeTest.Difference d : differences) {
				if (d.getColumnIndex() == (column - 1) && d.getRowIndex() == row) {
					value = value + " *";
					break;
				}
			}

			return getSuperTableCellRendererComponent(
				table, value, isSelected, hasFocus, row, column );
		}
	}
}
