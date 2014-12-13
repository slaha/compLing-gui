package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.CompLing;
import cz.compling.model.*;
import cz.slahora.compling.gui.model.WorkingText;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.FDistribution;

import java.util.LinkedHashMap;
import java.util.Map;

class OneShiftModel {
	private final int shift;
	private final Selections selections;
	private final String[] vocals;
	private final Map<WorkingText, Assonance> assonances;

	private TestData testData;
	private BartlettTest bartlettTest;
	private AnovaTest anova;
	private ScheffeTest scheffe;
	private KruskalWallisTest kruskalWallis;

	public OneShiftModel(int shift, Selections selections, Map<WorkingText, CompLing> texts, String[] vocals) {
		this.shift = shift;
		this.selections = selections;
		this.vocals = vocals;

		assonances = new LinkedHashMap<WorkingText, Assonance>(texts.size());

		for (Map.Entry<WorkingText, CompLing> e : texts.entrySet()) {
			final CompLing.PoemAnalysis poemAnalysis = e.getValue().poemAnalysis();
			final Assonance assonance = poemAnalysis.assonance(vocals).getAssonance();
			assonances.put(e.getKey(), assonance);
		}
	}

	public int getShift() {
		return shift;
	}

	public boolean isTestingPossible() {

		final String[] allNames = selections.getAllNames();
		if (allNames.length < 2) {
			return false;
		}
		for (String name : allNames) {
			if (selections.getGroup(name).getSize() <= 6) {
				return false;
			}
		}
		return true;
	}

	public String getVocalText(int shift) {
		return (shift == 1 ? "vokál" : shift >= 5 ? "vokálů" : "vokály");
	}

	private TestData getTestData() {
		if (this.testData == null) {
			final String[] groupsNames = selections.getAllNames();
			int groupsCount = groupsNames.length;
			double[][] values = new double[groupsCount][];

			for (int group = 0; group < groupsCount; group++) {
				final Selections.Selection selection = selections.getGroup(groupsNames[group]);
				int size = selection.getSize();
				double v[] = new double[size];
				int index = 0;
				for (Map.Entry<WorkingText, CompLing> entry : selection) {
					v[index++] = assonances.get(entry.getKey()).getAssonanceFor(shift);
				}
				values[group] = v;
			}
			this.testData = new TestData(values);
		}
		return testData;
	}

	private BartlettTest getBartlett() {
		if (this.bartlettTest == null) {
			this.bartlettTest = new BartlettTest(getTestData());
		}
		return bartlettTest;
	}

	public BartlettResult getBartlettResult(double alpha) {
		final int degreesOfFreedom = getTestData().getGroupsCount() - 1;
		final ChiSquaredDistribution chi = new ChiSquaredDistribution(degreesOfFreedom);

		final double b = getBartlett().getB();
		final double criticalValue = chi.inverseCumulativeProbability(alpha);

		return new BartlettResult(b, criticalValue, getTestData().getGroupsCount());
	}

	private AnovaTest getAnova() {
		if (this.anova == null) {
			this.anova = new AnovaTest(getTestData());
		}
		return anova;
	}

	public AnovaResult getAnovaResult(double alpha) {
		final AnovaTest anova = getAnova();

		double k = anova.getSaDegreesOfFreedom();
		double n = anova.getSeDegreesOfFreedom();
		final FDistribution f = new FDistribution(n, k);
		double criticalValue = f.inverseCumulativeProbability(alpha);

		return new AnovaResult(anova, criticalValue);
	}

	private ScheffeTest getSheffe(double alpha, int k, int n) {
		if (this.scheffe == null) {
			final AnovaTest a = getAnova();
			this.scheffe = new ScheffeTest(getTestData(), k, n, alpha);
		}
		return scheffe;
	}

	public ScheffeResult getScheffeResult(double alpha, int n, int k) {
		final ScheffeTest sheffe = getSheffe(alpha, k, n);

		return new ScheffeResult(sheffe);
	}

	private KruskalWallisTest getKruskalWallisTest() {
		if (this.kruskalWallis == null) {
			kruskalWallis = new KruskalWallisTest(getTestData());
		}
		return kruskalWallis;
	}

	public KruskalWallisResult getKruskalWallisResult(double alpha) {
		final KruskalWallisTest kw = getKruskalWallisTest();

		final int degreesOfFreedom = (kw.getK() - 1);
		final ChiSquaredDistribution chi = new ChiSquaredDistribution(degreesOfFreedom);

		final double criticalValue = chi.inverseCumulativeProbability(alpha);

		return new KruskalWallisResult(kw, criticalValue);
	}

	public String[] getGroupsNames() {
		return selections.getAllNames();
	}

	public Selections.Selection getGroup(String groupName) {
		return selections.getGroup(groupName);
	}

	public double getAssonanceFor(WorkingText text, int shift) {
		final Assonance assonance = assonances.get(text);
		if (assonance == null) {
			throw new IllegalArgumentException(text + " not found in assonances table");
		}

		if (shift > assonance.getMaxStep()) {
			throw new IllegalArgumentException("shift " + shift + " too big. Max shift for text " + text + " is " + assonance.getMaxStep());
		}
		return assonance.getAssonanceFor(shift);
	}
}
