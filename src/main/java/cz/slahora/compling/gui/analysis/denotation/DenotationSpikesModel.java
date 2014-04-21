package cz.slahora.compling.gui.analysis.denotation;

import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.PipeArrayList;
import cz.slahora.compling.gui.utils.CsvParserUtils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.*;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 12.4.14 10:38</dd>
 * </dl>
 */
public class DenotationSpikesModel implements Csv<DenotationSpikesModel> {

	/**
	 * Key: number of spike
	 * value: spike
	*/
	private final TIntObjectMap<Spike> spikes;

	/** no. of current spike (used for creating new spike) */
	private int currentSpike;

	public DenotationSpikesModel() {
		spikes = new TIntObjectHashMap<Spike>();
	}

	public int getSpikesCount() {
		return spikes.size();
	}

	public void addNewSpike() {
		Spike spike = new Spike(++currentSpike);
		spikes.put(currentSpike, spike);
	}

	/**
	 * Returns spike for {@code row}<sup>th</sup> row of table
	 *
	 * @return Spike which should be displayed on {@code row}<sup>th</sup> row of table
	 */
	public Spike getSpikeOnRow(int row) {
		int[] keys = spikes.keys();
		Arrays.sort(keys);
		if (row > keys.length) {
			return null;
		}
		return spikes.get(keys[row]);
	}

	/**
	 * Remove spike with number {@code spikeNumber}.
	 *
	 * @param spikeNumber number of spike to remove.
	 *
	 * @return the lowest number of {@code DenotationPoemModel.DenotationWord} which was in removed spike
	 */
	public int removeSpike(int spikeNumber) {
		Spike spike = spikes.remove(spikeNumber);
		int lowestWordNumber = Integer.MAX_VALUE;
		for (DenotationPoemModel.DenotationWord word : spike.getWords().keySet()) {
			word.getElementInSpike(spike).onRemoveFromSpike(spike);
			if (word.getNumber() < lowestWordNumber) {
				lowestWordNumber = word.getNumber();
			}
		}
		spike.words.clear();
		return lowestWordNumber;
	}

	/**
	 * Checks if there is at least one Spike
	 */
	public boolean hasSpikes() {
		return !spikes.isEmpty();
	}

	/**
	 * @return all Spikes as sorted array (by Spike's number)
	 */
	public Spike[] getSpikes() {
		Spike[] values = spikes.values(new Spike[spikes.size()]);
		Arrays.sort(values, new Comparator<Spike>() {
			@Override
			public int compare(Spike o1, Spike o2) {
				return o1.getNumber() - o2.getNumber();
			}
		});
		return values;
	}

	@Override
	public CsvSaver<DenotationSpikesModel> getCsvSaver() {
		return new DenotationSpikesModelSaver();
	}

	@Override
	public boolean supportsCsvImport() {
		return true;
	}

	@Override
	public CsvLoader<DenotationSpikesModel> getCsvLoader() {
		return new DenotationSpikesModelLoader();
	}

	/**
	 * Spike
	 */
	public static class Spike {
		private final DenotationWordsMap words;

		/** number of Spike */
		private final int number;

		public Spike(int number) {
			this.number = number;
			words = new DenotationWordsMap();
		}

		public void add(DenotationPoemModel.DenotationWord word, DenotationPoemModel.DenotationSpikeNumber spikeNumber) {
			words.put(word, spikeNumber);
		}

		public int getNumber() {
			return number;
		}

		public DenotationWordsMap getWords() {
			return words;
		}

		@Override
		public String toString() {
			return String.valueOf(number);
		}

		public void remove(DenotationPoemModel.DenotationWord word) {
			words.remove(word);
		}

		public void toCsv(CsvData data) {
			data.getCurrentSection().addData(number);
			PipeArrayList<SpikeWordBundle> wordNumbers = new PipeArrayList<SpikeWordBundle>();
			for (DenotationPoemModel.DenotationWord word : words.keySet()) {
				final DenotationPoemModel.DenotationSpikeNumber spikeNumber = word.getElementInSpike(this);
				wordNumbers.add(new SpikeWordBundle(word.getNumber(), spikeNumber.getNumber(), spikeNumber.getWord()));
			}
			data.getCurrentSection().addData(wordNumbers);
		}
	}

	public static class DenotationWordsMap extends HashMap<DenotationPoemModel.DenotationWord, DenotationPoemModel.DenotationSpikeNumber> {
		@Override
		public String toString() {
			if (isEmpty()) {
				return "–";
			}
			final Iterator<DenotationPoemModel.DenotationWord> iterator = keySet().iterator();
			StringBuilder sb = new StringBuilder();
			appendWord(sb, iterator.next());
			while (iterator.hasNext()) {
				sb.append(", ");
				appendWord(sb, iterator.next());
			}
			return sb.toString();
		}

		void appendWord(StringBuilder sb, DenotationPoemModel.DenotationWord word) {
			sb.append(word.getWords()).append(' ').append(word.getElements());
		}

