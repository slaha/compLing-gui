package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.CompLing;
import cz.slahora.compling.gui.model.WorkingText;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;

public class OneShiftResultsTableModel extends ResultsTableModel {

	public OneShiftResultsTableModel(OneShiftModel model, String[] groupNames, NumberFormat decimalFormat) {
		super(createTable(model, groupNames, decimalFormat), createHeader(model, groupNames));
	}

	private static Object[] createHeader(OneShiftModel model, String[] groupNames) {
		int maxGroupSize = getMaxGroupSize(model, groupNames);
		Object[] columnNames = new Object[maxGroupSize + 1];
		columnNames[0] = "";
		for (int i = 1; i < columnNames.length; i++) {
			columnNames[i] = "Báseň " + i;
		}
		return columnNames;
	}

	private static Object[][] createTable(OneShiftModel model, String[] groupNames, NumberFormat decimalFormat) {
		Object[][] table = new Object[groupNames.length][];

		final int shift = model.getShift();

		for (int row = 0; row < table.length; row++) {
			final String groupName = groupNames[row];
			final Selections.Selection group = model.getGroup(groupName);
			final int groupSize = group.getSize();
			final Object[] rowData = new Object[1 + groupSize];

			rowData[0] = groupName;

			final Iterator<Map.Entry<WorkingText, CompLing>> it = group.iterator();
			int index = 1;
			while (it.hasNext()) {
				Map.Entry<WorkingText, CompLing> entry = it.next();

				WorkingText uid = entry.getKey();
				double value = model.getAssonanceFor(uid, shift);
				Object formattedValue = decimalFormat.format(value);
				rowData[index++] = formattedValue;
			}
			table[row] = rowData;
		}
		return table;
	}

	private static int getMaxGroupSize(OneShiftModel model, String[] groupNames) {
		int maxGroupSize = 0;

		for (final String groupName : groupNames) {
			int groupSize = model.getGroup(groupName).getSize();
			maxGroupSize = Math.max(maxGroupSize, groupSize);
		}
		return maxGroupSize;
	}
}
