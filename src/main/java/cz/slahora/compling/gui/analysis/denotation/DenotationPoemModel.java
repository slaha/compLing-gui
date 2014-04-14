package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.text.poem.Poem;
import cz.compling.text.poem.Verse;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.PipeArrayList;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.CsvParserUtils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DenotationPoemModel implements Csv<DenotationPoemModel> {

	private final Poem poem;

	private final TIntObjectMap<DenotationStrophe> strophes;
	private final TIntObjectMap<DenotationWord> allWords;
	private int maxWordNumber;

	public DenotationPoemModel(WorkingText text) {
		this(text, true);
	}

	private DenotationPoemModel(WorkingText text, boolean compute) {
		this.poem = text.getCompLing().poemAnalysis().poem;
		this.strophes = new TIntObjectHashMap<DenotationStrophe>();
		this.allWords = new TIntObjectHashMap<DenotationWord>();

		if (!compute) {
			return;
		}
		DenotationStrophe strophe;
		DenotationVerse denotationVerse;
		int numberOfWord = 1;
		int numberOfVerse = 1;
		for (int stropheNmbr = 1; stropheNmbr <= poem.getCountOfStrophes(); stropheNmbr++) {
			Collection<Verse> versesOfStrophe = poem.getVersesOfStrophe(stropheNmbr);
			strophe = new DenotationStrophe(stropheNmbr);
			for (Verse verse : versesOfStrophe) {
				denotationVerse = new DenotationVerse(numberOfVerse++);
				for (String word : verse.getWords(false)) {
					DenotationWord denotationWord = new DenotationWord(word, numberOfWord, this);
					numberOfWord++;

					denotationVerse.add(denotationWord);
				}
				strophe.add(denotationVerse);
			}

			strophes.put(stropheNmbr, strophe);
		}
		this.maxWordNumber = numberOfWord - 1; //..it is incremented once more
	}

	public int getCountOfStrophes() {
		return poem.getCountOfStrophes();
	}

	public DenotationStrophe getStrophe(int strophe) {
		return strophes.get(strophe);
	}

	/*********************************/
	/* CSV */

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

	private void clear() {
		for (DenotationStrophe strophe : strophes.valueCollection()) {
			for (DenotationVerse verse : strophe.verses) {
				verse.words.clear();
			}
			strophe.verses.clear();
		}
		strophes.clear();
		allWords.clear();
		maxWordNumber = -1;
	}

	public DenotationWord getWord(int number) {
		return allWords.get(number);
	}

	public static class DenotationStrophe {
		final List<DenotationVerse> verses;
		private final int number;

		public DenotationStrophe(int stropheNmbr) {
			this.verses = new ArrayList<DenotationVerse>();
			this.number = stropheNmbr;
		}

		public void add(DenotationVerse denotationVerse) {
			verses.add(denotationVerse);
		}

		public int getNumber() {
			return number;
		}

		void toCsv(CsvData csvData) {
			csvData.getCurrentSection().addData(number);
		}
	}

	public static class DenotationVerse {
		final List<DenotationWord> words;
		private final int number;

		public DenotationVerse(int numberOfVerse) {
			this.words = new ArrayList<DenotationWord>();
			this.number = numberOfVerse;
		}

		public void add(DenotationWord denotationWord) {
			words.add(denotationWord);
		}

		public int getNumber() {
			return number;
		}

		public void toCsv(CsvData csvData) {
			csvData.getCurrentSection().addData(number);
		}
	}

	public static class DenotationWord {

		private final List<DenotationSpikeNumber> numbers;
		private final List<String> words;
		private final int number;
		private final DenotationPoemModel model;
		private boolean ignored;

		/** if true this word is joined with another and should be ignored */
		private boolean joined;

		private DenotationWord(DenotationPoemModel model, int number) {
			this.model = model;
			this.number = number;
			model.allWords.put(number, this);

			this.words = new ArrayList<String>() {
				@Override
				public String toString() {
					if (isEmpty()) {
						return "";
					}
					StringBuilder sb = new StringBuilder();
					sb.append(get(0));
					for (int i = 1; i < size(); i++) {
						sb.append(' ').append(get(i));
					}
					return sb.toString();
				}
			};
			this.numbers = new PipeArrayList<DenotationSpikeNumber>();
		}

		public DenotationWord(String word, final int number, DenotationPoemModel model) {
			this(model, number);
			this.words.add(word);
			this.numbers.add(new DenotationSpikeNumber(number));
		}

		public DenotationWord(int number, Collection<String> words, Collection<DenotationSpikeNumber> elements, boolean joined,
				boolean ignored, DenotationPoemModel model) {
			this(model, number);
			this.words.addAll(words);
			this.numbers.addAll(elements);
			this.joined = joined;
			this.ignored = ignored;
		}

		public boolean isIgnored() {
			return ignored;
		}

		public void setIgnored(final boolean ignored) {
			if (this.ignored == ignored) {
				return;
			}
			this.ignored = ignored;
			ForEachRunner runner;
			if (ignored) {
				final int decrement = numbers.size();
				numbers.clear();
				runner = new ForEachRunner() {
					@Override
					public void run(DenotationWord word) {
						word.incrementNumbers(-decrement); //decrement
					}
				};

			} else {
				DenotationWord previousWord = getPreviousWord();
				int value;
				if (previousWord == null) {
					value = 1;
				} else {
					value = previousWord.getHighestNumber().number + 1;
				}

				numbers.add(new DenotationSpikeNumber(value));
				runner = new ForEachRunner() {
					@Override
					public void run(DenotationWord word) {
						word.incrementNumbers(1);
					}
				};
			}
			forEachValue(new ForEach(runner));
		}

		private void incrementNumbers(int increment) {
			for (DenotationSpikeNumber spikeNumber : numbers) {
				spikeNumber.increment(increment);
			}
		}

		public void addElement() {
			if (ignored) {
				return;
			}
			DenotationSpikeNumber spikeNumber = new DenotationSpikeNumber(getHighestNumber().number + 1);
			numbers.add(spikeNumber);
			forEachValue(new ForEach(new ForEachRunner() {
				@Override
				public void run(DenotationWord word) {
					word.incrementNumbers(1);
				}
			}));
		}

		public void removeElement() {
			if (ignored) {
				return;
			}
			numbers.remove(getHighestNumber());
			forEachValue(new ForEach(new ForEachRunner() {
				@Override
				public void run(DenotationWord word) {
					word.incrementNumbers(-1);
				}
			}));
		}

		/**
		 * @return true if join another word; false if not
		 */
		public boolean joinNext() {
			if (ignored) {
				return false;
			}
			DenotationWord next = getNextWord();
			if (next == null) {
				//..end of text
				return false;
			}
			words.addAll(next.words);
			next.words.clear();
			next.numbers.clear();
			next.joined = true;

			forEachValue(new ForEach(new ForEachRunner() {
				@Override
				public void run(DenotationWord word) {
					word.incrementNumbers(-1);
				}
			}));

			return true;
		}

		public void splitLast() {
			if (words.size() == 1) {
				return;
			}
			String remove = words.remove(words.size() - 1);

			final DenotationWord nextWord = getNextWord(false);
			if (nextWord != null) {
				nextWord.words.add(remove);
				DenotationSpikeNumber spikeNumber = new DenotationSpikeNumber(getHighestNumber().number + 1);
				nextWord.numbers.add(spikeNumber);
				nextWord.joined = false;

				forEachValue(new ForEach(new ForEachRunner() {
					@Override
					public void run(DenotationWord word) {
						if (word.number > nextWord.number) {
							word.incrementNumbers(1);
						}
					}
				}));
			}
		}

		public DenotationWord getNextWord() {
			return getNextWord(true);
		}

		private DenotationWord getNextWord(boolean ignoreJoined) {
			DenotationWord w = null;
			int nmbr = number + words.size() - 1; //..current number + joined words - this word
			do {
				if (nmbr > getMaxWordNumber()) {
					break;
				}
				w = getAllWords().get(++nmbr);
			} while (ignoreJoined ? w != null && w.joined : w == null);
			return w;
		}

		private DenotationWord getPreviousWord() {
			DenotationWord w;
			int nmbr = number;
			do {
				w = getAllWords().get(--nmbr);
			} while (w != null && w.joined);
			return w;
		}

		@Override
		public String toString() {
			return words.toString();
		}

		public int getLowestNumber() {
			if (numbers.isEmpty()) {
				throw new IllegalStateException("Numbers were empty when calling getLowestNumber");
			}
			int min = numbers.get(0).number;
			for (int i = 1; i < numbers.size(); i++) {
				if (numbers.get(i).number < min) {
					min = numbers.get(i).number;
				}
			}
			return min;
		}

		public DenotationSpikeNumber getHighestNumber() {
			if (numbers.isEmpty()) {
				throw new IllegalStateException("Numbers were empty when calling getHighestNumber");
			}
			DenotationSpikeNumber max = numbers.get(0);
			for (int i = 1; i < numbers.size(); i++) {
				if (numbers.get(i).number > max.number) {
					max = numbers.get(i);
				}
			}
			return max;
		}

		public int getNumber() {
			return number;
		}

		public boolean canRemoveElement() {
			return numbers.size() > 1;
		}

		public List<String> getWords() {
			return Collections.unmodifiableList(words);
		}

		public List<DenotationSpikeNumber> getElements() {
			return Collections.unmodifiableList(numbers);
		}

		public boolean isJoined() {
			return joined;
		}

		public boolean hasJoined() {
			return words.size() > 1;

		}

		public String getLastJoined() {
			return words.get(words.size() - 1);
		}

		public void toCsv(CsvData csvData) {
			csvData.getCurrentSection().addData(getNumber());
			csvData.getCurrentSection().addData(new PipeArrayList<String>(getWords()));
			csvData.getCurrentSection().addData(getElements());
			csvData.getCurrentSection().addData(isJoined());
			csvData.getCurrentSection().addData(isIgnored());
		}

		private TIntObjectMap<DenotationWord> getAllWords() {
			return model.allWords;
		}

		private void forEachValue(ForEach forEach) {
			getAllWords().forEachValue(forEach);
		}

		public int getMaxWordNumber() {
			return model.maxWordNumber;
		}

		public void onAddToSpike(DenotationSpikesModel.Spike spike) {


		}

		public Collection<DenotationSpikesModel.Spike> getSpikes() {
			List<DenotationSpikesModel.Spike> spikes = new ArrayList<DenotationSpikesModel.Spike>() {
				@Override
				public String toString() {
					if (isEmpty()) {
						return "";
					}
					StringBuilder sb = new StringBuilder();
					sb.append(get(0));
					for (int i = 1; i < size(); i++) {
						sb.append(", ").append(get(i));
					}
					return sb.toString();
				}
			};

			for (DenotationSpikeNumber spikeNumber : numbers) {
				if (spikeNumber.isInSpike()) {
					spikes.add(spikeNumber.spike);
				}
			}
			
			return spikes;
		}

		public boolean hasFreeElement() {
			for (DenotationSpikeNumber spikeNumber : numbers) {
				if (!spikeNumber.isInSpike()) {
					return true;
				}
			}
			return false;
		}

		public DenotationSpikeNumber getFreeElement() {
			for (DenotationSpikeNumber spikeNumber : numbers) {
				if (!spikeNumber.isInSpike()) {
					return spikeNumber;
				}
			}
			throw new IllegalStateException("Word " + words + " (number " + number + ") does not have any free elements");
		}

		public boolean isInSpike() {
			for (DenotationSpikeNumber spikeNumber : numbers) {
				if (spikeNumber.isInSpike()) {
					return true;
				}
			}
			return false;
		}

		public DenotationSpikeNumber getElementInSpike(DenotationSpikesModel.Spike spike) {
			for (DenotationSpikeNumber spikeNumber : numbers) {
				if (spikeNumber.hasSpike(spike)) {
					return spikeNumber;
				}
			}
			throw new IllegalStateException("Word " + words + " (number " + number + ") does not belong to spike " + spike);
		}

		private class ForEach implements TObjectProcedure<DenotationWord> {

			final ForEachRunner runnable;

			private ForEach(ForEachRunner runnable) {
				this.runnable = runnable;
			}

			@Override
			public boolean execute(DenotationWord denotationWord) {
				if (denotationWord.number > number && !denotationWord.joined && !denotationWord.ignored) {
					runnable.run(denotationWord);
				}
				return true;
			}
		}
	}

	public static class DenotationSpikeNumber {
		private int number;
		private DenotationSpikesModel.Spike spike;

		public DenotationSpikeNumber(int number) {
			this.number = number;
		}


		public DenotationSpikeNumber increment(int increment) {
			number += increment;
			return this;
		}

		@Override
		public String toString() {
			return String.valueOf(number);
		}

		public void onAddToSpike(DenotationSpikesModel.Spike spike) {
			this.spike = spike;
		}

		public void onRemoveFromSpike(DenotationSpikesModel.Spike spike) {
			if (this.spike == spike) {
				this.spike = null;
			} else {
				throw new IllegalArgumentException("Spike " + spike + " is not current spike of this number " + this.spike);
			}
		}

		public boolean isInSpike() {
			return spike != null;
		}

		public boolean hasSpike(DenotationSpikesModel.Spike spike) {
			return this.spike == spike;
		}
	}

	private interface ForEachRunner {
		void run(DenotationWord word);
	}

	private static class DenotationPoemModelSaver extends CsvSaver<DenotationPoemModel> {
		@Override
		public CsvData saveToCsv(DenotationPoemModel object, Object... params) {
			CsvData csvData = new CsvData();
			csvData.addSection();
			csvData.getCurrentSection().addHeader("Number of strophe");
			csvData.getCurrentSection().addHeader("Number of verse");
			csvData.getCurrentSection().addHeader("Number of word");
			csvData.getCurrentSection().addHeader("Word(s)");
			csvData.getCurrentSection().addHeader("Denotation element(s)");
			csvData.getCurrentSection().addHeader("Joined");
			csvData.getCurrentSection().addHeader("Ignored");
			for (int stropheNmbr = 1; stropheNmbr <= object.getCountOfStrophes(); stropheNmbr++) {
				DenotationStrophe strophe = object.getStrophe(stropheNmbr);
				if (strophe == null) {
					System.out.println("stropheNmbr = " + stropheNmbr);
				}
				for (DenotationVerse verse : strophe.verses) {
					for (DenotationWord word : verse.words) {
						csvData.getCurrentSection().startNewLine();
						strophe.toCsv(csvData);
						verse.toCsv(csvData);
						word.toCsv(csvData);
					}
				}
			}
			return csvData;
		}
	}

	private class DenotationPoemModelLoader extends CsvLoader<DenotationPoemModel> {
		@Override
		public void loadFromCsv(CsvData csv, DenotationPoemModel model, Object... params)
				throws ParseException {

			model.clear();

			int currentStropheNumber = -1;
			int currentVerseNumber = -1;
			int maxWordNumber = -1;

			DenotationStrophe currentStrophe = null;
			DenotationVerse currentVerse = null;

			DenotationWord word;
			for (List<Object> dataLine : csv.getSection(0).getDataLines()) {
				final CsvParserUtils.CollectionSplitter splitter = new CsvParserUtils.CollectionSplitter() {
					@Override
					public String getSplitter() {
						return PipeArrayList.SPLITTER;
					}
				};
				final CsvParserUtils.CollectionParser<DenotationSpikeNumber> parser = new CsvParserUtils.CollectionParser<DenotationSpikeNumber>() {
					@Override
					public void parse(String toParse, Collection<DenotationSpikeNumber> toAdd) throws ParseException {
						try {
							int number = Integer.parseInt(toParse);
							toAdd.add(new DenotationSpikeNumber(number));
						} catch (NumberFormatException nfe) {
							throw new ParseException(nfe.getMessage(), -1);
						}
					}
				};
				int column = 0;
				int stropheNumber = CsvParserUtils.getAsInt(dataLine.get(column++));
				int verseNumber = CsvParserUtils.getAsInt(dataLine.get(column++));
				int wordNumber = CsvParserUtils.getAsInt(dataLine.get(column++));
				Collection<String> words = CsvParserUtils.getAsStringList(dataLine.get(column++), splitter);
				Collection<DenotationSpikeNumber> numbers = CsvParserUtils.getAsList(dataLine.get(column++), splitter, parser);
				boolean joined = CsvParserUtils.getAsBool(dataLine.get(column++));
				boolean ignored = CsvParserUtils.getAsBool(dataLine.get(column++));

				word = new DenotationWord(wordNumber, words, numbers, joined, ignored, model);
 				if (currentStropheNumber != stropheNumber) {
					if (currentStrophe != null) {
						model.strophes.put(currentStropheNumber, currentStrophe);
					}
					currentStrophe = new DenotationStrophe(stropheNumber);
					currentStropheNumber = stropheNumber;
				}
				if (currentVerseNumber != verseNumber) {
					currentVerse = new DenotationVerse(verseNumber);
					currentVerseNumber = verseNumber;
					if (currentStrophe == null) {
						throw new ParseException("Current strophe is null when attempting to add verse", -1);
					}
					currentStrophe.add(currentVerse);
				}
				if (currentVerse == null) {
					throw new ParseException("Current verse is null when attempting to add word", -1);
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
		}

	}
}
