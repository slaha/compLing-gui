package cz.slahora.compling.gui.panels.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.MapUtils;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import org.javatuples.Pair;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordFrequenciesController {
	private final Map<WorkingText, IWordFrequency> wordFrequencies;
	private final TObjectIntMap<String> wordsToFrequencies;

	public WordFrequenciesController(Map<WorkingText, IWordFrequency> wordFrequencies) {
		this.wordFrequencies = wordFrequencies;
		this.wordsToFrequencies = mapWordsToFrequencies();
	}

	public String getMainParagraphText() {
		StringBuilder s = new StringBuilder();

		final int analyzedTextsCount = wordFrequencies.size();

		s.append(getBylyForm(analyzedTextsCount)).append(' ').append(analyzedTextsCount).append(' ').append(getTextForm(analyzedTextsCount)).append(":\n");
		appendTextsAsList(s);

		s.append("Bylo nalezeno celkem ").append(getTotalWordsCount()).append(" různých slov.</p>");

		FrequencyWordPair mostFrequentWord = getMostFrequentWord();
		s.append("<p>").append("Nejčastěji se vyskytující slovo je slovo '").append(mostFrequentWord.getWords()).append("', které se vyskytlo celkem ")
			.append(mostFrequentWord.getFrequency()).append("×.");

		return s.toString();
	}

	private void appendTextsAsList(StringBuilder s) {
		s.append("<ul>");

		for (WorkingText text : wordFrequencies.keySet()) {
			s.append("<li>").append(text.getName()).append(',').append("</li>");
		}
		s.replace(s.lastIndexOf(","), s.lastIndexOf(",") + 1, "."); //..replace last ',' with '.'

		s.append("</ul>");
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

	public int getTotalWordsCount() {
		Set<String> words = new HashSet<String>();

		for (IWordFrequency wordFrequency : wordFrequencies.values()) {
			for (Pair<String, Integer> pair : wordFrequency.getWordFrequency().getAllWordsByFrequency(null)) {
				words.add(pair.getValue0());
			}
		}

		return words.size();
	}

	public WordFrequencyTableModel getTableModel() {
		return new WordFrequencyTableModel(wordsToFrequencies, wordFrequencies);
	}

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

	public CategoryDataset getRelativeBarDataSet(final int lowerBound) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		final double allCharactersInAllTexts = (double) this.getTotalWordsCount();
		wordsToFrequencies.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String character, int sum) {
				if (sum < lowerBound) {
					return true;
				}
				double perc = (sum / allCharactersInAllTexts) * 100.0d;
				dataset.setValue(perc, "Relativní četnost", character);
				return true;
			}
		});
		return dataset;
	}

	public int getMaxOccurence() {
		return getMostFrequentWord().getFrequency();
	}
}