		public String toStringForSpike(Spike s) {
			if (isEmpty()) {
				return "–";
			}

			final List<DenotationPoemModel.DenotationWord> sorted = new ArrayList<DenotationPoemModel.DenotationWord>(keySet());
			Collections.sort(sorted, new Comparator<DenotationPoemModel.DenotationWord>() {
				@Override
				public int compare(DenotationPoemModel.DenotationWord o1, DenotationPoemModel.DenotationWord o2) {
					return o1.getNumber() - o2.getNumber();
				}
			});
			final Iterator<DenotationPoemModel.DenotationWord> iterator = sorted.iterator();
			StringBuilder sb = new StringBuilder();
			appendWordSpike(sb, iterator.next(), s);
			while (iterator.hasNext()) {
				sb.append(", ");
				appendWordSpike(sb, iterator.next(), s);
			}
			return sb.toString();
		}

		void appendWordSpike(StringBuilder sb, DenotationPoemModel.DenotationWord word, Spike s) {
			final DenotationPoemModel.DenotationSpikeNumber spikeNumber = word.getElementInSpike(s);
			sb.append(spikeNumber.getWord()).append(' ').append(spikeNumber);
		}
	}

	/**
	 * This is helper class for saving
	 * <ul>
	 *     <li>Number of word</li>
	 *     <li>Number of denotation element</li>
	 *     <li>String which represents the DenotationWord</li>
	 * </ul>
	 * into csv file
	 */
	private static class SpikeWordBundle {

		/** splitter for values - when saving */
		public static final char SPLITTER_CHAR = '\\';
		/** splitter for values - when loading */
		public static final String SPLITTER = "\\\\";


		private final int wordNumber;
		private final int elementNumber;
		private final String wordAsString;

		public SpikeWordBundle(int wordNumber, int elementNumber, String elementInSpike) {
			this.wordNumber = wordNumber;
			this.elementNumber = elementNumber;
			this.wordAsString = elementInSpike;
		}


		@Override
		public String toString() {
			return "[" + wordNumber + SPLITTER_CHAR + elementNumber + SPLITTER_CHAR + wordAsString + "]";
		}
	}

	private static class DenotationSpikesModelSaver extends CsvSaver<DenotationSpikesModel> {

		@Override
		public CsvData saveToCsv(DenotationSpikesModel object, Object... params) {
			CsvData data = new CsvData();
			data.addSection();
			data.getCurrentSection().addHeader("Spike number");
			data.getCurrentSection().addHeader("Word number(s) [word number\\denotation element\\value]");
			for (Spike spike : object.getSpikes()) {
				data.getCurrentSection().startNewLine();
				spike.toCsv(data);
			}
			return data;
		}
	}

	private static class DenotationSpikesModelLoader extends CsvLoader<DenotationSpikesModel> {

		/**
		 * @param params [0]..DenotationSpikesModel; [1]..DenotationPoemModel
		 */
		@Override
		public void loadFromCsv(CsvData csv, DenotationSpikesModel objectToLoad, Object... params) throws CsvParserException {
			DenotationSpikesModel spikesModel = (DenotationSpikesModel) params[0];
			DenotationPoemModel poemModel = (DenotationPoemModel) params[1];
			final CsvParserUtils.CollectionSplitter splitter = new CsvParserUtils.CollectionSplitter() {
				@Override
				public String getSplitter() {
					return PipeArrayList.SPLITTER;
				}
			};
			final CsvParserUtils.CollectionParser<SpikeWordBundle> parser = new CsvParserUtils.CollectionParser<SpikeWordBundle>() {
				@Override
				public void parse(String toParse, Collection<SpikeWordBundle> toAdd) throws CsvParserException {
					try {
						toParse = toParse.substring(1, toParse.length() - 1); //..remove [ and ]
						String[] split = toParse.split(SpikeWordBundle.SPLITTER);
						int wordNumber = Integer.parseInt(split[0]);
						int elementNumber = Integer.parseInt(split[1]);
						String word = split[2];
						toAdd.add(new SpikeWordBundle(wordNumber, elementNumber, word));
					} catch (NumberFormatException nfe) {
						throw new CsvParserException(nfe.getMessage());
					} catch (IndexOutOfBoundsException ioobe) {
						throw new CsvParserException(ioobe.getMessage());
					}
				}
			};
			int maxNumber = 0;
			for (List<Object> objects : csv.getCurrentSection().getDataLines()) {
				int number = CsvParserUtils.getAsInt(objects.get(0));
				Spike spike = new Spike(number);

				Collection<SpikeWordBundle> wordsNumbers = CsvParserUtils.getAsList(objects.get(1), splitter, parser);
				for (SpikeWordBundle bundle : wordsNumbers) {

					DenotationPoemModel.DenotationWord word = poemModel.getWord(bundle.wordNumber);
					final DenotationPoemModel.DenotationSpikeNumber element = word.getFreeElement(bundle.elementNumber);
					spike.add(word, element);
					element.onAddToSpike(spike, bundle.wordAsString);
				}
				spikesModel.spikes.put(number, spike);
				if (maxNumber < number) {
					maxNumber = number;
				}
			}
			spikesModel.currentSpike = maxNumber;
		}
	}
}
