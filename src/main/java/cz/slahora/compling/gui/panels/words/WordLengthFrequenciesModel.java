package cz.slahora.compling.gui.panels.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.Selection;
import cz.slahora.compling.gui.utils.MapUtils;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import org.javatuples.Pair;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.table.TableModel;
import java.util.*;

/**
 *
 * TODO
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 10.8.14 9:47</dd>
 * </dl>
 */
public class WordLengthFrequenciesModel implements IWordFrequenciesModel {

	private final Map<WorkingText, IWordFrequency> wordFrequencies;
	private final TIntIntMap lengthsToFrequencies;
	private final Selection<String> selection;

	public WordLengthFrequenciesModel(Map<WorkingText, IWordFrequency> wordFrequencies) {
		this.wordFrequencies = wordFrequencies;
		selection = new Selection<String>();
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
			.append(getUniqueWordsCount())
			.append(" různých slov o ")
			.append(getTotalWordsCount())
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

	private int getUniqueWordsCount() {
		Set<String> words = new HashSet<String>();

		for (IWordFrequency wordFrequency : wordFrequencies.values()) {
			for (Pair<String, Integer> pair : wordFrequency.getWordFrequency().getAllWordsByFrequency(null)) {
				words.add(pair.getValue0());
			}
		}

		return words.size();
	}

	@Override
	public int getTotalWordsCount() {
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
		final int[] lengths = lengthsToFrequencies.keys();
		int max = lengths[0];
		for (int length : lengths) {
			if (max < length) {
				max = length;
			}
		}
		return max;
	}

	@Override
	public Set<String> getAllDomainElements() {
		final TIntSet lengths = lengthsToFrequencies.keySet();
		final  Set<String> all = new HashSet<String>(lengths.size());
		lengths.forEach(new TIntProcedure() {
			@Override
			public boolean execute(int length) {
				all.add(String.valueOf(length));
				return true;
			}
		});
		return all;
	}

	@Override
	public void addCompareChartCategory(String item) {
		selection.add(item);

	}

	@Override
	public void removeComparePlotCategory(String item) {
		selection.remove(item);
	}

	@Override
	public boolean isInCompareChartCategories(String word) {
		return selection.contains(word);
	}

	@Override
	public Set<String> getAllCompareChartCategories() {
		return selection.getAll();
	}

	@Override
	public CategoryDataset getBarDataSetFor(String... words) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		Arrays.sort(words);

		for (String wordString : words) {

			for (Map.Entry<WorkingText, IWordFrequency> entry : wordFrequencies.entrySet()) {
				int word = Integer.parseInt(wordString);
				dataset.setValue(entry.getValue().getWordFrequency().getFrequencyFor(word), wordString, entry.getKey().getName());
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
}
