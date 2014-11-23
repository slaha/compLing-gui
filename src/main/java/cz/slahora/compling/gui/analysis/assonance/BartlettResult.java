package cz.slahora.compling.gui.analysis.assonance;

class BartlettResult {

	private final double bartlettB;
	private final double criticalValue;
	private final TestMethodResult testMethodResult;
	private final int bartlettK;

	public BartlettResult(double b, double criticalValue, int bartlettK) {
		this.bartlettB = b;
		this.criticalValue = criticalValue;
		this.bartlettK = bartlettK;

		testMethodResult = b < criticalValue ? TestMethodResult.H0_NOT_REJECTED : TestMethodResult.H0_REJECTED;
	}

	public double getBartlettB() {
		return bartlettB;
	}

	public double getCriticalValue() {
		return criticalValue;
	}

	public TestMethodResult getTestMethodResult() {
		return testMethodResult;
	}

	public int getBartlettK() {
		return bartlettK;
	}
}
