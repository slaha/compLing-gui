package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.DenotationElement;
import cz.compling.model.denotation.DenotationWord;
import cz.compling.model.denotation.Spike;

import java.util.ArrayList;
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
 * <dd> 11.5.14 12:19</dd>
 * </dl>
 */
public class GuiDenotationResultsModel {
	private final IDenotation denotation;
	private final TextCore core;

	public GuiDenotationResultsModel(IDenotation denotation) {
		this.denotation = denotation;
		this.core = new TextCore();
	}

	public int getSpikesCount() {
		return denotation.getSpikes().size();
	}

	public List<Integer> getPositionSpike(int spikeNumber) {
		List<Integer> positionSpike = new ArrayList<Integer>();
		final Spike spike = denotation.getSpike(spikeNumber);
		for (DenotationWord word : spike.getWords()) {
			for (DenotationElement element : word.getDenotationElements()) {
				if (element.isInSpike() && element.getSpike().getNumber() == spikeNumber) {
					positionSpike.add(element.getNumber());
				}
			}
		}
		Collections.sort(positionSpike);
		return positionSpike;
	}

	public TextCore getTextCoreSpikes() {
		return core;
	}

	class TextCore {
		private final List<Spike> core = new ArrayList<Spike>();
		private final List<Spike> outsideCore = new ArrayList<Spike>();

		private TextCore() {
			for (Spike spike : denotation.getSpikes()) {
				if (spike.getWords().size() >= 2) {
					core.add(spike);
				} else {
					outsideCore.add(spike);
				}
			}
		}

		public void add(Spike spike) {
			core.add(spike);
			outsideCore.remove(spike);
		}

		public void remove(Spike spike) {
			core.remove(spike);
			outsideCore.add(spike);
		}

		public int size() {
			return core.size();
		}

		public List<Spike> getCore() {
			Collections.sort(core, new Comparator<Spike>() {
				@Override
				public int compare(Spike o1, Spike o2) {
					return o1.getNumber() - o2.getNumber();
				}
			});
			return core;
		}

		public List<Spike> getNotInCore() {
			Collections.sort(outsideCore, new Comparator<Spike>() {
				@Override
				public int compare(Spike o1, Spike o2) {
					return o1.getNumber() - o2.getNumber();
				}
			});
			return outsideCore;
		}

		public boolean isInCore(Spike spike) {
			return core.contains(spike);
		}

		public int getCoreCardinalNumber() {
			int i = 0;
			for (Spike spike : core) {
				i += spike.getWords().size();
			}
			return i;
		}
	}
}
