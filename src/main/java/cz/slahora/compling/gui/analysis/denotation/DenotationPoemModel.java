package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.text.poem.Poem;
import cz.compling.text.poem.Verse;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.PipeArrayList;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.CsvParserUtils;

import java.util.Collection;
import java.util.List;

public class DenotationPoemModel implements Csv<DenotationPoemModel> {

	private final Poem poem;
	private final IDenotation denotation;

	public DenotationPoemModel(WorkingText text) {
		this(text, true);
	}

	private DenotationPoemModel(WorkingText text, boolean compute) {
		this.poem = text.getCompLing().poemAnalysis().poem;
		this.denotation = text.getCompLing().poemAnalysis().denotationAnalysis();
	}

	public int getCountOfWords() {
		return denotation.getCountOfWords();
	}

	public cz.compling.model.denotation.DenotationWord getWord(int wordNumber) {
		return denotation.getWord(wordNumber);
	}

	/*********************************/
	/*
	/* CSV
	/*
	/*********************************/
	@Override
	public CsvSaver<DenotationPoemModel> getCsvSaver() {
		return new DenotationPoemModelSaver();
	}

	@Override
	public boolean supportsCsvImport() {
		return true;
	}

	@Override
	public CsvLoader<DenotationPoemModel> getCsvLoader() {
		return new DenotationPoemModelLoader();
	}


	public IDenotation getDenotation() {
		return denotation;
	}

	public static class DenotationSpikeNumber {
		/** String value of the word. Can be different than in poem for example when the word has more denotation elements */
		private String word;

		/** Number of the spike number */
		private int number;

//		private GuiDenotationSpikesModel.Spike spike;

		public DenotationSpikeNumber(int number, String word) {
			this.number = number;
			this.word = word;
		}
	}



	private static class DenotationPoemModelSaver extends CsvSaver<DenotationPoemModel> {
		@Override
		public CsvData saveToCsv(DenotationPoemModel object, Object... params) {
			CsvData csvData = new CsvData();
			csvData.addSection();
			csvData.getCurrentSection().addHeader("Number of strophe");
			csvData.getCurrentSection().addHeader("Number of verse");
			csvData.getCurrentSection().addHeader("Word");
			csvData.getCurrentSection().addHeader("Number of word");
			csvData.getCurrentSection().addHeader("Word(s)");
			csvData.getCurrentSection().addHeader("Denotation element(s)");
			csvData.getCurrentSection().addHeader("Joined");
			csvData.getCurrentSection().addHeader("Ignored");
			//TODO
			/*
			for (int stropheNmbr = 1; stropheNmbr <= object.getCountOfWords(); stropheNmbr++) {

				DenotationStrophe strophe = object.getStrophe(stropheNmbr);
				for (DenotationVerse verse : strophe.verses) {
					for (DenotationWord word : verse.words) {
						csvData.getCurrentSection().startNewLine();
						strophe.toCsv(csvData);
						verse.toCsv(csvData);
						word.toCsv(csvData);
					}
				}
			}
			 */
			return csvData;
		}
	}

	private static class DenotationPoemModelLoader extends CsvLoader<DenotationPoemModel> {

		/** splitter for splitting lists */
		private static final CsvParserUtils.CollectionSplitter SPLITTER = new CsvParserUtils.CollectionSplitter() {
			@Override
			public String getSplitter() {
				return PipeArrayList.SPLITTER;
			}
		};

		/** parser for parsing DenotationSpikeNumbers */
		private static final CsvParserUtils.CollectionParser<DenotationSpikeNumber> PARSER = new CsvParserUtils.CollectionParser<DenotationSpikeNumber>() {
					@Override
					public void parse(String toParse, Collection<DenotationSpikeNumber> toAdd) throws CsvParserException {
						try {
							int number = Integer.parseInt(toParse);
							toAdd.add(new DenotationSpikeNumber(number, null));
						} catch (NumberFormatException nfe) {
							throw new CsvParserException(nfe.getMessage());
						}
					}
				};

