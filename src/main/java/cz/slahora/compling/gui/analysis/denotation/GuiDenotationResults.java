package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.Spike;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.apache.commons.lang.text.StrBuilder;
import org.javatuples.Pair;
import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

/**
 *
 * TODO
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 11.5.14 12:12</dd>
 * </dl>
 */
public class GuiDenotationResults {

	private final WorkingText text;
	private final GuiDenotationResultsModel model;
	private final JPanel panel;
	public GuiDenotationResults(WorkingText text, IDenotation denotation) {
		this.text = text;
		this.model = new GuiDenotationResultsModel(denotation);
		this.panel = new JPanel(new GridBagLayout());

		panel.setBackground(Color.white);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10)); //..padding

		int y = 0;

		//..top headline
		panel.add(
			new HtmlLabelBuilder().hx(1, "Denotační analýza").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.WEST).build()
		);

		StringBuilder s = new StringBuilder();
		s
			.append("Míra denotační kompaktnosti textu K je ").append(String.format("%.2f",model.getTextCompactness()))
			.append(".<br>Centralizovanost textu R je ").append(String.format("%.3f",model.getTextCentralization()))
			.append(".<br>MacIntoshův index textu je ").append(String.format("%.2f", model.getMacIntosh()));
		panel.add(
			new HtmlLabelBuilder().p(s.toString()).build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		JXCollapsiblePane diffusionSpikes = new JXCollapsiblePane();
		diffusionSpikes.setBackground(Color.WHITE);
		diffusionSpikes.setLayout(new GridLayout(0, 5, 1, 1));
		diffusionSpikes.setCollapsed(true);

		for (int spike = 1; spike <= model.getSpikesCount(); spike++) {
			if (model.getSpikeSize(spike) > 1) {
				JPanel p = new JPanel();
				p.add(new HtmlLabelBuilder().b(String.valueOf(spike)).build());
				p.add(new JLabel(String.format("%.2f", model.getDiffusionFor(spike))));
				diffusionSpikes.add(
					p
				);
			}
		}
		ToggleHeader toggle = new ToggleHeader(diffusionSpikes.getActionMap().get("toggle"),
			new HtmlLabelBuilder().hx(2, "Difuznost hřebů").build().getText());

		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			diffusionSpikes,
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		JXCollapsiblePane positionSpikes = new JXCollapsiblePane();
		positionSpikes.setLayout(new GridBagLayout());
		positionSpikes.setCollapsed(true);
		for (int spike = 1; spike <= model.getSpikesCount(); spike++) {
			positionSpikes.add(new PositionSpikePanel(spike, model.getPositionSpike(spike)),
				new GridBagConstraintBuilder().gridxy(0, spike).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
			);
		}
		toggle = new ToggleHeader(positionSpikes, new HtmlLabelBuilder().hx(2, "Poziční hřeby").build().getText());

		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			positionSpikes,
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		panel.add(
			new HtmlLabelBuilder().hx(2, "Jádro textu").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		final GuiDenotationResultsModel.TextCore core = model.getTextCoreSpikes();
		JLabel spikeLabel = new JLabel();
		panel.add(
			spikeLabel,
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		JPanel textCorePanel = new JPanel(new GridBagLayout());
		textCorePanel.setBackground(Color.white);
		CoreSpikePanel coreSpikePanel = new CoreSpikePanel(core, spikeLabel);
		panel.add(
			coreSpikePanel,
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);


		ExtendedCorePanel extendedCore = new ExtendedCorePanel(model.findCoreWithMaxDiffusion(), model.getSpikesInExtendedCore());
		coreSpikePanel.addObserver(extendedCore);
		toggle = new ToggleHeader(extendedCore, "Rozšířené jádro textu");
		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			extendedCore,
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);
		//..last panel for align another components to the top
		JPanel dummyPanel = new JPanel();
		dummyPanel.setBackground(Color.WHITE);
		panel.add(
			dummyPanel,
			new GridBagConstraintBuilder().gridxy(0, y).fill(GridBagConstraints.BOTH).weightx(1).weighty(1).anchor(GridBagConstraints.NORTH).build()
		);
	}

	public JPanel getPanel() {
		return panel;
	}

	private static class ToggleHeader extends JButton {

		private ToggleHeader(JXCollapsiblePane panel, String text) {
			this(panel.getActionMap().get("toggle"), text);
		}

		private ToggleHeader(Action action, String text) {
			setHideActionText(true);
			setFocusPainted(false);
			setMargin(new Insets(0, 0, 0, 0));
			setContentAreaFilled(false);
			setBorderPainted(false);
			setOpaque(false);
			setAction(action);
			setText(text);
		}
	}

	private class PositionSpikePanel extends JPanel {
		public PositionSpikePanel(int spikeNumber, List<Integer> wordNumbers) {
			super(new GridBagLayout());

			add(
				new HtmlLabelBuilder().hx(3, "Poziční hřeb č. " + spikeNumber).build(),
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).gridxy(0, 0).insets(new Insets(0, 0, 0, 25)).build()
			);
			StrBuilder builder = new StrBuilder();
			builder.append("<html>").appendWithSeparators(wordNumbers, ", ").append("</html>");
			add(
				new JLabel(builder.toString()),
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).gridxy(1, 0).weightx(1).build()
			);

			setBorder(new EmptyBorder(0, 0, 5, 0));
		}
	}

	private class CoreSpikePanel extends JPanel implements SpikeDetailsCollapsiblePanelMouseListener {
		private final GuiDenotationResultsModel.TextCore core;
		final JLabel spikeLabel;
		private SpikeDetailsCollapsiblePanel outsideCorePanel;
		private SpikeDetailsCollapsiblePanel corePanel;
		private SpikeDetailsCollapsiblePanel coreTopikalnostPanel;
		private final Observable observable;

		public CoreSpikePanel(GuiDenotationResultsModel.TextCore core, JLabel spikeLabel) {
			super(new GridBagLayout());

			this.core = core;
			this.spikeLabel = spikeLabel;

			setBackground(Color.white);

			observable = new Observable() {
				@Override
				public void notifyObservers(Object arg) {
					setChanged();
					super.notifyObservers(arg);

				}
			};


			refresh();
		}

		private void refresh() {
			boolean collapseCore = corePanel != null && corePanel.isCollapsed();
			boolean collapseOutsideCore = outsideCorePanel == null || outsideCorePanel.isCollapsed();
			boolean collapseTopikalnostCore = coreTopikalnostPanel == null || coreTopikalnostPanel.isCollapsed();
			removeAll();

			//..panel hřebů, které patří do jádra
			corePanel = new SpikeDetailsCollapsiblePanel();
			corePanel.setCollapsed(collapseCore);
			corePanel.addSpikes(core.getCore(), this);
			Action action = corePanel.getActionMap().get("toggle");
			ToggleHeader toggleHeader = new ToggleHeader(action, new HtmlLabelBuilder().hx(3, "Jádro textu").build().getText());
			add(
				toggleHeader,
				new GridBagConstraintBuilder().weighty(1).gridxy(0, 0).anchor(GridBagConstraints.NORTHWEST).build()
			);

			add(
				corePanel,
				new GridBagConstraintBuilder().anchor(GridBagConstraints.NORTHWEST).fill(GridBagConstraints.HORIZONTAL).weightx(1).gridxy(1, 0).build()
			);

			//..panel hřebů, které jsou mimo jádro
			outsideCorePanel = new SpikeDetailsCollapsiblePanel();
			outsideCorePanel.setCollapsed(collapseOutsideCore);
			outsideCorePanel.setLayout(new GridBagLayout());
			outsideCorePanel.addSpikes(core.getNotInCore(), this);

			action = outsideCorePanel.getActionMap().get("toggle");
			toggleHeader = new ToggleHeader(action, new HtmlLabelBuilder().hx(3, "Hřeby mimo jádro").build().getText());
			add(
				toggleHeader,
				new GridBagConstraintBuilder().weighty(1).weightx(1).gridxy(0, 1).anchor(GridBagConstraints.NORTHWEST).build()
			);
			add(
				outsideCorePanel,
				new GridBagConstraintBuilder().anchor(GridBagConstraints.NORTHWEST).gridxy(1, 1).build()
			);

			//...topikalnosť
			coreTopikalnostPanel = new SpikeDetailsCollapsiblePanel();
			coreTopikalnostPanel.setLayout(new GridBagLayout());
			coreTopikalnostPanel.setCollapsed(collapseTopikalnostCore);
			coreTopikalnostPanel.addSpikes(computeTopikalnost(), null);

			action = coreTopikalnostPanel.getActionMap().get("toggle");
			toggleHeader = new ToggleHeader(action, new HtmlLabelBuilder().hx(3, "Topikálnost jádrových hřebů").build().getText());
			add(
				toggleHeader,
				new GridBagConstraintBuilder().weighty(1).weightx(1).gridxy(0, 2).anchor(GridBagConstraints.NORTHWEST).build()
			);
			add(
				coreTopikalnostPanel,
				new GridBagConstraintBuilder().anchor(GridBagConstraints.NORTHWEST).gridxy(1, 2).build()
			);

			String spikeLbl = core.size() == 1 ? "hřeb" : core.size() < 5 && core.size() > 0 ? "hřeby" : "hřebů";
			spikeLabel.setText(String.format("Jádro textu obsahuje %d %s. Kardinální číslo jádra je %d.", core.size(), spikeLbl, core.getCoreCardinalNumber()));
		}

		private Map<Spike, String> computeTopikalnost() {
			Map<Spike, String> map = new LinkedHashMap<Spike, String>(core.getCore().size());
			for (Spike spike : core.getCore()) {
				double topikalnost = model.computeTopikalnost(spike);
				map.put(spike, String.format("%.2f", topikalnost));
			}
			return map;
		}

		private void addObserver(Observer observer) {
			observable.addObserver(observer);
		}

		private void removeObserver(Observer observer) {
			observable.deleteObserver(observer);
		}

		private void notifyObservers() {
			observable.notifyObservers(new Pair<Spike, List<Spike>>(model.findCoreWithMaxDiffusion(), model.getSpikesInExtendedCore()));
		}

		@Override
		public MouseListener getListenerFor(final Spike spike) {
			return new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (core.isInCore(spike)) {
						core.remove(spike);
					} else {
						core.add(spike);
					}
					notifyObservers();
					refresh();
					validate();
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					panel.setBackground(new Color(193, 193, 193));
				}

				@Override
				public void mouseExited(MouseEvent e) {
					panel.setBackground(Color.white);
				}
			};
		}
	}



	private static class ExtendedCorePanel extends JXCollapsiblePane implements Observer {

		public ExtendedCorePanel(Spike maxDiffusionSpike, List<Spike> spikesInExtendedCore) {
			setCollapsed(true);
			addDescriptions(maxDiffusionSpike);
			fill(spikesInExtendedCore);
		}

		private void addDescriptions(Spike maxDiffusionSpike) {
			if (maxDiffusionSpike != null) {
				add(new HtmlLabelBuilder()
					.p("Jádrový hřeb s nejvyšší difuzností je hřeb číslo %d", maxDiffusionSpike.getNumber())
					.setToolTipText(maxDiffusionSpike.getWords().toString())
					.build());
			}
		}

		private void fill(List<Spike> spikesInExtendedCore) {
			if (spikesInExtendedCore == null) {
				return;
			}
			final int size = spikesInExtendedCore.size();
			for (int i = 0; i < size; i++) {
				Spike s = spikesInExtendedCore.get(i);
				HtmlLabelBuilder labelBuilder = new HtmlLabelBuilder().normal(String.valueOf(s.getNumber()));
				if (i < size - 1) {
					labelBuilder.normal(", ");
				}
				JPanel spikePanel = new JPanel();
				spikePanel.add(labelBuilder.build());
				spikePanel.setToolTipText(s.getWords().toString());
				add(spikePanel);
			}

		}

		@Override
		public void update(Observable o, Object arg) {
			removeAll();
			Pair<Spike, List<Spike>> p =(Pair<Spike, List<Spike>>)arg;
			addDescriptions(p.getValue0());
			fill(p.getValue1());

		}
	}

	private static class SpikeDetailsCollapsiblePanel extends JXCollapsiblePane {

		private static final Insets INSETS = new Insets(0, 0, 0, 25);

		public void addSpikes(List<Spike> spikes, SpikeDetailsCollapsiblePanelMouseListener spikePanelListener) {
			int y = 0;
			for (Spike spike : spikes) {
				JPanel spikePanel = createSpikePanel(spike, spikePanelListener);
				spikePanel.setBackground(Color.white);

				JLabel spikeNumber = new HtmlLabelBuilder().hx(3, String.valueOf(spike.getNumber())).build();
				String content = new StrBuilder().append("<html>").appendWithSeparators(spike.getWords(), ", ").append("</html>").toString();
				JLabel spikeContent = new JLabel(content);

				spikePanel.add(
					spikeNumber,
					new GridBagConstraintBuilder().fill(GridBagConstraints.VERTICAL).insets(INSETS).weighty(1).gridxy(0, 0).build()
				);

				spikePanel.add(
					spikeContent,
					new GridBagConstraintBuilder().fill(GridBagConstraints.BOTH).weightx(1).weighty(1).gridxy(1, 0).build()
				);

				add(
					spikePanel,
					new GridBagConstraintBuilder().weightx(1).fill(GridBagConstraints.HORIZONTAL).gridxy(0, y++).build()
				);
			}
		}

		public void addSpikes(Map<Spike, String> spikes, SpikeDetailsCollapsiblePanelMouseListener spikePanelListener) {
			int y = 0;
			for (Map.Entry<Spike, String> entry : spikes.entrySet()) {
				JPanel spikePanel = createSpikePanel(entry.getKey(), spikePanelListener);
				spikePanel.setBackground(Color.white);

				JLabel spikeNumber = new HtmlLabelBuilder().hx(3, String.valueOf(entry.getKey().getNumber())).build();
				String content = new StrBuilder().append("<html>").append(entry.getValue()).append("</html>").toString();
				JLabel spikeContent = new JLabel(content);

				spikePanel.add(
					spikeNumber,
					new GridBagConstraintBuilder().fill(GridBagConstraints.VERTICAL).insets(INSETS).weighty(1).gridxy(0, 0).build()
				);

				spikePanel.add(
					spikeContent,
					new GridBagConstraintBuilder().fill(GridBagConstraints.BOTH).weightx(1).weighty(1).gridxy(1, 0).build()
				);

				add(
					spikePanel,
					new GridBagConstraintBuilder().weightx(1).fill(GridBagConstraints.HORIZONTAL).gridxy(0, y++).build()
				);
			}
		}

		private JPanel createSpikePanel(final Spike spike, SpikeDetailsCollapsiblePanelMouseListener spikePanelListener) {
			final JPanel panel = new JPanel(new GridBagLayout());
			if (spikePanelListener != null) {
				panel.addMouseListener(spikePanelListener.getListenerFor(spike));
			}
			return panel;
		}
	}

	private static interface SpikeDetailsCollapsiblePanelMouseListener  {

		MouseListener getListenerFor(Spike spike);

	}
}
