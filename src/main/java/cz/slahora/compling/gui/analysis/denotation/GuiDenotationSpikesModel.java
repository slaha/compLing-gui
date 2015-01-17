package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.DenotationElement;
import cz.compling.model.denotation.DenotationWord;
import cz.compling.model.denotation.Spike;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.PipeArrayList;
import cz.slahora.compling.gui.utils.CsvParserUtils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.StringUtils;

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
public class GuiDenotationSpikesModel implements Csv<GuiDenotationSpikesModel> {

	private final IDenotation denotation;

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

	private void addSpike(Spike spike) {
		denotation.addSpike(spike);

	}

	public String toStringForSpike(Spike spike) {
		if (spike.getWords().isEmpty()) {
			return "-";
		}
		final TIntObjectMap<String> map = getElementsInSpike(spike);
		final int[] keys = map.keys();
		Arrays.sort(keys);
		StringBuilder b = new StringBuilder();
		b.append(map.get(keys[0]));
		for (int i = 1; i < keys.length; i++) {
			b
				.append(", ")
				.append(map.get(keys[i]));
		}

		return b.toString();
	}

	private TIntObjectMap<String> getElementsInSpike(Spike spike) {
		TIntObjectMap<String> map = new TIntObjectHashMap<String>();
		for (DenotationWord dw : spike.getWords()) {
			for (DenotationElement element : dw.getDenotationElements()) {
				if (element.getSpike() == null) {
					continue;
				}
				if (element.getSpike().getNumber() == spike.getNumber()) {
					String s;
					if (StringUtils.isEmpty(element.getText())) {
						s = dw.toString();
					} else {
						s = element.getText();
					}
					map.put(dw.getNumber(), (s + " " + element.getNumber()));
				}
			}
		}
		return map;
	}

	private static class DenotationSpikesModelSaver extends CsvSaver<GuiDenotationSpikesModel> {

		@Override
		public CsvData saveToCsv(GuiDenotationSpikesModel object, Object... params) {
			CsvData data = new CsvData();
			data.addSection();
			final CsvData.CsvDataSection section = data.getCurrentSection();
			section.addHeader("Spike number");
			section.addHeader("Word number(s) [word number\\denotation element\\value]");
			for (Spike spike : object.getSpikes()) {
				section.startNewLine();
				for (DenotationWord w : spike.getWords()) {
					if (w.isInSpike(spike)) {
						section.addData(new GuiSpikeWordsBundle(w.getNumber(), spike.getNumber(), getAlias(spike, w)));
					}
				}

			}
			return data;
		}

		String getAlias(Spike spike, DenotationWord word) {
			for (DenotationElement element : word.getDenotationElements()) {
				if (word.isInSpike(spike)) {
					return element.getText();
				}
			}
			throw new IllegalArgumentException("word " + word + " (" + word.getNumber() + ") is not in spike " + spike + " (" + spike.getNumber() + ")");
		}
	}

	private static class DenotationSpikesModelLoader extends CsvLoader<GuiDenotationSpikesModel> {

		/**
		 * @param params [0]..DenotationSpikesModel; [1]..DenotationPoemModel
		 */
		@Override
		public void loadFromCsv(CsvData csv, GuiDenotationSpikesModel objectToLoad, Object... params) throws CsvParserException {
			GuiDenotationSpikesModel spikesModel = (GuiDenotationSpikesModel) params[0];
			GuiDenotationPoemModel poemModel = (GuiDenotationPoemModel) params[1];
			final CsvParserUtils.CollectionSplitter splitter = new CsvParserUtils.CollectionSplitter() {
				@Override
				public String getSplitter() {
					return PipeArrayList.SPLITTER;
				}
			};
			final CsvParserUtils.CollectionParser<GuiSpikeWordsBundle> parser = new CsvParserUtils.CollectionParser<GuiSpikeWordsBundle>() {
				@Override
				public void parse(String toParse, Collection<GuiSpikeWordsBundle> toAdd) throws CsvParserException {
					try {
						toParse = toParse.substring(1, toParse.length() - 1); //..remove [ and ]
						String[] split = toParse.split(GuiSpikeWordsBundle.SPLITTER);
						int wordNumber = Integer.parseInt(split[0]);
						int elementNumber = Integer.parseInt(split[1]);
						String word = split[2];
						toAdd.add(new GuiSpikeWordsBundle(wordNumber, elementNumber, word));
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
				spikesModel.addSpike(spike);

				Collection<GuiSpikeWordsBundle> wordsNumbers = CsvParserUtils.getAsList(objects.get(1), splitter, parser);
				for (GuiSpikeWordsBundle bundle : wordsNumbers) {

					DenotationWord word = poemModel.getWord(bundle.wordNumber);
					spike.addWord(word, bundle.wordAsString, bundle.elementNumber);
				}
				if (maxNumber < number) {
					maxNumber = number;
				}

			}
		}
	}
}
