package cz.slahora.compling.gui.analysis.assonance;

import org.jdesktop.swingx.JXTable;

import javax.swing.table.TableModel;

public class NonEditableTable extends JXTable {

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
