package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.model.ScheffeTest;
import cz.slahora.compling.gui.model.WorkingText;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

class ScheffeResult {
	private final double[][] values;
	private final List<ScheffeTest.Difference> differences;

	public ScheffeResult(ScheffeTest sheffe) {
		this.values = sheffe.getValues();
		this.differences = sheffe.getDifferentPairs();
	}

	public Object[][] getValues(WorkingText[] groupsNames) {

		final Object[][] res = new Object[values.length][];

		for (int i = 0; i < values.length; i++) {
			double[] row = values[i];
			Object[] newRow = new Object[row.length + 1];
			newRow[0] = groupsNames[i];
			final Double[] doubles = ArrayUtils.toObject(row);
			System.arraycopy(doubles, 0, newRow, 1, doubles.length);
			res[i] = newRow;
		}

		return res;
	}

	public List<ScheffeTest.Difference> getDifferences() {
		return differences;
	}
}
