package cz.slahora.compling.gui.analysis.denotation;

import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.PipeArrayList;
import cz.slahora.compling.gui.utils.CsvParserUtils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.text.ParseException;
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

	private final TIntObjectMap<Spike> spikes;
	private int currentSpike = 0;

	public DenotationSpikesModel() {
		spikes = new TIntObjectHashMap<Spike>();
	}

	public int getSpikesCount() {
		return spikes.size();
	}

	public void addTo(int spikeNumber, DenotationPoemModel.DenotationWord word) {
		spikes.get(spikeNumber).add(word);
	}

	public void addNewSpike() {
		Spike spike = new Spike(++currentSpike);
		spikes.put(currentSpike, spike);
	}

	public Spike getSpikeOnRow(int row) {
		int[] keys = spikes.keys();
		Arrays.sort(keys);
		if (row > keys.length) {
			return null;
		}
		return spikes.get(keys[row]);
	}

	public int removeSpike(int spikeNumber) {
		Spike spike = spikes.remove(spikeNumber);
		int lowestWordNumber = Integer.MAX_VALUE;
		for (DenotationPoemModel.DenotationWord word : spike.getWords()) {
			word.getElementInSpike(spike).onRemoveFromSpike(spike);
			if (word.getNumber() < lowestWordNumber) {
				lowestWordNumber = word.getNumber();
			}
		}
		spike.words.clear();
		return lowestWordNumber;
	}

	public boolean hasSpikes() {
		return !spikes.isEmpty();
	}

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

	public static class Spike {
		private final List<DenotationPoemModel.DenotationWord> words;
		private final int number;

		public Spike(int number) {
			this.number = number;
			words = new ArrayList<DenotationPoemModel.DenotationWord>() {
				@Override
				public String toString() {
					if (isEmpty()) {
						return "–";
					}
					StringBuilder sb = new StringBuilder();
					appendWord(sb, get(0));
					for (int i = 1; i < size(); i++) {
						sb.append(", ");
						appendWord(sb, get(i));
					}
					return sb.toString();
				}

				void appendWord(StringBuilder sb, DenotationPoemModel.DenotationWord word) {
					sb.append(word.getWords()).append(' ').append(word.getElements());
				}
			};
		}

		public void add(DenotationPoemModel.DenotationWord word) {
			words.add(word);
		}

		public int getNumber() {
			return number;
		}

		public List<DenotationPoemModel.DenotationWord> getWords() {
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
			PipeArrayList<Integer> wordNumbers = new PipeArrayList<Integer>();
			for (DenotationPoemModel.DenotationWord word : words) {
				wordNumbers.add(word.getNumber());
			}
			data.getCurrentSection().addData(wordNumbers);
		}
	}

	private static class DenotationSpikesModelSaver extends CsvSaver<DenotationSpikesModel> {

		@Override
		public CsvData saveToCsv(DenotationSpikesModel object, Object... params) {
			CsvData data = new CsvData();
			data.addSection();
			data.getCurrentSection().addHeader("Spike number");
			data.getCurrentSection().addHeader("Word number(s)");
			for (Spike spike : object.getSpikes()) {
				data.getCurrentSection().startNewLine();
				spike.toCsv(data);
			}
			return data;
		}
	}

	private static class DenotationSpikesModelLoader extends CsvLoader<DenotationSpikesModel> {

		@Override
		public void loadFromCsv(CsvData csv, DenotationSpikesModel objectToLoad, Object... params) throws ParseException {
			DenotationSpikesModel spikesModel = (DenotationSpikesModel) params[0];
			DenotationPoemModel poemModel = (DenotationPoemModel) params[1];
			final CsvParserUtils.CollectionSplitter splitter = new CsvParserUtils.CollectionSplitter() {
				@Override
				public String getSplitter() {
					return PipeArrayList.SPLITTER;
				}
			};
			for (List<Object> objects : csv.getCurrentSection().getDataLines()) {
				int number = CsvParserUtils.getAsInt(objects.get(0));
				Spike spike = new Spike(number);
				Collection<Integer> wordsNumbers = CsvParserUtils.getAsIntList(objects.get(1), splitter);
				for (Integer wordNumber : wordsNumbers) {
					DenotationPoemModel.DenotationWord word = poemModel.getWord(wordNumber);
					spike.add(word);
					word.onAddToSpike(spike);
				}
				spikesModel.spikes.put(number, spike);
			}

		}
	}
}
