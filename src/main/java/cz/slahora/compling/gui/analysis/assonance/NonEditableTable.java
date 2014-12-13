package cz.slahora.compling.gui.analysis.assonance;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class NonEditableTable extends JTable {

	public NonEditableTable(Object[][] table, Object[] columnNames) {
		super(table, columnNames);
	}

	public NonEditableTable(TableModel dm) {
		super(dm);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
