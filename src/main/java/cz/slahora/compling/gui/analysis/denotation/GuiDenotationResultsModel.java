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

	private final Comparator<Hreb> SPIKE_NUMBER_COMPARATOR = new Comparator<Hreb>() {
		@Override
		public int compare(Hreb o1, Hreb o2) {
			return o1.getNumber() - o2.getNumber();
		}
	};

	private final Comparator<Hreb> SPIKE_DIFFUSION_COMPARATOR = new Comparator<Hreb>() {
		@Override
		public int compare(Hreb o1, Hreb o2) {
			double d = getDiffusionFor(o1.getNumber()) - getDiffusionFor(o2.getNumber());
			if (d != 0) {
				return d < 0 ? -1 : 1;
			}
			return SPIKE_NUMBER_COMPARATOR.compare(o1, o2);
		}
	};
	private Double nonContinuousIndex;
	private Double nonIsolationIndex;
	private Double reachabilityIndex;

	public GuiDenotationResultsModel(IDenotation denotation) {
		this.denotation = denotation;
		this.core = new TextCore();
	}

	public int getHrebsCount() {
		return denotation.getHrebs().size();
	}

	public List<Integer> getPositionHreb(int hrebNumber) {
		List<Integer> positionHreb = new ArrayList<Integer>();
		final Hreb hreb = denotation.getHreb(hrebNumber);
		for (DenotationWord word : hreb.getWords()) {
			for (DenotationElement element : word.getDenotationElements()) {
				if (element.isInHreb() && element.getHreb().getNumber() == hrebNumber) {
					positionHreb.add(element.getNumber());
				}
			}
		}
		Collections.sort(positionHreb);
		return positionHreb;
	}

	public TextCore getTextCoreHrebs() {
		return core;
	}

	public double computeTopikalnost(Hreb hreb) {
		final double cardinalNumber = (double) core.getCoreCardinalNumber();

		return denotation.computeTopicality(hreb, cardinalNumber);
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

	public double getDiffusionFor(int hreb) {
		return denotation.getDiffusionFor(hreb);
	}

	public int getHrebSize(int hreb) {
		return denotation.getHreb(hreb).size();
	}

	public List<Hreb> getHrebsInExtendedCore() {
		final Hreb coreSpiceMaxDiffusion = findCoreWithMaxDiffusion();
		if (coreSpiceMaxDiffusion == null) {
			return null;
		}
		final double maxDiffusion = getDiffusionFor(coreSpiceMaxDiffusion.getNumber());

		List<Hreb> hrebs = new ArrayList<Hreb>();
		for (Hreb hreb : denotation.getHrebs()) {
			if (getHrebSize(hreb.getNumber()) > 1 && getDiffusionFor(hreb.getNumber()) <= maxDiffusion) {
				hrebs.add(hreb);
			}
		}

		Collections.sort(hrebs, SPIKE_DIFFUSION_COMPARATOR);

		return hrebs;
	}

	public Hreb findCoreWithMaxDiffusion() {
		final List<Hreb> core = getTextCoreHrebs().getCore();
		if (core.isEmpty()) {
			return null;
		}

		Hreb maxDiffusionHreb = core.get(0);
		double maxDiffusion = getDiffusionFor(maxDiffusionHreb.getNumber());
		for (int i = 1; i < core.size(); i++) {
			final double currentDiffusion = getDiffusionFor(core.get(i).getNumber());
			if (maxDiffusion < currentDiffusion) {
				maxDiffusion = currentDiffusion;
				maxDiffusionHreb = core.get(i);
			}
		}
		return maxDiffusionHreb;
	}

	public List<Pair<Hreb, Double> > getDiffusionForAll() {
		List<Pair<Hreb, Double> > list = new ArrayList<Pair<Hreb, Double>>();
		for (int hreb = 1; hreb <= getHrebsCount(); hreb++) {
			if (getHrebSize(hreb) > 1) {
				Pair<Hreb, Double> pair = new Pair<Hreb, Double>(denotation.getHreb(hreb), getDiffusionFor(hreb));
				list.add(pair);
			}
		}

		Collections.sort(list, new Comparator<Pair<Hreb, Double>>() {
			@Override
			public int compare(Pair<Hreb, Double> o1, Pair<Hreb, Double> o2) {
				return SPIKE_DIFFUSION_COMPARATOR.compare(o1.getValue0(), o2.getValue0());
			}
		});

		return list;
	}

	public PoemAsHrebNumbers getPoemAsHrebNumbers() {
		return denotation.getPoemAsHrebNumbers();
	}

	public List<Coincidence> getCoincidenceFor(int hrebNumber) {
		return denotation.getCoincidenceFor(hrebNumber);
	}

	public List<Coincidence> getDeterministicFor(int hrebNumber) {
		return denotation.getDeterministicFor(hrebNumber);
	}

	public List<Hreb> getAllHrebs() {
		return new ArrayList<Hreb>(denotation.getHrebs());
	}

	public double getNonContinuousIndex() {
		if (nonContinuousIndex == null) {
			nonContinuousIndex = denotation.getNonContinuousIndex();
		}
		return nonContinuousIndex;
	}

	public double getNonIsolationIndex() {
		if (nonIsolationIndex == null) {
			nonIsolationIndex = denotation.getNonIsolationIndex();
		}
		return nonIsolationIndex;
	}

	public double getReachabilityIndex() {
		if (getNonIsolationIndex() == Double.POSITIVE_INFINITY) {
			return Double.POSITIVE_INFINITY;
		}
		if (reachabilityIndex == null) {
			reachabilityIndex = denotation.getReachabilityIndex();
		}
		return reachabilityIndex;
	}

	class TextCore {
		private final List<Hreb> core = new ArrayList<Hreb>();
		private final List<Hreb> outsideCore = new ArrayList<Hreb>();

		private TextCore() {
			for (Hreb hreb : denotation.getHrebs()) {
				if (hreb.getWords().size() >= 2) {
					core.add(hreb);
				} else {
					outsideCore.add(hreb);
				}
			}
		}

		public void add(Hreb hreb) {
			core.add(hreb);
			outsideCore.remove(hreb);
		}

		public void remove(Hreb hreb) {
			core.remove(hreb);
			outsideCore.add(hreb);
		}

		public int size() {
			return core.size();
		}

		public List<Hreb> getCore() {
			Collections.sort(core, SPIKE_NUMBER_COMPARATOR);
			return core;
		}

		public List<Hreb> getNotInCore() {
			Collections.sort(outsideCore, SPIKE_NUMBER_COMPARATOR);
			return outsideCore;
		}

		public boolean isInCore(Hreb hreb) {
			return core.contains(hreb);
		}

		public int getCoreCardinalNumber() {
			int i = 0;
			for (Hreb hreb : core) {
				i += hreb.getWords().size();
			}
			return i;
		}
	}
}
