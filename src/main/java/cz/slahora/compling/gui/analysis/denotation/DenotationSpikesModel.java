package cz.slahora.compling.gui.analysis.denotation;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
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
public class DenotationSpikesModel {

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

	public void removeSpike(int spike) {
		spikes.remove(spike);
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
	}
}
