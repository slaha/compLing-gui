package cz.slahora.compling.gui.panels.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.MapUtils;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.javatuples.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordFrequenciesController {
	private final Map<WorkingText, IWordFrequency> wordFrequencies;

	public WordFrequenciesController(Map<WorkingText, IWordFrequency> wordFrequencies) {
		this.wordFrequencies = wordFrequencies;
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

		TObjectIntMap<String> maxFreqWords = mapWordsToFrequencies();
		final int max = MapUtils.findMaxValue(maxFreqWords);

		final List<String> maxWords = MapUtils.getAllKeysWithValue(maxFreqWords, max);

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
		return new WordFrequencyTableModel(mapWordsToFrequencies(), wordFrequencies);
	}
}
