package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.model.AnovaTest;

class AnovaResult {

	private final Object[][] anovaSourceValues;
	private final double sa;
	private final double se;
	private final double saDegreesOfFreedom;
	private final double seDegreesOfFreedom;
	private final double f;
	private final double saVariance;
	private final double seVariance;

	private final double criticalValue;
	private final TestMethodResult testMethodResult;

	public AnovaResult(AnovaTest anova, double criticalValue) {
		this.sa = anova.getSumSquareSa();
		this.se = anova.getSumSquareSe();
		this.saDegreesOfFreedom = anova.getSaDegreesOfFreedom();
		this.seDegreesOfFreedom =  anova.getSeDegreesOfFreedom();
		saVariance = anova.getSaVariance();
		seVariance = anova.getSeVariance();

		this.f = anova.getF();

		double[][] results = anova.getGroupedPartialResults();

		anovaSourceValues = new Object[results.length][];
		for (int rowIndex = 0; rowIndex < results.length; rowIndex++) {
			double[] row = results[rowIndex];
			Object[] newRow = new Object[row.length + 1];
			newRow[0] = "Posun o " + (rowIndex + 1);
			for (int columnIndex = 1; columnIndex < newRow.length; columnIndex++) {
				final double value = row[columnIndex - 1];
				newRow[columnIndex] = value;
			}
			anovaSourceValues[rowIndex] = newRow;
		}

		this.criticalValue = criticalValue;
		testMethodResult = getF() < criticalValue ? TestMethodResult.H0_NOT_REJECTED : TestMethodResult.H0_REJECTED;
	}

	public Object[][] getAnovaSourceValues() {
		return anovaSourceValues;
	}

	public double getSa() {
		return sa;
	}

	public double getSe() {
		return se;
	}

	public double getSaDegreesOfFreedom() {
		return saDegreesOfFreedom;
	}

	public double getSeDegreesOfFreedom() {
		return seDegreesOfFreedom;
	}

	public double getF() {
		return f;
	}

	public double getSaVariance() {
		return saVariance;
	}

	public double getSeVariance() {
		return seVariance;
	}

	public double getTotalDegreesOfFreedom() {
		return getSaDegreesOfFreedom() + getSeDegreesOfFreedom();
	}

	public double getTotalS() {
		return getSa() + getSe();
	}

	public double getCriticalValue() {
		return criticalValue;
	}

	public TestMethodResult getTestMethodResult() {
		return testMethodResult;
	}
}