		@Override
		public void loadFromCsv(CsvData csv, DenotationPoemModel model, Object... params)
				throws CsvParserException {
			//TODO
			/*
			int currentStropheNumber = -1;
			int currentVerseNumber = -1;
			int maxWordNumber = -1;

			DenotationStrophe currentStrophe = null;
			DenotationVerse currentVerse = null;

			int wordInVerseNumber = 0;
			int verseNumberInStrophe = 0;
			DenotationWord word;

			final Poem poem = model.poem;

			for (List<Object> dataLine : csv.getSection(0).getDataLines()) {

				int column = 0;
				int stropheNumber = CsvParserUtils.getAsInt(dataLine.get(column++));
				int verseNumber = CsvParserUtils.getAsInt(dataLine.get(column++));
				if (currentStropheNumber != stropheNumber) {
					//..new strophe
					verseNumberInStrophe = -1; //..set to -1 because it will be incremented

					//..add current strophe to strophes
					if (currentStrophe != null) {
						model.strophes.put(currentStropheNumber, currentStrophe);
					}
					currentStrophe = new DenotationStrophe(stropheNumber);
					currentStropheNumber = stropheNumber;
				}
				if (currentVerseNumber != verseNumber) {
					//..new verse
					wordInVerseNumber = 0;
					verseNumberInStrophe++;

					currentVerse = new DenotationVerse(verseNumber);
					currentVerseNumber = verseNumber;
					if (currentStrophe == null) {
						throw new CsvParserException("Current strophe is null when attempting to add verse");
					}
					currentStrophe.add(currentVerse);

				} else {
					//..next word in old verse
					wordInVerseNumber++;
				}

				String wordString = CsvParserUtils.getAsString(dataLine.get(column++));
				checkWord(poem, wordString, stropheNumber, verseNumberInStrophe, wordInVerseNumber);

				int wordNumber = CsvParserUtils.getAsInt(dataLine.get(column++));
				Collection<String> words = CsvParserUtils.getAsStringList(dataLine.get(column++), SPLITTER);
				Collection<DenotationSpikeNumber> numbers = CsvParserUtils.getAsList(dataLine.get(column++), SPLITTER, PARSER);

				boolean joined = CsvParserUtils.getAsBool(dataLine.get(column++));
				boolean ignored = CsvParserUtils.getAsBool(dataLine.get(column));

				word = new DenotationWord(wordString, wordNumber, words, numbers, joined, ignored, model);

				//..set word value in DenotationSpikeNumber to value from words (it is null from parsing
				for (DenotationSpikeNumber number : numbers) {
					number.word = word.getWords().toString();
				}

				if (currentVerse == null) {
					throw new CsvParserException("Current verse is null when attempting to add word");
				}
				currentVerse.add(word);
				if (maxWordNumber < wordNumber) {
					maxWordNumber = wordNumber;
				}
			}
			if (currentStrophe != null) {
				//..add last strophe
				model.strophes.put(currentStropheNumber, currentStrophe);
			}
			model.maxWordNumber = maxWordNumber;
			*/
		}

		private void checkWord(Poem poem, String wordString, int stropheNumber, int verseNumberInStrophe, int wordInVerseNumber) throws CsvParserException {
			try {
				final Collection<Verse> verses = poem.getVersesOfStrophe(stropheNumber);
				final Verse verse = ((List<Verse>)verses).get(verseNumberInStrophe);
				final String wordInVerse = verse.getWords(false).get(wordInVerseNumber);
				if (!wordString.equalsIgnoreCase(wordInVerse)) {
					throw new CsvParserException("Word in poem '" + wordInVerse + "' does not match word in loaded file '" + wordString + "'");
				}
			} catch (IllegalArgumentException ex) {
				throw new CsvParserException("In poem there is no word in strophe " + stropheNumber + ", verse " + verseNumberInStrophe + ", word number " + wordInVerseNumber);
			}

		}
	}
}

