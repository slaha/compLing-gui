package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.Coincidence;
import cz.compling.model.denotation.GuiPoemAsSpikeNumbers;
import cz.compling.model.denotation.Spike;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.apache.commons.lang.text.StrBuilder;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.javatuples.Pair;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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
		diffusionSpikes.setLayout(new GridBagLayout());
		diffusionSpikes.setCollapsed(true);

		final List<Pair<Spike, Double>> diffusions = model.getDiffusionForAll();
		final int rowsPerColumn = diffusions.size() / 5;
		final int biggerColumns = diffusions.size() % 5;

		int row = 0;
		int column = 0;
		for (Pair<Spike, Double> pair : diffusions) {
			JPanel p = new JPanel();
			p.add(new HtmlLabelBuilder().b(String.valueOf(pair.getValue0().getNumber())).build());
			p.add(new JLabel(String.format("%.2f", pair.getValue1())));

			diffusionSpikes.add(p,
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).weightx(1).gridxy(column, row).build()
			);
			row++;
			if (column < biggerColumns) {
				if (row > rowsPerColumn) {
					row = 0;
					column++;
				}
			} else {
				if (row == rowsPerColumn) {
					row = 0;
					column++;
				}
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
		toggle = new ToggleHeader(extendedCore, new HtmlLabelBuilder().hx(2, "Rozšířené jádro textu").build().getText());
		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			extendedCore,
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		CoincidencePanel poemAsSpikeNumbersPanel = new CoincidencePanel(model.getPoemAsSpikeNumbers());
		poemAsSpikeNumbersPanel.setBackground(Color.WHITE);
		toggle = new ToggleHeader(poemAsSpikeNumbersPanel, new HtmlLabelBuilder().hx(2, "Koincidence").build().getText());
		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridxy(0, y++).weightx(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			poemAsSpikeNumbersPanel,
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);
		final List<Coincidence> coincidenceFor14 = model.getCoincidenceFor(14);
		panel.add(
			new CoincidenceDetailPanel(coincidenceFor14),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		panel.add(
			new HtmlLabelBuilder().hx(2, "Graf").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);


		final Graph graph = createGraph(model.getAllSpikes(), createCoincidenceMap(model.getAllSpikes()));
		Viewer v = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_SWING_THREAD);
		v.enableAutoLayout();
		View view = v.addDefaultView(false);
		final JPanel graphPanel = new JPanel();
		graphPanel.setPreferredSize(new Dimension(1600, 900));
		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(view, BorderLayout.CENTER);

		panel.add(
			graphPanel,
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.BOTH).weightx(1).weighty(1).anchor(GridBagConstraints.NORTH).build()
		);


