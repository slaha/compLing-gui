package cz.slahora.compling.gui.analysis.assonance;

import cz.slahora.compling.gui.model.WorkingText;
import org.apache.commons.collections.iterators.ObjectArrayIterator;

import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

class ResultsTableModel extends AbstractTableModel implements Iterable<Object[]> {

	private final Object[][] table;
	private final Object[] columnNames;

	public ResultsTableModel(WorkingText[] poemNames, int maxStep, DifferentShiftsModel model, NumberFormat decimalFormat) {
		table = new Object[maxStep][poemNames.length + 1];

		for (int row = 0; row < table.length; row++) {
			for (int column = 0; column < table[row].length; column++) {

				Object o;
				if (column == 0) {
					o = "Posun o " + (row + 1);

				} else {
					WorkingText uid = poemNames[column - 1];
					final int shift = row + 1;
					double value = model.getAssonanceFor(uid, shift);
					o = decimalFormat.format(value);
				}
				table[row][column] = o;
			}
		}

		columnNames = new Object[table[0].length];
		columnNames[0] = "";
		System.arraycopy(poemNames, 0, columnNames, 1, poemNames.length);

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
