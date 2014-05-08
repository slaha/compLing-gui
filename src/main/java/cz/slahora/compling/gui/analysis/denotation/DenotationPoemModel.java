package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.DenotationElement;
import cz.compling.model.denotation.DenotationWord;
import cz.compling.text.poem.Poem;
import cz.compling.text.poem.Verse;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.PipeArrayList;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.CsvParserUtils;

import java.util.*;

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

	public DenotationWord getWord(int wordNumber) {
		return denotation.getWord(wordNumber);
	}

	/*********************************/
	/*                               */
	/*              CSV              */
	/*                               */
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


			final IDenotation denotation = object.denotation;

			for (int wordNumber = 1; wordNumber <= object.getCountOfWords(); wordNumber++) {

				DenotationWord word = denotation.getWord(wordNumber);
				final CsvData.CsvDataSection section = csvData.getCurrentSection();
				section.startNewLine();
				section.addData(word.getStropheNumber());
				section.addData(word.getVerseNumber());
				section.addData(word.getWord());
				section.addData(word.getNumber());
				section.addData(toNumberPipeList(word.getWords()));
				section.addData(new PipeArrayList<DenotationElement>(word.getDenotationElements()));
				section.addData(word.isJoined());
				section.addData(word.isIgnored());
			}

			return csvData;
		}

		private PipeArrayList<Integer> toNumberPipeList(List<DenotationWord> words) {
			PipeArrayList<Integer> list = new PipeArrayList<Integer>(words.size());
			for (DenotationWord word : words) {
				list.add(word.getNumber());
			}
			return list;
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

		/** parser for parsing DenotationElementd */

		private static final CsvParserUtils.CollectionParser<DenotationElement> PARSER = new CsvParserUtils.CollectionParser<DenotationElement>() {

				@Override
				public void parse(String toParse, Collection<DenotationElement> toAdd) throws CsvParserException {
					try {
						int number = Integer.parseInt(toParse);
						toAdd.add(new DenotationElement(null, number));
					} catch (NumberFormatException nfe) {
						throw new CsvParserException(nfe.getMessage());
					}
				}
			};

		private class WordHolder {
			private final DenotationWord word;
			private final Collection<Integer> joinedWords;
			private final Collection<Integer> denotationElements;

			public WordHolder(DenotationWord word, Collection<Integer> words, Collection<Integer> numbers) {
				this.word = word;
				this.joinedWords = words;
				this.denotationElements = numbers;
			}
		}


		@Override
		public void loadFromCsv(CsvData csv, DenotationPoemModel model, Object... params)
				throws CsvParserException {

			final Map<Integer, WordHolder> allParsedWords = new LinkedHashMap<Integer, WordHolder>();
			final IDenotation denotation = model.denotation;
			denotation.clearAllWords();

			verseInStrophe = -1;
			wordInVerseNumber = 0;
			lastVerse = Integer.MIN_VALUE;
			lastStrophe = Integer.MIN_VALUE;
			for (List<Object> dataLine : csv.getSection(0).getDataLines()) {

				int column = 0;
				int stropheNumber = CsvParserUtils.getAsInt(dataLine.get(column++));
				int verseNumber = CsvParserUtils.getAsInt(dataLine.get(column++));
				String wordValue = CsvParserUtils.getAsString(dataLine.get(column++));
				int wordNumber = CsvParserUtils.getAsInt(dataLine.get(column++));

				checkWord(model.poem, wordValue, stropheNumber, verseNumber);

				Collection<Integer> words = CsvParserUtils.getAsIntList(dataLine.get(column++), SPLITTER);
				Collection<Integer> numbers = CsvParserUtils.getAsIntList(dataLine.get(column++), SPLITTER);

				boolean joined = CsvParserUtils.getAsBool(dataLine.get(column++));
				boolean ignored = CsvParserUtils.getAsBool(dataLine.get(column));

				DenotationWord word = new DenotationWord(wordValue, wordNumber, verseNumber, stropheNumber);
				allParsedWords.put(wordNumber, new WordHolder(word, words, numbers));

				denotation.addNewWord(wordNumber, word);
				denotation.ignoreWord(wordNumber, ignored);
			}



			for (WordHolder holder : allParsedWords.values()) {
				final int thisNumber = holder.word.getNumber();
				final List<DenotationElement> elements = holder.word.getDenotationElements();
				for (int i = 0; i < elements.size(); i++) {
					denotation.removeElement(thisNumber, elements.get(0));
				}

				if (holder.word.isIgnored()) {
					continue;
				}

				for (int element : holder.denotationElements) {
					denotation.addNewElementTo(thisNumber, element);
				}

				for (Integer joinedWord : holder.joinedWords) {
					if (thisNumber != joinedWord) {
						denotation.joinWords(thisNumber, joinedWord);
					}
				}
			}
		}

		private int countElements(Collection<Integer> denotationElements) {

			final HashSet<Integer> elementNumbers = new HashSet<Integer>();
			for (Integer element : denotationElements) {
				elementNumbers.add(element);
			}

			return denotationElements.size() - 1;
		}

		int verseInStrophe = -1;
		int wordInVerseNumber = 0;
		int lastVerse = Integer.MIN_VALUE;
		int lastStrophe = Integer.MIN_VALUE;

		private void checkWord(Poem poem, String wordValue, int stropheNumber, int verseNumber) throws CsvParserException {
			if (lastStrophe != stropheNumber) {
				lastStrophe = stropheNumber;
				verseInStrophe = -1;
			}

			if (lastVerse != verseNumber) {
				lastVerse = verseNumber;
				wordInVerseNumber = 0;
				verseInStrophe++;
			} else {
				wordInVerseNumber++;
			}
			doCheck(poem, wordValue, stropheNumber, verseInStrophe, wordInVerseNumber);
		}

		private void doCheck(Poem poem, String wordString, int stropheNumber, int verseNumberInStrophe, int wordInVerseNumber) throws CsvParserException {


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

