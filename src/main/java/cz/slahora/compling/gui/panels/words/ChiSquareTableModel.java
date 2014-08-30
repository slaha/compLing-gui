package cz.slahora.compling.gui.panels.words;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 *
 * TODO
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 24.8.14 13:07</dd>
 * </dl>
 */
public class ChiSquareTableModel extends AbstractTableModel {

	private final WordLengthFrequenciesChiQTest chiQTest;

	private final int[] lengths;

	public ChiSquareTableModel(WordLengthFrequenciesChiQTest chiQTest) {
		this.chiQTest = chiQTest;
		lengths = chiQTest.getWordLengths();
		Arrays.sort(lengths);

	}

	@Override
	public int getRowCount() {
		return lengths.length;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
			case 1:
				return Integer.class;
			default:
				return Double.class;
		}
	}


	@Override
	public String getColumnName(int column) {
		switch (column) {
			case 0:
				return "Délka slova";
			case 1:
				return "Počet slov dané délky";
			case 2:
				return "Četnost slov dané délky";
			case 3:
				return "Očekávaný počet slov dané délky";
			case 4:
				return "Očekávaná četnost slov dané délky";
		}
		return null;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		int length = lengths[rowIndex];
		switch (columnIndex) {
			case 0:
				return length;
			case 1:
				return chiQTest.getFrequencyFor(length);
			case 2:
				return chiQTest.getRelativeFrequencyFor(length);
			case 3:
				return chiQTest.getProbabilityCount(length);
			case 4:
				return chiQTest.getProbability(length);
		}
		return null;
	}

	static class DecimalFormatRenderer extends DefaultTableCellRenderer {
		private static final DecimalFormat formatter = new DecimalFormat( "0.0000" );

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			value = formatter.format(value);

			// And pass it on to parent class
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );
		}
	}
}
