package cz.slahora.compling.gui.analysis.assonance;

import javax.swing.JTable;

public class NonEditableTable extends JTable {

	public NonEditableTable(Object[][] table, Object[] columnNames) {
		super(table, columnNames);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
