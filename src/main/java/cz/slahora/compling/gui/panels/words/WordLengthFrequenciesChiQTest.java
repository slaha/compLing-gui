package cz.slahora.compling.gui.panels.words;

import cz.compling.model.WordFrequency;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.Arrays;

public class WordLengthFrequenciesChiQTest {

	private final PoissonDistribution poissonDistribution;
	private final int totalCount;
	private final WordFrequency wordFrequency;
	private final int[] wordLengths;

	public WordLengthFrequenciesChiQTest(PoissonDistribution poissonDistribution, WordFrequency wordFrequency) {
		this.poissonDistribution = poissonDistribution;
		this.wordFrequency = wordFrequency;
		this.totalCount = wordFrequency.getCountOfWords();
		this.wordLengths = wordFrequency.getWordLengths();
		Arrays.sort(wordLengths);
	}

	public int[] getWordLengths() {
		return wordLengths;
	}

	public int getFrequencyFor(int length) {
		return wordFrequency.getFrequencyFor(length);
	}

	public double getRelativeFrequencyFor(int length) {
		return wordFrequency.getRelativeFrequencyFor(length);
	}

	public double getProbability(int length) {
		return poissonDistribution.probability(length);
	}

	public double getProbabilityCount(int length) {
		return poissonDistribution.probability(length) * totalCount;
	}
}
