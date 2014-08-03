package cz.slahora.compling.gui.panels.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.compling.model.WordFrequency;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;

import java.util.*;

public class WordFrequenciesCsvSaver extends Csv.CsvSaver<WordFrequenciesModel> {

	@Override
	public CsvData saveToCsv(WordFrequenciesModel model, Object... params) {
		CsvData csvData = new CsvData();
		csvData.addSection();
		final Set<String> allWords = model.getAllWords();
		final Map<WorkingText, IWordFrequency> allFrequencies = model.getAllFrequencies();
		final Set<WorkingText> allTexts = model.getAllTexts();
		final Map<String, WorkingText> namesToTexts = getNamesOf(allTexts);
		final List<String> namesOfTexts = new ArrayList<String>(namesToTexts.keySet());

		//..add all words to header
		csvData.getCurrentSection().addHeader(namesOfTexts);

		for (String word : allWords) {
			csvData.getCurrentSection().startNewLine();

			csvData.getCurrentSection().addData(word);

			for (String textName : namesOfTexts) {
				final WorkingText text = namesToTexts.get(textName);
				final WordFrequency wordFrequency = allFrequencies.get(text).getWordFrequency();
				csvData.getCurrentSection().addData(wordFrequency.getFrequencyFor(word));
			}
		}
		csvData.getCurrentSection().addHeader(0, "Slovo");
		return csvData;
	}

	private Map<String, WorkingText> getNamesOf(Set<WorkingText> allTexts) {
		Map<String, WorkingText> names = new HashMap<String, WorkingText>(allTexts.size());
		for (WorkingText text : allTexts) {
			final String name = text.getName() + "%s";
			String nameToPut = String.format(name, "");
			int i = 1;
			while (names.containsKey(nameToPut)) {
				nameToPut = String.format(name, "_" + i++);
			}
			names.put(nameToPut, text);
		}
		return names;
	}
}
