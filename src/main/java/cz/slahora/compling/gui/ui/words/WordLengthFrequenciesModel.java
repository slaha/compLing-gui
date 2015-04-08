package cz.slahora.compling.gui.ui.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.compling.model.WordFrequency;
import cz.compling.utils.Reference;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.ui.Selection;
import cz.slahora.compling.gui.utils.MapUtils;
import cz.slahora.compling.gui.utils.StatisticUtils;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.javatuples.Pair;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.table.TableModel;
import java.util.*;

/**
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 10.8.14 9:47</dd>
 * </dl>
 */
public class WordLengthFrequenciesModel implements IWordFrequenciesModel<Integer> {

	private final Map<WorkingText, IWordFrequency> wordFrequencies;

	/** key: length of the word; value: count of words with the length */
	private final TIntIntMap lengthsToFrequencies;
	private final Selection<Integer> selection;

	public WordLengthFrequenciesModel(Map<WorkingText, IWordFrequency> wordFrequencies) {
		this.wordFrequencies = wordFrequencies;
		selection = new Selection<Integer>();
		lengthsToFrequencies = mapLengthsToFrequencies();
	}

	private TIntIntMap mapLengthsToFrequencies() {
		TIntIntMap lengthToFreq = new TIntIntHashMap();
		//..iterate over all wordFrequencies
		for (IWordFrequency wordFrequency : wordFrequencies.values()) {
			//..get all words. Do not sort
			final List<Pair<Integer, Integer>> allWordsByLength = wordFrequency.getWordFrequency().getAllWordsLengthsByFrequency(null);
			for (Pair<Integer, Integer> pair : allWordsByLength) {
				//..iterate over all words in text and put the word to the map
				lengthToFreq.adjustOrPutValue(pair.getValue0(), pair.getValue1(), pair.getValue1());
			}
		}
		return lengthToFreq;

	}

	@Override
	public String getMainParagraphText() {
		StringBuilder s = new StringBuilder();

		final int analyzedTextsCount = wordFrequencies.size();

		s
			.append(getBylyForm(analyzedTextsCount))
			.append(' ')
			.append(analyzedTextsCount)
			.append(' ')
			.append(getTextForm(analyzedTextsCount))
			.append(":\n");

		appendTextsAsList(s);

		s
			.append("\nBylo nalezeno celkem ")
			.append(getTotalWordsCount())
			.append(" různých slov o ")
			.append(getDomainSize())
			.append(" délkách.");

		LengthFrequencyWordPair mostFrequentWord = getMostFrequentWord();
		s
			.append("\n\n")
			.append("Nejčastěji se vyskytující délka slova je '")
			.append(mostFrequentWord.getLengths())
			.append("'. Bylo nalezeno celkem ")
			.append(mostFrequentWord.getFrequency())
			.append(" slov s touto délkou. Tato slova jsou: ")
			.append(mostFrequentWord.getWords())
			.append(".");

		return s.toString();
	}

	private void appendTextsAsList(StringBuilder s) {

		for (WorkingText text : wordFrequencies.keySet()) {
			s.append("\t\u2022 ").append(text.getName()).append(',').append('\n');
		}
		s.replace(s.lastIndexOf(","), s.lastIndexOf(",") + 1, "."); //..replace last ',' with '.'
	}

	private String getBylyForm(int size) {
		return size == 1 ? "Byl analyzován" : size > 4 ? "Bylo analyzováno" : "Byly analyzovány";
	}

	private String getTextForm(int size) {
		return size == 1 ? "text" : size > 4 ? "textů" : "texty";
	}

	@Override
	public LengthFrequencyWordPair getMostFrequentWord() {
		final int maxFreq = MapUtils.findMaxValue(lengthsToFrequencies);

		final List<Integer> maxLengths = MapUtils.getAllKeysWithValue(lengthsToFrequencies, maxFreq);

		final List<String> wordsWithMaxLength = new ArrayList<String>();
		for (IWordFrequency wordFrequency : wordFrequencies.values()) {
			for (Pair<String, Integer> pair : wordFrequency.getWordFrequency().getAllWordsByFrequency(null)) {
				int wordLength = pair.getValue0().length();
				if (maxLengths.contains(wordLength)) {
					wordsWithMaxLength.add(pair.getValue0());
				}
			}

		}

		return new LengthFrequencyWordPair(maxLengths, maxFreq, wordsWithMaxLength);
	}

	@Override
	public int getTotalWordsCount() {
		Set<String> words = new HashSet<String>();

		for (IWordFrequency wordFrequency : wordFrequencies.values()) {
			for (Pair<String, Integer> pair : wordFrequency.getWordFrequency().getAllWordsByFrequency(null)) {
				words.add(pair.getValue0());
			}
		}

		return words.size();
	}

	/**
	 * Returns count of different lengths
	 */
	@Override
	public int getDomainSize() {
		return lengthsToFrequencies.keys().length;
	}

	@Override
	public TableModel getTableModel() {
		return new WordLengthFrequencyTableModel(lengthsToFrequencies, wordFrequencies);
	}

	@Override
	public PieDataset getPieDataSet(final int lowerBound) {

		final DefaultPieDataset dataset = new DefaultPieDataset();

		lengthsToFrequencies.forEachEntry(new TIntIntProcedure() {
			@Override
			public boolean execute(int length, int sum) {
				if (sum < lowerBound) {
					return true;
				}
				dataset.setValue(length, (Number)sum);
				return true;
			}
		});

		return dataset;
	}

