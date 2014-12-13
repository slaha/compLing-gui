package cz.slahora.compling.gui.analysis.assonance;

import org.apache.commons.collections.iterators.ObjectArrayIterator;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

abstract class ResultsTableModel extends AbstractTableModel implements Iterable<Object[]> {

	protected final Object[][] table;
	protected final Object[] columnNames;

	public ResultsTableModel(Object[][] table, Object[] columnNames) {
		this.table = table;
		this.columnNames = columnNames;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column].toString();
	}

	@Override
	public int getRowCount() {
		return table.length;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return table[rowIndex][columnIndex];
	}

	@Override
	public Iterator<Object[]> iterator() {
		return new ObjectArrayIterator(table);
	}

	public Collection<?> getHeader() {
		return Arrays.asList(columnNames);
	}
}
