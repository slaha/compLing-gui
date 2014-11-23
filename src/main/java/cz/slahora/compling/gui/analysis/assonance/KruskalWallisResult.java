package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.model.KruskalWallisTest;

class KruskalWallisResult {

	private final double q;
	private final double k;

	private final double criticalValue;
	private final TestMethodResult testMethodResult;

	private final double[][] rankedValues;

	public KruskalWallisResult(KruskalWallisTest kw, double criticalValue) {

		q = kw.getQ();
		k = kw.getK();
		rankedValues = kw.getRankedValues();

		this.criticalValue = criticalValue;

		this.testMethodResult = q > criticalValue ? TestMethodResult.H0_REJECTED : TestMethodResult.H0_NOT_REJECTED;

	}

	public double getQ() {
		return q;
	}

	public double getK() {
		return k;
	}

	public double getCriticalValue() {
		return criticalValue;
	}

	public TestMethodResult getTestMethodResult() {
		return testMethodResult;
	}

	public Object[][] getRankedTable() {
		Object[][] result = new Object[rankedValues.length][];

		final int rowLength = getRowLength() + 1;

		for (int i = 0; i < rankedValues.length; i++) {
			double[] r = rankedValues[i];
			final Object[] newRow = new Object[rowLength];
			result[i] = newRow;
			newRow[0] = (i + 1) + ". výběr";
			for (int j = 1; j < r.length - 3; j++) {
				newRow[j] = r[j - 1];
			}
			for (int j = 0; j < 3; j++) {
				newRow[newRow.length - j] = r[r.length - j];
			}
		}
		return result;
	}

	private int getRowLength() {
		int max = rankedValues[0].length;

		for (double[] r : rankedValues) {
			max = Math.max(max, r.length);
		}

		return max;
	}
}