//		JFrame f = new JFrame("Graf");
//		f.setContentPane(graphPanel);
//		f.setVisible(true);
		//..last panel for align another components to the top
		JPanel dummyPanel = new JPanel();
		dummyPanel.setBackground(Color.WHITE);
		panel.add(
			dummyPanel,
			new GridBagConstraintBuilder().gridxy(0, y).fill(GridBagConstraints.BOTH).weightx(1).weighty(1).anchor(GridBagConstraints.NORTH).build()
		);


	}

	private Map<Spike, List<Coincidence>> createCoincidenceMap(List<Spike> allSpikes) {
		Map<Spike, List<Coincidence>> map = new HashMap<Spike, List<Coincidence>>();

		for (Spike spike : allSpikes) {
			map.put(spike, model.getCoincidenceFor(spike.getNumber()));
		}

		return map;
	}

	private Graph createGraph(List<Spike> allSpikes, Map<Spike, List<Coincidence>> coincidenceFor) {
		Graph graph = new MultiGraph("Koincidence pro hřeb č. 14");

		final double alpha = 0.1d;

		for (Spike spike : allSpikes) {
			final String s = String.valueOf(spike.getNumber());
			Node node = graph.addNode(s);
			node.addAttribute("ui.label", s);
		}

		for (Map.Entry<Spike, List<Coincidence>> entry : coincidenceFor.entrySet()) {
			for (Coincidence coincidence : entry.getValue()) {
				final String one = String.valueOf(entry.getKey().getNumber());
				if (coincidence.probability <= alpha) {
					final String another = String.valueOf(coincidence.anotherSpike.getNumber());
					graph.addEdge(one+another, one, another);
				}
			}
		}
		return graph;
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
		public MouseListener getListenerFor(final Spike spike, final JPanel panel) {
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
			setBackground(Color.white);
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

			SpikeDetailsCollapsiblePanel extendedCore = new SpikeDetailsCollapsiblePanel();
			ToggleHeader toggleHeader = new ToggleHeader(extendedCore, new HtmlLabelBuilder().hx(3, "Rozšířené jádro textu").build().getText());

			extendedCore.addSpikes(spikesInExtendedCore, null);

			JPanel coverPanel = new JPanel(new GridBagLayout());
			coverPanel.setBackground(Color.white);
			coverPanel.add(
				toggleHeader,
				new GridBagConstraintBuilder().weighty(1).weightx(1).gridxy(0, 0).anchor(GridBagConstraints.NORTHWEST).build()
			);
			coverPanel.add(
				extendedCore,
				new GridBagConstraintBuilder().anchor(GridBagConstraints.NORTHWEST).gridxy(1, 0).build()
			);

			add(coverPanel,
				new GridBagConstraintBuilder().gridxy(0, 1).weighty(1).weightx(1).anchor(GridBagConstraints.NORTHWEST).build()
			);
		}

		@Override
		public void update(Observable o, Object arg) {
			removeAll();
			Pair<Spike, List<Spike>> p =(Pair<Spike, List<Spike>>)arg;
			addDescriptions(p.getValue0());
			fill(p.getValue1());

		}
	}

	private static class CoincidencePanel extends JXCollapsiblePane {

		private final JPanel poemPanel;

		public CoincidencePanel(GuiPoemAsSpikeNumbers poemAsSpikeNumbers) {
			this.poemPanel = new JPanel();
			poemPanel.setLayout(new BoxLayout(poemPanel, BoxLayout.Y_AXIS));
			poemPanel.setBackground(Color.WHITE);
			setLayout(new GridBagLayout());

			fill(poemAsSpikeNumbers);

			add(poemPanel);
		}

		private void fill(GuiPoemAsSpikeNumbers poem) {
			int lastStrophe = 1;

			for (GuiPoemAsSpikeNumbers.Strophe strophe : poem.getStrophes()) {
				if (lastStrophe != strophe.getNumber()) {
					JPanel betweenStrophes = new JPanel() {
						@Override
						public Dimension getPreferredSize() {
							return new Dimension(10, 25);
						}
					};
					poemPanel.add(betweenStrophes);
					lastStrophe = strophe.getNumber();
				}
				for (GuiPoemAsSpikeNumbers.Strophe.Verse verse : strophe.getVerses()) {
					String verseString = new StrBuilder().appendWithSeparators(verse.getSpikes(), ", ").toString();
					JLabel lbl = new JLabel(verseString);
					poemPanel.add(lbl);
				}
			}
		}
	}

	private static class CoincidenceDetailPanel extends JPanel {
		JXTable table;
		DefaultTableModel model;
		private CoincidenceDetailPanel(final List<Coincidence> coincidences) {
			super(new BorderLayout());
			setBackground(Color.white);
			final String header[] = {"Koincidence s hřebem č.", "Počet koincidencí","Pravěpodobnost"};
			model = new DefaultTableModel(header, coincidences.size()) {

				@Override
				public int getColumnCount() {
					return 3;
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					final Coincidence coincidence = coincidences.get(rowIndex);
					switch (columnIndex) {
						case 0:
							return coincidence.anotherSpike;
						case 1:
							return coincidence.coincidenceCount;
						case 2:
							return String.format("%.4f", coincidence.probability);
					}
					return null;
				}

			};
			table = new JXTable(model);
			table.setSortable(false);
			add(table.getTableHeader(), BorderLayout.NORTH);
			add(table, BorderLayout.CENTER);
		}
	}
	private static class SpikeDetailsCollapsiblePanel extends JXCollapsiblePane {

		private static final Insets INSETS = new Insets(0, 0, 0, 25);

		private SpikeDetailsCollapsiblePanel() {
			setBackground(Color.white);
		}

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
				panel.addMouseListener(spikePanelListener.getListenerFor(spike, panel));
			}
			return panel;
		}
	}

	private static interface SpikeDetailsCollapsiblePanelMouseListener  {

		MouseListener getListenerFor(final Spike spike, final JPanel panel);

	}
}
