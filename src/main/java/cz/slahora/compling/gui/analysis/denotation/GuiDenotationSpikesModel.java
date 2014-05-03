package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.Spike;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.PipeArrayList;
import cz.slahora.compling.gui.utils.CsvParserUtils;
import org.apache.commons.lang.text.StrBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
public class GuiDenotationSpikesModel implements Csv<GuiDenotationSpikesModel> {

	private final IDenotation denotation;

	/** no. of current spike (used for creating new spike) */
	private int currentSpike;

	public GuiDenotationSpikesModel(IDenotation denotation) {
		this.denotation = denotation;
	}

	public int getSpikesCount() {
		return denotation.getSpikes().size();
	}

	/**
	 * Returns spike for {@code row}<sup>th</sup> row of table
	 *
	 * @return Spike which should be displayed on {@code row}<sup>th</sup> row of table
	 */
	public Spike getSpikeOnRow(int row) {
		return getSpikes().get(row);
	}

	/**
	 * Remove spike with number {@code spikeNumber}.
	 *
	 * @param spikeNumber number of spike to remove.
	 *
	 * @return the lowest number of {@code DenotationPoemModel.DenotationWord} which was in removed spike
	 */
	public int removeSpike(int spikeNumber) {
		return denotation.removeSpike(spikeNumber);
	}

	/**
	 * @return all Spikes as sorted array (by Spike's number)
	 */
	public List<Spike> getSpikes() {
		final List<Spike> spikes = (List<Spike>) denotation.getSpikes();
		Collections.sort(spikes, new Comparator<Spike>() {
			@Override
			public int compare(Spike o1, Spike o2) {
				return o1.getNumber() - o2.getNumber();
			}
		});
		return spikes;
	}

	@Override
	public CsvSaver<GuiDenotationSpikesModel> getCsvSaver() {
		return new DenotationSpikesModelSaver();
	}

	@Override
	public boolean supportsCsvImport() {
		return true;
	}

	@Override
	public CsvLoader<GuiDenotationSpikesModel> getCsvLoader() {
		return new DenotationSpikesModelLoader();
	}

	public boolean isAnySpikeInTheTable() {
		return getSpikesCount() > 0;
	}

	public void createNewSpike() {
		denotation.createNewSpike();
	}

	public String toStringForSpike(Spike spike) {
		if (spike.getWords().isEmpty()) {
			return "-";
		}
		StrBuilder b = new StrBuilder();
		b.appendWithSeparators(spike.getWords().keySet(), ", ");
		return b.toString();
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

	private static class DenotationSpikesModelSaver extends CsvSaver<GuiDenotationSpikesModel> {

		@Override
		public CsvData saveToCsv(GuiDenotationSpikesModel object, Object... params) {
			CsvData data = new CsvData();
			data.addSection();
			data.getCurrentSection().addHeader("Spike number");
			data.getCurrentSection().addHeader("Word number(s) [word number\\denotation element\\value]");
			for (Spike spike : object.getSpikes()) {
				data.getCurrentSection().startNewLine();
//				spike.toCsv(data);
			}
			return data;
		}
	}

	private static class DenotationSpikesModelLoader extends CsvLoader<GuiDenotationSpikesModel> {

		/**
		 * @param params [0]..DenotationSpikesModel; [1]..DenotationPoemModel
		 */
		@Override
		public void loadFromCsv(CsvData csv, GuiDenotationSpikesModel objectToLoad, Object... params) throws CsvParserException {
			GuiDenotationSpikesModel spikesModel = (GuiDenotationSpikesModel) params[0];
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
			};//TODO
			/*
			int maxNumber = 0;
			for (List<Object> objects : csv.getCurrentSection().getDataLines()) {
				int number = CsvParserUtils.getAsInt(objects.get(0));
				Spike spike = new Spike(number);

				Collection<SpikeWordBundle> wordsNumbers = CsvParserUtils.getAsList(objects.get(1), splitter, parser);
				for (SpikeWordBundle bundle : wordsNumbers) {

					DenotationWord word = poemModel.getWord(bundle.wordNumber);

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
			*/
		}
	}
}
