package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.model.ScheffeTest;
import cz.slahora.compling.gui.ui.AbstractResultsPanel;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

abstract class AbsAssonanceResultsPanel extends AbstractResultsPanel {

	private static final Color LIGHT_GRAY = new Color(232, 232, 232);
	protected static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.####");
	protected static final Comparator<String> TEXT_WITH_NUMBER_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {

			int o1NmbrIndex = getFirstNumberIndex(o1);
			int o2NmbrIndex = getFirstNumberIndex(o2);

			final String o1BeforeNmbr = o1.substring(0, o1NmbrIndex);
			final String o2BeforeNmbr = o2.substring(0, o2NmbrIndex);

			final int cmp = o1BeforeNmbr.compareTo(o2BeforeNmbr);
			if (cmp == 0) {
				//..same part before numbers
				int o1Number = getNumberFrom(o1);
				int o2Number = getNumberFrom(o2);
				return o1Number - o2Number;
			}
			return cmp;
		}

		int getFirstNumberIndex(String s) {
			char[] charArray = s.toCharArray();
			for (int i = 0; i < charArray.length; i++) {
				char c = charArray[i];
				if (Character.isDigit(c)) {
					return i;
				}
			}
			return s.length();
		}
		int getNumberFrom(String s) {
			String nmbr = "";
			for (char c : s.toCharArray()) {
				if (Character.isDigit(c)) {
					nmbr += c;
				}
			}
			if (nmbr.length() == 0) {
				return 0;
			}
			return Integer.parseInt(nmbr);
		}
	};

	private double alpha;

	protected AbsAssonanceResultsPanel(JPanel panel, double alpha) {
		super(panel);
		this.alpha = alpha;
	}


	protected List<JComponent> createKruskalWallisLayout(KruskalWallisResult kwResult, TableLabels labels) {
		 List<JComponent> components = new ArrayList<JComponent>();

		Object[][] values = kwResult.getRankedTable(labels);

		final int length = values[0].length;
		Object[] headline = new Object[length];
		headline[headline.length - 1] = "<html>t<sub>i</sub></html>";
		headline[headline.length - 2] = "<html>n<sub>i</sub></html>";
		headline[headline.length - 3] = "<html>T<sub>i</sub></html>";
		for (int i = 0; i < headline.length - 3; i++) {
			headline[i] = "";
		}
		final JTable jTable = new NonEditableTable(values, headline);

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

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jTable.getModel());
		sorter.setComparator(0, TEXT_WITH_NUMBER_COMPARATOR);
		jTable.setRowSorter(sorter);

		header.setDefaultRenderer(new HeaderCellRenderer(jTable));
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
				lbl = new HtmlLabelBuilder().text("Na hladině významnosti α=%d%% nezamítáme nulovou hypotézu. Asonance pro různé posuny se významě neliší", (int)(100-100* alpha)).build();
				break;
			case H0_REJECTED:
				lbl = new HtmlLabelBuilder().text("Na hladině významnosti α=%d%% zamítáme nulovou hypotézu. Asonance se liší", (int)(100-100* alpha)).build();
				break;
			default:
				lbl = null;
				break;
		}

		if (lbl != null) {
			lbl.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
			components.add(lbl);
		}

		return components;
	}

	protected List<JComponent> createAnovaLayout(AnovaResult anovaResult) {
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

		JLabel fLabel = new HtmlLabelBuilder().text("F").sub("k - 1, n - k").text("(α) = F").sub(anovaResult.getSaDegreesOfFreedom() + ", " + anovaResult.getSeDegreesOfFreedom()).text("(" + alpha + ") = " + DECIMAL_FORMAT.format(anovaResult.getCriticalValue())).build();
		fLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
		components.add(fLabel);

		JLabel lbl;
		switch (anovaResult.getTestMethodResult()) {

			case H0_NOT_REJECTED:
				lbl = new HtmlLabelBuilder().text("Na hladině významnosti α=%d%% nezamítáme nulovou hypotézu. Asonance pro různé posuny se významě neliší", (int)(100-100* alpha)).build();
				break;
			case H0_REJECTED:
				lbl = new HtmlLabelBuilder().text("Na hladině významnosti α=%d%% zamítáme nulovou hypotézu. Asonance se liší", (int)(100-100* alpha)).build();
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

	protected List<JComponent> doScheffe(ScheffeResult scheffeResult, TableLabels scheffeLabel) {
		List<JComponent> components = new ArrayList<JComponent>();

		Object[][] values = scheffeResult.getValues(scheffeLabel);

		final int length = values[0].length;
		Object[] headline = new Object[length];
		headline[0] = "";
		for (int i = 1; i < length; i++) {
			headline[i] = scheffeLabel.labelFor(i + 1);
		}
		final JTable jTable = new NonEditableTable(values, headline);

		final List<ScheffeTest.Difference> differences = scheffeResult.getDifferences();
		for (ScheffeTest.Difference d : differences) {
			values[d.getRowIndex()][d.getColumnIndex() + 1] = values[d.getRowIndex()][d.getColumnIndex()+1] + " *";
		}
		TableCellRenderer scheffeRenderer = new ScheffeRenderer();
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

		header.setDefaultRenderer(new HeaderCellRenderer(jTable));
		jTable.setTableHeader(header);
		jTable.setRowHeight((int) (jTable.getRowHeight() * 1.4));

		JLabel scheffeResultsTableHeadline = new HtmlLabelBuilder().hx(2, "Schéffeho metoda").build();

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jTable.getModel());
		sorter.setComparator(0, TEXT_WITH_NUMBER_COMPARATOR);
		for (int i = 1; i < jTable.getColumnCount(); i++) {
			sorter.setSortable(i, false);
		}
		jTable.setRowSorter(sorter);

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

		final JTable jTable = new NonEditableTable(tableModel, columnNames);
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
		header.setDefaultRenderer(new HeaderCellRenderer(jTable));

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

		final JTable jTable = new NonEditableTable(anovaResult.getAnovaSourceValues(), columnNames);
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
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jTable.getModel());
		sorter.setComparator(0, TEXT_WITH_NUMBER_COMPARATOR);
		jTable.setRowSorter(sorter);
		header.setDefaultRenderer(new HeaderCellRenderer(jTable));

		return jTable;
	}

	protected class HeaderCellRenderer implements TableCellRenderer {

		DefaultTableCellRenderer renderer;

		public HeaderCellRenderer(JTable table) {
			renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
			renderer.setHorizontalAlignment(JLabel.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			DefaultTableCellRenderer rendererComponent = (javax.swing.table.DefaultTableCellRenderer) renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			rendererComponent.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, LIGHT_GRAY));
			rendererComponent.setOpaque(true);
			rendererComponent.setBackground(LIGHT_GRAY);
			final Font f = rendererComponent.getFont();
			rendererComponent.setFont(f.deriveFont(f.getStyle() | Font.BOLD));

			return rendererComponent;
		}
	}

	protected class FirstColumnRenderer extends DefaultTableCellRenderer {
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

	protected class DecimalFormatRenderer extends DefaultTableCellRenderer {

		public DecimalFormatRenderer() {
			setHorizontalAlignment(SwingConstants.RIGHT);
		}

		protected Object formatValue(Object value) {
			try {
				String v = DECIMAL_FORMAT.format(value);
				if ("0".equals(v)) {
					return "–";
				}
				return v;
			} catch (IllegalArgumentException e) {
				System.err.println("Not a number " + value);
			}
			return value;
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

	protected class ScheffeRenderer extends DecimalFormatRenderer {


		public ScheffeRenderer() {
			super();
		}

		protected Object formatValue(Object _value) {
			String value = _value.toString();
			boolean withStar = value.contains(" *");
			value = value.replace(" *", "");
			try {
				String v = DECIMAL_FORMAT.format(new BigDecimal(value));
				if ("0".equals(v)) {
					return "–";
				}
				return withStar ? (v + " *") : v;
			} catch (IllegalArgumentException e) {
				System.err.println("Not a number " + value);
			}
			return value;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			value = formatValue(value);

			return getSuperTableCellRendererComponent(
				table, value, isSelected, hasFocus, row, column );
		}
	}
}
