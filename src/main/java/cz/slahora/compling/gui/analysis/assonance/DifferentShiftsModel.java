package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.CompLing;
import cz.compling.model.*;
import cz.slahora.compling.gui.model.WorkingText;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.FDistribution;

import java.text.NumberFormat;
import java.util.*;

class DifferentShiftsModel {

	private final Map<WorkingText, Assonance> assonances;
	private final int maxSteps;

	private TestData testData;
	private BartlettTest bartlettTest;
	private AnovaTest anova;
	private ScheffeTest scheffe;
	private KruskalWallisTest kruskalWallis;

	public DifferentShiftsModel(Map<WorkingText, CompLing> texts, String[] vocals, int maxSteps) {
		this.maxSteps = maxSteps;
		assonances = new LinkedHashMap<WorkingText, Assonance>(texts.size());

		for (Map.Entry<WorkingText, CompLing> e : texts.entrySet()) {
			final CompLing.PoemAnalysis poemAnalysis = e.getValue().poemAnalysis();
			final Assonance assonance = poemAnalysis.assonance(vocals).getAssonance();
			assonances.put(e.getKey(), assonance);
		}
	}

	public WorkingText[] getPoemNames() {
		Set<WorkingText> workingTexts = assonances.keySet();
		return workingTexts.toArray(new WorkingText[workingTexts.size()]);
	}

	public int getAssonanceFor(WorkingText text, int shift) {
		final Assonance assonance = assonances.get(text);
		if (assonance == null) {
			throw new IllegalArgumentException(text + " not found in assonances table");
		}

		if (shift > assonance.getMaxStep()) {
			throw new IllegalArgumentException("shift " + shift + " too big. Max shift for text " + text + " is " + assonance.getMaxStep());
		}
		return assonance.getAssonanceFor(shift);
	}

	public int getLowestMaxStep() {
		int lowestMaxStep = Integer.MAX_VALUE;
		for (Assonance a : assonances.values()) {
			int max = a.getMaxStep();
			if (max < lowestMaxStep) {
				lowestMaxStep = max;
			}
		}
		return Math.min(maxSteps, lowestMaxStep);
	}

	public boolean isTestingPossible() {
		for (Assonance a : assonances.values()) {
			if (a.getMaxStep() <= 6) {
				return false;
			}
		}
		return true;
	}

	public BartlettResult getBartlettResult(double alpha) {
		final int degreesOfFreedom = getTestData().getGroupsCount() - 1;
		final ChiSquaredDistribution chi = new ChiSquaredDistribution(degreesOfFreedom);

		final double b = getBartlett().getB();
		final double criticalValue = chi.inverseCumulativeProbability(alpha);

		return new BartlettResult(b, criticalValue, getTestData().getGroupsCount());
	}

	private TestData getTestData() {
		if (this.testData == null) {
			double[][] values = new double[maxSteps][assonances.size()];

			final Collection<Assonance> entries = assonances.values();
			List<Assonance> aList = new ArrayList<Assonance>(entries);

			for (int poem = 0; poem < aList.size(); poem++) {
				Assonance assonance = aList.get(poem);
				for (int shift = 0; shift < maxSteps; shift++) {
					values[shift][poem] = assonance.getAssonanceFor(shift + 1);
				}
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

	private AnovaTest getAnova() {
		if (this.anova == null) {
			this.anova = new AnovaTest(getTestData());
		}
		return anova;
	}

	public AnovaResult getAnovaResult(NumberFormat numberFormat, double alpha) {
		final AnovaTest anova = getAnova();

		double k = anova.getSaDegreesOfFreedom();
		double n = anova.getSeDegreesOfFreedom();
		final FDistribution f = new FDistribution(n, k);
		double criticalValue = f.inverseCumulativeProbability(alpha);

		return new AnovaResult(anova, criticalValue);
	}

	private ScheffeTest getSheffe(double alpha) {
		if (this.scheffe == null) {
			final AnovaTest a = getAnova();
			final int k = (int) a.getSaDegreesOfFreedom();
			final int n = (int) a.getSeDegreesOfFreedom();
			this.scheffe = new ScheffeTest(getTestData(), k, n, alpha);
		}
		return scheffe;
	}

	public ScheffeResult getScheffeResult(double alpha) {
		final ScheffeTest sheffe = getSheffe(alpha);

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
}