	@Override
	public CategoryDataset getAbsoluteBarDataSet(int lowerBound) {
		final DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
		lengthsToFrequencies.forEachEntry(new TIntIntProcedure() {
			@Override
			public boolean execute(int length, int freq) {
				categoryDataset.setValue((Number)freq, "Četnost", length);
				return true;
			}
		});

		return categoryDataset;
	}

	private int getWordsCount() {
		int s = 0;
		for (int length : lengthsToFrequencies.keys()) {
			s += lengthsToFrequencies.get(length);
		}
		return s;
	}

	@Override
	public CategoryDataset getRelativeBarDataSet(int lowerBound) {
		final DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();

		final double allCharactersInAllTexts = getWordsCount();
		lengthsToFrequencies.forEachEntry(new TIntIntProcedure() {
			@Override
			public boolean execute(int length, int freq) {


				double perc = ((double)freq / allCharactersInAllTexts) * 100.0d;
				categoryDataset.setValue((Number)perc, "Relativní četnost", length);
				return true;
			}
		});

		return categoryDataset;
	}

	@Override
	public int getFilterMaxValue() {
		final Reference<Integer> max = new Reference<Integer>(-1);
		lengthsToFrequencies.forEachEntry(new TIntIntProcedure() {
			@Override
			public boolean execute(int length, int countOfOccurrences) {
				if (countOfOccurrences > max.value) {
					max.value = countOfOccurrences;
				}
				return true;
			}
		});
		return max.value;
	}

	@Override
	public Integer[] getAllDomainElements() {
		final TIntSet lengths = lengthsToFrequencies.keySet();
		final Integer[] all = new Integer[lengths.size()];
		final Reference<Integer> index = new Reference<Integer>(0);
		lengths.forEach(new TIntProcedure() {
			@Override
			public boolean execute(int length) {
				all[index.value] = length;
				index.value++;
				return true;
			}
		});
		return all;
	}

	@Override
	public Comparator<Integer> getDomainElementsComparator() {
		return new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		};
	}

	@Override
	public void addCompareChartCategory(Integer item) {
		selection.add(item);

	}

	@Override
	public void removeComparePlotCategory(Integer item) {
		selection.remove(item);
	}

	@Override
	public boolean isInCompareChartCategories(Integer word) {
		return selection.contains(word);
	}

	@Override
	public Set<Integer> getAllCompareChartCategories() {
		return selection.getAll();
	}

	@Override
	public CategoryDataset getBarDataSetFor(Collection<Integer> _words) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();


		Integer[] words = _words.toArray(new Integer[_words.size()]);
		Arrays.sort(words, getDomainElementsComparator());

		for (Integer word : words) {

			for (Map.Entry<WorkingText, IWordFrequency> entry : wordFrequencies.entrySet()) {
				dataset.setValue(entry.getValue().getWordFrequency().getFrequencyFor(word), String.valueOf(word), entry.getKey().getName());
			}
		}
		return dataset;
	}

	@Override
	public Map<WorkingText, IWordFrequency> getAllFrequencies() {
		return wordFrequencies;
	}

	@Override
	public Set<WorkingText> getAllTexts() {
		return wordFrequencies.keySet();
	}

	@Override
	public ChiSquare getChiSquareFor(WorkingText workingText) {
		ChiSquare chiSquare = new ChiSquare();
		final WordFrequency wordFrequency = wordFrequencies.get(workingText).getWordFrequency();
		computePoissonLambda(wordFrequency, chiSquare);

		PoissonDistribution poissonDistribution = new PoissonDistribution(chiSquare.lambda);
		WordLengthFrequenciesChiQTest chiQTest = new WordLengthFrequenciesChiQTest(poissonDistribution, wordFrequency);
		chiSquare.tableModel = new ChiSquareTableModel(chiQTest);
		chiSquare.chiQTest = chiQTest;

		return getChiSquareFor(workingText, chiSquare);
	}

	@Override
	public ChiSquare getChiSquareFor(WorkingText workingText, ChiSquare chiSquare) {
		final ChiSquareTest test = new ChiSquareTest();
		double[] expected = StatisticUtils.computeExpected(chiSquare.lengths, chiSquare.chiQTest);
		test.chiSquare(expected, chiSquare.observed);
		test.chiSquareTest(expected, chiSquare.observed);
		chiSquare.chiTest = test.chiSquareTest(expected, chiSquare.observed, chiSquare.alpha);
		chiSquare.chiSquareValue = test.chiSquare(expected, chiSquare.observed);
		chiSquare.chiSquareCriticalValue = new ChiSquaredDistribution(chiSquare.degreesOfFreedom).inverseCumulativeProbability(1d - chiSquare.alpha);
		return chiSquare;
	}

	private void computePoissonLambda(WordFrequency wordFreq, ChiSquare chiSquare) {
		final int[] wordLengths = wordFreq.getWordLengths();
		Arrays.sort(wordLengths);

		long[] wordLengthsObserved = new long[wordLengths.length];
		for (int i = 0; i < wordLengths.length; i++) {
			wordLengthsObserved[i] = wordFreq.getFrequencyFor(wordLengths[i]);
		}
		chiSquare.lengths = wordLengths;
		chiSquare.observed = wordLengthsObserved;
		chiSquare.degreesOfFreedom = wordLengths.length - 1;
		chiSquare.lambda = StatisticUtils.maxLikelihood(wordLengths, wordLengthsObserved);
	}

	public class ChiSquare {



		private double lambda;
		private int[] lengths;
		private long[] observed;
		private WordLengthFrequenciesChiQTest chiQTest;
		public ChiSquareTableModel tableModel;

		public double alpha = 0.05;
		public boolean chiTest;
		public Object distribution = "Poissonovu";
		public double chiSquareValue;
		public double chiSquareCriticalValue;
		public int degreesOfFreedom;
	}
}
