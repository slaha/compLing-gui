package cz.slahora.compling.gui.utils;

import cz.slahora.compling.gui.panels.words.WordLengthFrequenciesChiQTest;

import java.util.Arrays;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 24.8.14 14:03</dd>
 * </dl>
 */
public class StatisticUtils {

	public static double maxLikelihood(int[] i, long[] Xi) {
		double sum = 0;
		for (int x = 0; x < i.length; x++) {
			sum += i[x] * Xi[x];
		}
		double n = getN(Xi);
		return ((1d/n) * sum);
	}

	private static long getN(long[] Xi) {
		long n = 0;
		for (long aXi : Xi) {
			n += aXi;
		}
		return n;
	}

	public static double[] computeExpected(int[] lengths, WordLengthFrequenciesChiQTest chiQTest) {
		double[] expected = new double[lengths.length];
		Arrays.sort(lengths);
		int index = 0;
		for (int length : lengths) {
			expected[index++] = chiQTest.getProbabilityCount(length);
		}
		
		return expected;
	}
}
