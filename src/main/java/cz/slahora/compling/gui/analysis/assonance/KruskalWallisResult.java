package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.model.KruskalWallisTest;
import cz.compling.model.TestData;

import java.util.Arrays;

class KruskalWallisResult {

	private final double q;
	private final double k;
	private final double n;

	private final double criticalValue;
	private final TestMethodResult testMethodResult;

	private final double[][] rankedValues;

	public KruskalWallisResult(KruskalWallisTest kw, double criticalValue) {

		q = kw.getQ();
		k = kw.getK();
		n = kw.getN();
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

	public double getN() {
		return n;
	}

	public double getCriticalValue() {
		return criticalValue;
	}

	public TestMethodResult getTestMethodResult() {
		return testMethodResult;
	}

	public Object[][] getRankedTable(TableLabels labels) {
		Object[][] result = new Object[rankedValues.length][];

		for (int i = 0; i < rankedValues.length; i++) {
			double[] r = rankedValues[i];
			final int rowLength = r.length + 1;
			final Object[] newRow = new Object[rowLength];
			result[i] = newRow;
			newRow[0] = labels.labelFor(i+1);
			for (int j = 1; j <= r.length - 3; j++) {
				newRow[j] = r[j - 1];
			}
			for (int j = 1; j <= 3; j++) {
				newRow[newRow.length - j] = r[r.length - j];
			}
		}
		return result;
	}

	public static void main(String[] args) {
		double[][] values = new double[][] {
			{ 55, 54, 58, 61, 52, 60, 53, 65 },
			{ 52, 50, 51, 51, 49 },
			{ 47, 53, 49, 50, 46, 48, 50 } };
		TestData td = new TestData(values);
		KruskalWallisTest kw = new KruskalWallisTest(td);

		KruskalWallisResult result = new KruskalWallisResult(kw, 1);
		TableLabels l = new TableLabels() {
			@Override
			public String labelFor(int index) {
				return index + ". výběr";
			}
		};
		for (Object[] objects : result.getRankedTable(l)) {
			System.out.println(Arrays.toString(objects));
		}
	}
}
