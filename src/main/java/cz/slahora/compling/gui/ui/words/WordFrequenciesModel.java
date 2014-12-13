package cz.slahora.compling.gui.ui.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.ui.Selection;
import cz.slahora.compling.gui.utils.MapUtils;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import org.javatuples.Pair;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.text.Collator;
import java.util.*;

public class WordFrequenciesModel implements IWordFrequenciesModel<String> {
	private final Map<WorkingText, IWordFrequency> wordFrequencies;
	private final TObjectIntMap<String> wordsToFrequencies;
	private final Selection<String> selection;

	public WordFrequenciesModel(Map<WorkingText, IWordFrequency> wordFrequencies) {
		this.wordFrequencies = wordFrequencies;
		this.wordsToFrequencies = mapWordsToFrequencies();
		selection = new Selection<String>();
	}

	@Override
	public String getMainParagraphText() {
		StringBuilder s = new StringBuilder();

		final int analyzedTextsCount = wordFrequencies.size();

		s.append(getBylyForm(analyzedTextsCount)).append(' ').append(analyzedTextsCount).append(' ').append(getTextForm(analyzedTextsCount)).append(":\n");
		appendTextsAsList(s);

		s.append("\nBylo nalezeno celkem ").append(getTotalWordsCount()).append(" různých slov.");

		FrequencyWordPair mostFrequentWord = getMostFrequentWord();
		s.append("\n\n").append("Nejčastěji se vyskytující slovo je slovo '").append(mostFrequentWord.getWords()).append("', které se vyskytlo celkem ")
			.append(mostFrequentWord.getFrequency()).append("×.");

		return s.toString();
	}

	private void appendTextsAsList(StringBuilder s) {

		for (WorkingText text : wordFrequencies.keySet()) {
			s.append("\t\u2022 ").append(text.getName()).append(',').append('\n');
		}
		s.replace(s.lastIndexOf(","), s.lastIndexOf(",") + 1, "."); //..replace last ',' with '.'
	}

	private TObjectIntMap<String> mapWordsToFrequencies() {
		TObjectIntMap<String> wordToFreq = new TObjectIntHashMap<String>();
		//..iterate over all wordFrequencies
		for (IWordFrequency wordFrequency : wordFrequencies.values()) {
			//..get all words. Do not sort
			final List<Pair<String, Integer>> allWordsByFrequency = wordFrequency.getWordFrequency().getAllWordsByFrequency(null);
			for (Pair<String, Integer> pair : allWordsByFrequency) {
				//..iterate over all words in text and put the word to the map
				wordToFreq.adjustOrPutValue(pair.getValue0(), pair.getValue1(), pair.getValue1());
			}
		}
		return wordToFreq;
	}

	@Override
	public FrequencyWordPair getMostFrequentWord() {

		final int max = MapUtils.findMaxValue(wordsToFrequencies);

		final List<String> maxWords = MapUtils.getAllKeysWithValue(wordsToFrequencies, max);

		return new FrequencyWordPair(max, maxWords);
	}

	private String getBylyForm(int size) {
		return size == 1 ? "Byl analyzován" : size > 4 ? "Bylo analyzováno" : "Byly analyzovány";
	}

	private String getTextForm(int size) {
		return size == 1 ? "text" : size > 4 ? "textů" : "texty";
	}

	@Override
	public int getTotalWordsCount() {

		return wordsToFrequencies.keys().length;
	}

	@Override
	public WordFrequencyTableModel getTableModel() {
		return new WordFrequencyTableModel(wordsToFrequencies, wordFrequencies);
	}

	@Override
	public PieDataset getPieDataSet(final int lowerBound) {

		final DefaultPieDataset dataset = new DefaultPieDataset();

		wordsToFrequencies.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String word, int sum) {
				if (sum < lowerBound) {
					return true;
				}
				dataset.setValue(word, sum);
				return true;
			}
		});

		return dataset;
	}

	@Override
	public CategoryDataset getAbsoluteBarDataSet(final int lowerBound) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		wordsToFrequencies.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String word, int sum) {
				if (sum < lowerBound) {
					return true;
				}
				dataset.setValue(sum, "Četnost", word);
				return true;
			}
		});
		return dataset;
	}

	@Override
	public CategoryDataset getRelativeBarDataSet(final int lowerBound) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		final double totalWordsInAllTexts = (double) this.getTotalWordsCount();
		wordsToFrequencies.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String character, int sum) {
				if (sum < lowerBound) {
					return true;
				}
				double perc = (sum / totalWordsInAllTexts) * 100.0d;
				dataset.setValue(perc, "Relativní četnost", character);
				return true;
			}
		});
		return dataset;
	}

	@Override
	public int getFilterMaxValue() {
		return getMostFrequentWord().getFrequency();
	}

	@Override
	public String[] getAllDomainElements() {
		return wordsToFrequencies.keySet().toArray(new String[wordsToFrequencies.size()]);
	}

	@Override
	public Comparator<String> getDomainElementsComparator() {
		Collator coll = Collator.getInstance(Locale.getDefault());
		coll.setStrength(Collator.PRIMARY);
		return (Comparator)coll;
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
	public CategoryDataset getBarDataSetFor(Collection<String> _words) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		List<String> words = new ArrayList<String>(_words);
		Collections.sort(words, getDomainElementsComparator());

		for (String word : words) {

			for (Map.Entry<WorkingText, IWordFrequency> entry : wordFrequencies.entrySet()) {

				dataset.setValue(entry.getValue().getWordFrequency().getFrequencyFor(word), word, entry.getKey().getName());
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
	public WordLengthFrequenciesModel.ChiSquare getChiSquareFor(WorkingText workingText) {
		throw new UnsupportedOperationException(WordFrequenciesModel.class.getSimpleName() + " cannot create chi-square test table model");
	}

	@Override
	public WordLengthFrequenciesModel.ChiSquare getChiSquareFor(WorkingText workingText, WordLengthFrequenciesModel.ChiSquare alpha) {
		throw new UnsupportedOperationException(WordFrequenciesModel.class.getSimpleName() + " cannot create chi-square test table model");
	}
}
