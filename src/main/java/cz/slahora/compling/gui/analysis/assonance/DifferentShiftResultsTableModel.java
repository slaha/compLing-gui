package cz.slahora.compling.gui.analysis.assonance;

import cz.slahora.compling.gui.model.WorkingText;

import java.text.NumberFormat;

class DifferentShiftResultsTableModel extends ResultsTableModel {
	public DifferentShiftResultsTableModel(WorkingText[] poemNames, int maxStep, DifferentShiftsModel model, NumberFormat decimalFormat) {
		super(createTable(poemNames,maxStep, model, decimalFormat), createHeaders(poemNames));
	}

	private static Object[] createHeaders(WorkingText[] poemNames) {
		Object[] columnNames = new Object[poemNames.length + 1];
		columnNames[0] = "";
		System.arraycopy(poemNames, 0, columnNames, 1, poemNames.length);
		return columnNames;
	}

	private static Object[][] createTable(WorkingText[] poemNames, int maxStep, DifferentShiftsModel model, NumberFormat decimalFormat) {
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
					o = decimalFormat.format(value);
				}
				table[row][column] = o;
			}
		}
		return table;
	}
}
