package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.*;
import org.javatuples.Pair;

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

	private final Comparator<Spike> SPIKE_NUMBER_COMPARATOR = new Comparator<Spike>() {
		@Override
		public int compare(Spike o1, Spike o2) {
			return o1.getNumber() - o2.getNumber();
		}
	};

	private final Comparator<Spike> SPIKE_DIFFUSION_COMPARATOR = new Comparator<Spike>() {
		@Override
		public int compare(Spike o1, Spike o2) {
			double d = getDiffusionFor(o1.getNumber()) - getDiffusionFor(o2.getNumber());
			if (d != 0) {
				return d < 0 ? -1 : 1;
			}
			return SPIKE_NUMBER_COMPARATOR.compare(o1, o2);
		}
	};

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

	public double computeTopikalnost(Spike spike) {
		final double cardinalNumber = (double) core.getCoreCardinalNumber();

		return denotation.computeTopikalnost(spike, cardinalNumber);
	}

	public double getTextCompactness() {
		return denotation.getTextCompactness();
	}

	public double getTextCentralization() {
		return denotation.getTextCentralization();
	}

	public double getMacIntosh() {
		return denotation.getMacIntosh();
	}

	public double getDiffusionFor(int spike) {
		return denotation.getDiffusionFor(spike);
	}

	public int getSpikeSize(int spike) {
		return denotation.getSpike(spike).size();
	}

	public List<Spike> getSpikesInExtendedCore() {
		final Spike coreSpiceMaxDiffusion = findCoreWithMaxDiffusion();
		if (coreSpiceMaxDiffusion == null) {
			return null;
		}
		final double maxDiffusion = getDiffusionFor(coreSpiceMaxDiffusion.getNumber());

		List<Spike> spikes = new ArrayList<Spike>();
		for (Spike spike : denotation.getSpikes()) {
			if (getSpikeSize(spike.getNumber()) > 1 && getDiffusionFor(spike.getNumber()) <= maxDiffusion) {
				spikes.add(spike);
			}
		}

		Collections.sort(spikes, SPIKE_DIFFUSION_COMPARATOR);

		return spikes;
	}

	public Spike findCoreWithMaxDiffusion() {
		final List<Spike> core = getTextCoreSpikes().getCore();
		if (core.isEmpty()) {
			return null;
		}

		Spike maxDiffusionSpike = core.get(0);
		double maxDiffusion = getDiffusionFor(maxDiffusionSpike.getNumber());
		for (int i = 1; i < core.size(); i++) {
			final double currentDiffusion = getDiffusionFor(core.get(i).getNumber());
			if (maxDiffusion < currentDiffusion) {
				maxDiffusion = currentDiffusion;
				maxDiffusionSpike = core.get(i);
			}
		}
		return maxDiffusionSpike;
	}

	public List<Pair<Spike, Double> > getDiffusionForAll() {
		List<Pair<Spike, Double> > list = new ArrayList<Pair<Spike, Double>>();
		for (int spike = 1; spike <= getSpikesCount(); spike++) {
			if (getSpikeSize(spike) > 1) {
				Pair<Spike, Double> pair = new Pair<Spike, Double>(denotation.getSpike(spike), getDiffusionFor(spike));
				list.add(pair);
			}
		}

		Collections.sort(list, new Comparator<Pair<Spike, Double>>() {
			@Override
			public int compare(Pair<Spike, Double> o1, Pair<Spike, Double> o2) {
				return SPIKE_DIFFUSION_COMPARATOR.compare(o1.getValue0(), o2.getValue0());
			}
		});

		return list;
	}

	public GuiPoemAsSpikeNumbers getPoemAsSpikeNumbers() {
		return denotation.getPoemAsSpikeNumbers();
	}

	public List<Coincidence> getCoincidenceFor(int spikeNumber) {
		return denotation.getCoincidenceFor(spikeNumber);
	}

	public List<Spike> getAllSpikes() {
		return new ArrayList<Spike>(denotation.getSpikes());
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
			Collections.sort(core, SPIKE_NUMBER_COMPARATOR);
			return core;
		}

		public List<Spike> getNotInCore() {
			Collections.sort(outsideCore, SPIKE_NUMBER_COMPARATOR);
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

		public int getCoreSize() {
			return 0;
		}
	}
}
