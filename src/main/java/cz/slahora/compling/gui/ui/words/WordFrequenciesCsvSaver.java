package cz.slahora.compling.gui.ui.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.compling.model.WordFrequency;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;

import java.util.*;

public class WordFrequenciesCsvSaver extends Csv.CsvSaver<IWordFrequenciesModel> {

	@Override
	public CsvData saveToCsv(IWordFrequenciesModel model, Object... params) {
		CsvData csvData = new CsvData();
		csvData.addSection();
		final Set<Object> allWords = new HashSet<Object>(Arrays.asList(model.getAllDomainElements()));
		final Map<WorkingText, IWordFrequency> allFrequencies = model.getAllFrequencies();
		final Set<WorkingText> allTexts = model.getAllTexts();
		final Map<String, WorkingText> namesToTexts = getNamesOf(allTexts);
		final List<String> namesOfTexts = new ArrayList<String>(namesToTexts.keySet());

		//..add all words to header
		csvData.getCurrentSection().addHeader(namesOfTexts);

		for (Object word : allWords) {
			csvData.getCurrentSection().startNewLine();

			csvData.getCurrentSection().addData(word); //..add word to first column

			for (String textName : namesOfTexts) {
				final WorkingText text = namesToTexts.get(textName); //..get WorkingText by its exported name
				final WordFrequency wordFrequency = allFrequencies.get(text).getWordFrequency(); //..get WordFrequency
				if (word instanceof String) {
					csvData.getCurrentSection().addData(wordFrequency.getFrequencyFor((String)word)); //..add freq of the word
				}
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
