package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.Coincidence;
import cz.compling.model.denotation.DenotationMath;
import cz.compling.model.denotation.Hreb;
import cz.compling.model.denotation.PoemAsHrebNumbers;
import cz.slahora.compling.gui.analysis.ToggleHeader;
import cz.slahora.compling.gui.analysis.assonance.NonEditableTable;
import cz.slahora.compling.gui.model.LastDirectory;
import cz.slahora.compling.gui.settings.SettingsManager;
import cz.slahora.compling.gui.ui.MultipleLinesLabel;
import cz.slahora.compling.gui.utils.*;
import org.apache.commons.lang.text.StrBuilder;
import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerListener;
import org.graphstream.ui.swingViewer.ViewerPipe;
import org.javatuples.Pair;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXTable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;

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

	private final GuiDenotationResultsModel model;
	private final JPanel panel;
	private Viewer graphViewer;
	private GraphType graphType;


	public GuiDenotationResults(IDenotation denotation) {
		this.model = new GuiDenotationResultsModel(denotation);
		this.panel = new JPanel(new GridBagLayout());

		panel.setBackground(Color.white);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10)); //..padding

		int y = 0;

		//..top headline
		panel.add(
			new HtmlLabelBuilder().hx(1, "Denotační analýza").build(),
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.WEST).build()
		);

		StringBuilder s = new StringBuilder();
		s
			.append("Míra denotační kompaktnosti textu K je ").append(String.format("%.2f",model.getTextCompactness()))
			.append(".<br>Centralizovanost textu R je ").append(String.format("%.3f",model.getTextCentralization()))
			.append(".<br>MacIntoshův index textu je ").append(String.format("%.2f", model.getMacIntosh()));
		panel.add(
			new HtmlLabelBuilder().p(s.toString()).build(),
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.NORTH).build()
		);

		JXCollapsiblePane diffusionHrebs = new JXCollapsiblePane();
		diffusionHrebs.setBackground(Color.WHITE);
		diffusionHrebs.setLayout(new GridBagLayout());
		diffusionHrebs.setCollapsed(true);

		final List<Pair<Hreb, Double>> diffusions = model.getDiffusionForAll();
		final int rowsPerColumn = diffusions.size() / 5;
		final int biggerColumns = diffusions.size() % 5;

		int row = 0;
		int column = 0;
		for (Pair<Hreb, Double> pair : diffusions) {
			JPanel p = new JPanel();
			p.add(new HtmlLabelBuilder().b(String.valueOf(pair.getValue0().getNumber())).build());
			p.add(new JLabel(String.format("%.2f", pair.getValue1())));

			diffusionHrebs.add(p,
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).weightX(1).gridXY(column, row).build()
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

		ToggleHeader toggle = new ToggleHeader(diffusionHrebs,
			new HtmlLabelBuilder().hx(2, "Difuznost hřebů").build().getText());

		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			diffusionHrebs,
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.NORTH).build()
		);

		JXCollapsiblePane positionHrebs = new JXCollapsiblePane();
		positionHrebs.setLayout(new GridBagLayout());
		positionHrebs.setCollapsed(true);
		for (int hreb = 1; hreb <= model.getHrebsCount(); hreb++) {
			positionHrebs.add(new PositionHrebPanel(hreb, model.getPositionHreb(hreb)),
				new GridBagConstraintBuilder().gridXY(0, hreb).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.NORTH).build()
			);
		}
		toggle = new ToggleHeader(positionHrebs, new HtmlLabelBuilder().hx(2, "Poziční hřeby").build().getText());

		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			positionHrebs,
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.NORTH).build()
		);

		panel.add(
			new HtmlLabelBuilder().hx(2, "Jádro textu").build(),
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.NORTH).build()
		);

		final GuiDenotationResultsModel.TextCore core = model.getTextCoreHrebs();
		JLabel hrebLabel = new JLabel();
		panel.add(
			hrebLabel,
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.NORTH).build()
		);

		JPanel textCorePanel = new JPanel(new GridBagLayout());
		textCorePanel.setBackground(Color.white);
		CoreHrebPanel coreHrebPanel = new CoreHrebPanel(core, hrebLabel);
		panel.add(
			coreHrebPanel,
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.NORTH).build()
		);


		ExtendedCorePanel extendedCore = new ExtendedCorePanel(model.findCoreWithMaxDiffusion(), model.getHrebsInExtendedCore());
		coreHrebPanel.addObserver(extendedCore);
		toggle = new ToggleHeader(extendedCore, new HtmlLabelBuilder().hx(2, "Rozšířené jádro textu").build().getText());
		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			extendedCore,
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.NORTH).build()
		);

		JXCollapsiblePane coincidencePanel = new JXCollapsiblePane(new BorderLayout());
		coincidencePanel.setBackground(Color.white);
		coincidencePanel.setCollapsed(true);


		CoincidencePanel poemAsHrebNumbersPanel = new CoincidencePanel(model.getPoemAsHrebNumbers());
		poemAsHrebNumbersPanel.setBackground(Color.WHITE);
		toggle = new ToggleHeader(poemAsHrebNumbersPanel, new HtmlLabelBuilder().hx(3, "Báseň jako pořadová čísla hřebů").build().getText());
		JPanel poemAsHrebNumbersPanelWrapper = new JPanel(new BorderLayout());
		poemAsHrebNumbersPanelWrapper.setBackground(Color.white);

		poemAsHrebNumbersPanelWrapper.add(toggle, BorderLayout.NORTH);
		poemAsHrebNumbersPanelWrapper.add(poemAsHrebNumbersPanel, BorderLayout.SOUTH);

		coincidencePanel.add(poemAsHrebNumbersPanelWrapper, BorderLayout.NORTH);

		final JPanel coincidenceTableParentPanel = new JPanel();
		coincidenceTableParentPanel.setBackground(Color.white);

		SpinnerModel coincidenceSpinnerModel = new SpinnerNumberModel(1, 1, model.getHrebsCount(), 1);
		final JSpinner coincidenceSpinner = new JSpinner(coincidenceSpinnerModel);
		final ChangeListener listener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Number number = (Number) coincidenceSpinner.getValue();

				coincidenceTableParentPanel.removeAll();
				List<Coincidence> coincidenceFor = model.getCoincidenceFor(number.intValue());
				coincidenceTableParentPanel.add(new CoincidenceDetailPanel(coincidenceFor));
				coincidenceTableParentPanel.validate();
			}
		};
		coincidenceSpinner.addChangeListener(listener);
		listener.stateChanged(new ChangeEvent(coincidenceSpinner));

		JPanel coincidenceSpinnerPanel = new JPanel();
		coincidenceSpinnerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		coincidenceSpinnerPanel.add(new JLabel("Koincidence pro hřeb č.:"));
		coincidenceSpinnerPanel.add(coincidenceSpinner);

		JXCollapsiblePane coincidenceTablePanel = new JXCollapsiblePane(new BorderLayout());
		coincidenceTablePanel.setBackground(Color.white);
		coincidenceTablePanel.setCollapsed(true);
		coincidenceTablePanel.add(coincidenceSpinnerPanel, BorderLayout.NORTH);
		coincidenceTablePanel.add(coincidenceTableParentPanel, BorderLayout.CENTER);

		toggle = new ToggleHeader(coincidenceTablePanel, new HtmlLabelBuilder().hx(3, "Tabulka koincidence").build().getText());
		JPanel coincidenceTablePanelWrapper = new JPanel(new BorderLayout());
		coincidenceTablePanelWrapper.setBackground(Color.white);

		coincidenceTablePanelWrapper.add(toggle, BorderLayout.NORTH);
		coincidenceTablePanelWrapper.add(coincidenceTablePanel, BorderLayout.SOUTH);
		coincidencePanel.add(coincidenceTablePanelWrapper, BorderLayout.SOUTH);
		toggle = new ToggleHeader(coincidencePanel, new HtmlLabelBuilder().hx(2, "Koincidence").build().getText());

		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			coincidencePanel,
			new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).anchor(GridBagConstraints.WEST).build()
		);
		panel.add(
			new HtmlLabelBuilder().hx(2, "Graf").build(),
			new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1).build()
		);

		final JPanel graphPanel = new GraphPanel();

		panel.add(
			graphPanel,
			new GridBagConstraintBuilder().gridXY(0, y++).fill(HORIZONTAL).weightX(1).build()
		);

		//..last panel for align another components to the top
		JPanel dummyPanel = new JPanel();
		dummyPanel.setBackground(Color.WHITE);
		panel.add(
			dummyPanel,
			new GridBagConstraintBuilder().gridXY(0, y).fill(BOTH).weightX(1).weightY(1).anchor(GridBagConstraints.NORTH).build()
		);


	}

	private Map<Hreb, List<Coincidence>> createCoincidenceMap(List<Hreb> allHrebs) {
		Map<Hreb, List<Coincidence>> map = new HashMap<Hreb, List<Coincidence>>();

		for (Hreb hreb : allHrebs) {
			map.put(hreb, model.getCoincidenceFor(hreb.getNumber()));
		}

		return map;
	}
	private Map<Hreb, List<Coincidence>> createDeterministicMap(List<Hreb> allHrebs) {
		Map<Hreb, List<Coincidence>> map = new HashMap<Hreb, List<Coincidence>>();

		for (Hreb hreb : allHrebs) {
			map.put(hreb, model.getDeterministicFor(hreb.getNumber()));
		}

		return map;
	}

	private Graph createDeterministicGraph(List<Hreb> allHrebs, Map<Hreb, List<Coincidence>> coincidenceFor, final double alpha) {
		Graph graph = new DefaultGraph("Deterministicko-pravděpodobnostní graf pro hladinu významnosti α=" + alpha);
		return fillGraph(graph, allHrebs, coincidenceFor, alpha);
	}

	private Graph createCoincidenceGraph(List<Hreb> allHrebs, Map<Hreb, List<Coincidence>> coincidenceFor, final double alpha) {
		Graph graph = new DefaultGraph("Graf koincidence na hladině významnosti α=" + alpha);
		return fillGraph(graph, allHrebs, coincidenceFor, alpha);
	}

	private Graph fillGraph(Graph graph, List<Hreb> allHrebs, Map<Hreb, List<Coincidence>> coincidenceFor, final double alpha) {
		graph.addAttribute("ui.stylesheet", GraphStyle.getStyleSheet());
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");

		for (Hreb hreb : allHrebs) {
			final String s = String.valueOf(hreb.getNumber());
			Node node = graph.addNode(s);
			node.addAttribute("ui.label", s);
		}

		double graphNodeMultiplier = Math.max(1d, SettingsManager.getInstance().getGraphNodeSizeMultiplier());

		for (Map.Entry<Hreb, List<Coincidence>> entry : coincidenceFor.entrySet()) {
			final String one = String.valueOf(entry.getKey().getNumber());
			for (Coincidence coincidence : entry.getValue()) {
				if (coincidence.probability <= alpha) {
					final String another = String.valueOf(coincidence.anotherHreb.getNumber());
					final String id = one + '_' + another;
					final String id2 = another + '_' + one;
					if (graph.getEdge(id) == null && graph.getEdge(id2) == null) {
						final Edge edge = graph.addEdge(id, one, another);
						edge.setAttribute("layout.weight", 4d * graphNodeMultiplier);
					}
				}
			}
		}

		return graph;
	}

	public JPanel getPanel() {
		return panel;
	}

	public GuiDenotationResultsModel getModel() {
		return model;
	}

	private class PositionHrebPanel extends JPanel {
		public PositionHrebPanel(int hrebNumber, List<Integer> wordNumbers) {
			super(new GridBagLayout());

			add(
				new HtmlLabelBuilder().hx(3, "Poziční hřeb č. " + hrebNumber).build(),
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).gridXY(0, 0).insets(new Insets(0, 0, 0, 25)).build()
			);
			StrBuilder builder = new StrBuilder();
			builder.append("<html>").appendWithSeparators(wordNumbers, ", ").append("</html>");
			add(
				new JLabel(builder.toString()),
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).gridXY(1, 0).weightX(1).build()
			);

			setBorder(new EmptyBorder(0, 0, 5, 0));
		}
	}

	private class CoreHrebPanel extends JPanel implements HrebDetailsCollapsiblePanelMouseListener {
		private final GuiDenotationResultsModel.TextCore core;
		final JLabel hrebLabel;
		private HrebDetailsCollapsiblePanel outsideCorePanel;
		private HrebDetailsCollapsiblePanel corePanel;
		private HrebDetailsCollapsiblePanel coreTopikalnostPanel;
		private final Observable observable;

		public CoreHrebPanel(GuiDenotationResultsModel.TextCore core, JLabel hrebLabel) {
			super(new GridBagLayout());

			this.core = core;
			this.hrebLabel = hrebLabel;

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
			corePanel = new HrebDetailsCollapsiblePanel();
			corePanel.setCollapsed(collapseCore);
			corePanel.addHrebs(core.getCore(), this);
			ToggleHeader toggleHeader = new ToggleHeader(corePanel, new HtmlLabelBuilder().hx(3, "Jádro textu").build().getText());
			add(
				toggleHeader,
				new GridBagConstraintBuilder().weightY(1).gridXY(0, 0).anchor(GridBagConstraints.NORTHWEST).build()
			);

			add(
				corePanel,
				new GridBagConstraintBuilder().anchor(GridBagConstraints.NORTHWEST).fill(GridBagConstraints.HORIZONTAL).weightX(1).gridXY(1, 0).build()
			);

			//..panel hřebů, které jsou mimo jádro
			outsideCorePanel = new HrebDetailsCollapsiblePanel();
			outsideCorePanel.setCollapsed(collapseOutsideCore);
			outsideCorePanel.setLayout(new GridBagLayout());
			outsideCorePanel.addHrebs(core.getNotInCore(), this);

			toggleHeader = new ToggleHeader(outsideCorePanel, new HtmlLabelBuilder().hx(3, "Hřeby mimo jádro").build().getText());
			add(
				toggleHeader,
				new GridBagConstraintBuilder().weightY(1).weightX(1).gridXY(0, 1).anchor(GridBagConstraints.NORTHWEST).build()
			);
			add(
				outsideCorePanel,
				new GridBagConstraintBuilder().anchor(GridBagConstraints.NORTHWEST).gridXY(1, 1).build()
			);

			//...topikalnosť
			coreTopikalnostPanel = new HrebDetailsCollapsiblePanel();
			coreTopikalnostPanel.setCollapsed(collapseTopikalnostCore);
			coreTopikalnostPanel.setLayout(new GridBagLayout());
			coreTopikalnostPanel.addHrebs(computeTopikalnost(), null);

			toggleHeader = new ToggleHeader(coreTopikalnostPanel, new HtmlLabelBuilder().hx(3, "Tematičnost jádrových hřebů").build().getText());
			add(
				toggleHeader,
				new GridBagConstraintBuilder().weightY(1).weightX(1).gridXY(0, 2).anchor(GridBagConstraints.NORTHWEST).build()
			);
			add(
				coreTopikalnostPanel,
				new GridBagConstraintBuilder().anchor(GridBagConstraints.NORTHWEST).gridXY(1, 2).build()
			);


			String hrebLbl = core.size() == 1 ? "hřeb" : core.size() < 5 && core.size() > 0 ? "hřeby" : "hřebů";
			hrebLabel.setText(String.format("Jádro textu obsahuje %d %s. Kardinální číslo jádra je %d.", core.size(), hrebLbl, core.getCoreCardinalNumber()));
		}

		private Map<Hreb, String> computeTopikalnost() {
			Map<Hreb, String> map = new LinkedHashMap<Hreb, String>(core.getCore().size());
			for (Hreb hreb : core.getCore()) {
				double topikalnost = model.computeTopikalnost(hreb);
				map.put(hreb, String.format("%.2f", topikalnost));
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
			observable.notifyObservers(new Pair<Hreb, List<Hreb>>(model.findCoreWithMaxDiffusion(), model.getHrebsInExtendedCore()));
		}

		@Override
		public MouseListener getListenerFor(final Hreb hreb, final JPanel panel) {
			return new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (core.isInCore(hreb)) {
						core.remove(hreb);
					} else {
						core.add(hreb);
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

		public ExtendedCorePanel(Hreb maxDiffusionHreb, List<Hreb> hrebsInExtendedCore) {
			setBackground(Color.white);
			setCollapsed(true);
			addDescriptions(maxDiffusionHreb);
			fill(hrebsInExtendedCore);
		}

		private void addDescriptions(Hreb maxDiffusionHreb) {
			if (maxDiffusionHreb != null) {
				add(new HtmlLabelBuilder()
						.p("Jádrový hřeb s nejvyšší difuzností je hřeb číslo %d", maxDiffusionHreb.getNumber())
						.setToolTipText(maxDiffusionHreb.getWords().toString())
						.build());
			}
		}

		private void fill(List<Hreb> hrebsInExtendedCore) {
			if (hrebsInExtendedCore == null) {
				return;
			}

			HrebDetailsCollapsiblePanel extendedCore = new HrebDetailsCollapsiblePanel();
			ToggleHeader toggleHeader = new ToggleHeader(extendedCore, new HtmlLabelBuilder().hx(3, "Rozšířené jádro textu").build().getText());

			extendedCore.addHrebs(hrebsInExtendedCore, null);

			JPanel coverPanel = new JPanel(new GridBagLayout());
			coverPanel.setBackground(Color.white);
			coverPanel.add(
				toggleHeader,
				new GridBagConstraintBuilder().weightY(1).gridXY(0, 0).anchor(GridBagConstraints.NORTHWEST).build()
			);
			coverPanel.add(
				extendedCore,
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).weightX(1).anchor(GridBagConstraints.NORTHWEST).gridXY(1, 0).build()
			);

			add(coverPanel,
				new GridBagConstraintBuilder().gridXY(0, 1).weightY(1).weightX(1).anchor(GridBagConstraints.NORTHWEST).build()
			);
		}

		@Override
		public void update(Observable o, Object arg) {
			removeAll();
			Pair<Hreb, List<Hreb>> p =(Pair<Hreb, List<Hreb>>)arg;
			addDescriptions(p.getValue0());
			fill(p.getValue1());

		}
	}

	private static class CoincidencePanel extends JXCollapsiblePane {

		private final JPanel poemPanel;

		public CoincidencePanel(PoemAsHrebNumbers poemAsHrebNumbers) {
			this.poemPanel = new JPanel();
			poemPanel.setLayout(new BoxLayout(poemPanel, BoxLayout.Y_AXIS));
			poemPanel.setBackground(Color.WHITE);
			setLayout(new GridBagLayout());

			fill(poemAsHrebNumbers);
			setBorder(BorderFactory.createLineBorder(getBackground(), 20));
			add(poemPanel);

			setCollapsed(true);
		}

		private void fill(PoemAsHrebNumbers poem) {
			int lastStrophe = 1;

			for (PoemAsHrebNumbers.Strophe strophe : poem.getStrophes()) {
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
				for (PoemAsHrebNumbers.Strophe.Verse verse : strophe.getVerses()) {
					String verseString = new StrBuilder().appendWithSeparators(verse.getHrebs(), ", ").toString();
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
							return coincidence.anotherHreb;
						case 1:
							return coincidence.coincidenceCount;
						case 2:
							return String.format("%.4f", coincidence.probability);
					}
					return null;
				}

			};
			table = new NonEditableTable(model);
			table.setSortable(false);
			add(table.getTableHeader(), BorderLayout.NORTH);
			add(table, BorderLayout.CENTER);
		}
	}
	private static class HrebDetailsCollapsiblePanel extends JXCollapsiblePane {

		private static final Insets INSETS = new Insets(0, 0, 0, 25);

		private HrebDetailsCollapsiblePanel() {
			setBackground(Color.white);
		}

		public void addHrebs(List<Hreb> hrebs, HrebDetailsCollapsiblePanelMouseListener hrebPanelListener) {
			int y = 0;
			for (Hreb hreb : hrebs) {
				JPanel hrebPanel = createHrebPanel(hreb, hrebPanelListener);
				hrebPanel.setBackground(Color.white);

				JLabel hrebNumber = new HtmlLabelBuilder().hx(3, String.valueOf(hreb.getNumber())).build();
				String content = new StrBuilder().appendWithSeparators(hreb.getWords(), ", ").toString();
				JTextArea hrebContent = new MultipleLinesLabel(content);

				hrebPanel.add(
					hrebNumber,
					new GridBagConstraintBuilder().fill(GridBagConstraints.VERTICAL).insets(INSETS).weightY(1).gridXY(0, 0).build()
				);

				hrebPanel.add(
					hrebContent,
					new GridBagConstraintBuilder().fill(BOTH).weightX(1).weightY(1).gridXY(1, 0).build()
				);

				add(
					hrebPanel,
					new GridBagConstraintBuilder().weightX(1).fill(GridBagConstraints.HORIZONTAL).gridXY(0, y++).build()
				);
			}
		}

		public void addHrebs(Map<Hreb, String> hrebs, HrebDetailsCollapsiblePanelMouseListener hrebPanelListener) {
			int y = 0;
			for (Map.Entry<Hreb, String> entry : hrebs.entrySet()) {
				JPanel hrebPanel = createHrebPanel(entry.getKey(), hrebPanelListener);
				hrebPanel.setBackground(Color.white);

				JLabel hrebNumber = new HtmlLabelBuilder().hx(3, String.valueOf(entry.getKey().getNumber())).build();
				String content = new StrBuilder().append("<html>").append(entry.getValue()).append("</html>").toString();
				JLabel hrebContent = new JLabel(content);

				hrebPanel.add(
					hrebNumber,
					new GridBagConstraintBuilder().fill(GridBagConstraints.VERTICAL).insets(INSETS).weightY(1).gridXY(0, 0).build()
				);

				hrebPanel.add(
					hrebContent,
					new GridBagConstraintBuilder().fill(BOTH).weightX(1).weightY(1).gridXY(1, 0).build()
				);

				add(
					hrebPanel,
					new GridBagConstraintBuilder().weightX(1).fill(GridBagConstraints.HORIZONTAL).gridXY(0, y++).build()
				);
			}
		}

		private JPanel createHrebPanel(final Hreb hreb, HrebDetailsCollapsiblePanelMouseListener hrebPanelListener) {
			final JPanel panel = new JPanel(new GridBagLayout());
			if (hrebPanelListener != null) {
				panel.addMouseListener(hrebPanelListener.getListenerFor(hreb, panel));
			}
			return panel;
		}
	}

	private static interface HrebDetailsCollapsiblePanelMouseListener  {

		MouseListener getListenerFor(final Hreb hreb, final JPanel panel);

	}

	private class GraphPanel extends JPanel implements ActionListener, ChangeListener, ItemListener {

		private final JCheckBox autoLayoutCheckBox;
		private final JSpinner alphaSpinner;
		private final JPanel graphPanel;
		private final GraphInfoPanel infoPanel;
		private final VertexInfoPanel vertexInfoPanel;
		private final JLabel graphLabel;
		private GraphViewerListener listener;
		private final JToggleButton coincidenceGraphToggle;
		private final JToggleButton deterministicGraphToggle;

		private final boolean ready;

		public GraphPanel() {
			setLayout(new GridBagLayout());

			JLabel alphaLabel = new JLabel("Hladina významnosti α");
			alphaSpinner = new JSpinner(new SpinnerNumberModel(0.1d, 0.001d, 1d, 0.01));
			alphaSpinner.setEditor(new JSpinner.NumberEditor(alphaSpinner, "0.000"));
			alphaSpinner.addChangeListener(this);


			JPanel alphaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			alphaPanel.add(
				alphaLabel
			);
			alphaPanel.add(
				alphaSpinner
			);

			autoLayoutCheckBox = new JCheckBox("Povolit automatické rozmístění grafu");
			autoLayoutCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (graphViewer != null) {
						if (autoLayoutCheckBox.isSelected()) {
							graphViewer.enableAutoLayout();
						} else {
							graphViewer.disableAutoLayout();
						}
					}
				}
			});

			JButton saveGraphButton = new JButton("Uložit jako obrázek PNG");
			saveGraphButton.addActionListener(this);

			coincidenceGraphToggle = new JToggleButton("Graf koincidence");
			coincidenceGraphToggle.addItemListener(this);
			deterministicGraphToggle = new JToggleButton("Deterministický graf");
			deterministicGraphToggle.addItemListener(this);
			coincidenceGraphToggle.setSelected(true);

			ButtonGroup graphTypeGroup = new ButtonGroup();
			graphTypeGroup.add(coincidenceGraphToggle);
			graphTypeGroup.add(deterministicGraphToggle);

			JPanel graphTypePanel = new JPanel(new BorderLayout());
			graphTypePanel.add(coincidenceGraphToggle, BorderLayout.WEST);
			graphTypePanel.add(deterministicGraphToggle, BorderLayout.EAST);

			JPanel centerSettingsPanel = new JPanel(new BorderLayout());
			centerSettingsPanel.add(graphTypePanel, BorderLayout.NORTH);
			centerSettingsPanel.add(autoLayoutCheckBox, BorderLayout.SOUTH);


			JPanel graphSettingPanel = new JPanel(new BorderLayout());
			graphSettingPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
			graphSettingPanel.add(alphaPanel, BorderLayout.NORTH);
			graphSettingPanel.add(centerSettingsPanel, BorderLayout.CENTER);
			graphSettingPanel.add(saveGraphButton, BorderLayout.SOUTH);

			GridBagConstraints c = new GridBagConstraintBuilder().gridXY(0, 0).anchor(GridBagConstraints.CENTER).build();
			add(graphSettingPanel, c);
			c = new GridBagConstraintBuilder().gridXY(0, 2).weightX(1).weightY(1).fill(BOTH).build();
			graphPanel = new JPanel(new BorderLayout());
			graphPanel.setPreferredSize(new Dimension(1000, 750));
			graphPanel.setBackground(Color.white);

			graphLabel = new JLabel();
			graphLabel.setOpaque(true);
			graphLabel.setBackground(Color.white);
			add(graphPanel, c);

			infoPanel = new GraphInfoPanel();
			add(infoPanel, new GridBagConstraintBuilder().gridXY(0, 3).weightX(1).weightY(1).fill(BOTH).build());

			vertexInfoPanel = new VertexInfoPanel();
			add(vertexInfoPanel, new GridBagConstraintBuilder().gridXY(0, 4).weightX(1).weightY(1).fill(BOTH).build());

			ready = true;
			stateChanged(new ChangeEvent(alphaSpinner));
		}
		@Override
		public void actionPerformed(ActionEvent e) {

			final BufferedImage image = ScreenImage.createImage(this);

			File lastDir = LastDirectory.getInstance().getLastDirectory();
			final File imageFile = FileChooserUtils.getFileToSave(lastDir, panel, "png");
			if (imageFile == null) {
				return;
			}
			try{
				if (imageFile.createNewFile()) {
					ImageIO.write(image, "png", imageFile);
				} else {
					throw new IOException("Cannot create file " + imageFile);
				}
			}catch(Exception ex){
				JOptionPane.showMessageDialog(panel, "Nepovedlo se uložit graf", "Chyba", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}

		@Override
		public void stateChanged(ChangeEvent e) {

			createGraph();

		}

		private void onGraphChanged(Graph graph, double alpha) {
			graphViewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_SWING_THREAD);
			graphViewer.enableAutoLayout();

			final View view = graphViewer.addDefaultView(false);
			ViewerPipe fromViewer = graphViewer.newViewerPipe();
			if (listener != null) {
				listener.viewClosed("");
			}

			listener = new GraphViewerListener(fromViewer);
			fromViewer.addSink(graph);
			fromViewer.removeElementSink(graph);
			fromViewer.addViewerListener(listener);

			final ConnectedComponents connectedComponents = new ConnectedComponents(graph);
			connectedComponents.compute();
			connectedComponents.setCountAttribute("component");

			int nodeSize = GraphStyle.getGraphNodeSize();
			int nodeTextSize = GraphStyle.getGraphNodeTextSize();
			view.setForeLayoutRenderer(new ForeLayoutRenderer(connectedComponents, nodeSize, nodeTextSize));
			graphLabel.setText("<html><h2>" + graph.getId() + "</h2></html>");
			graphPanel.removeAll();
			graphPanel.add(graphLabel, BorderLayout.NORTH);
			graphPanel.add(view, BorderLayout.CENTER);
			infoPanel.onGraphChanged(graph, connectedComponents, alpha);
			vertexInfoPanel.onGraphChanged(graph, connectedComponents, alpha);

			validate();
			repaint();
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				graphType = e.getSource() == coincidenceGraphToggle ? GraphType.COINCIDENCE : GraphType.DETERMINISTIC;

				createGraph();
			}
		}

		private void createGraph() {
			if (!ready) {
				return;
			}

			graphViewer = null;
			autoLayoutCheckBox.setSelected(true);

			final double alpha = (Double)alphaSpinner.getModel().getValue();
			Graph graph;
			final List<Hreb> allHrebs = model.getAllHrebs();
			switch (graphType) {
				case DETERMINISTIC:
					graph = createDeterministicGraph(allHrebs, createDeterministicMap(allHrebs), alpha);
					break;
				case COINCIDENCE:
					graph = createCoincidenceGraph(allHrebs, createCoincidenceMap(allHrebs), alpha);
					break;
				default:
					throw new IllegalStateException("Not supported graph type " + graphType);
			}

			onGraphChanged(graph, alpha);

		}

		private class GraphViewerListener implements ViewerListener {

			private final ViewerPipe fromViewer;
			private boolean loop = true;

			public GraphViewerListener(final ViewerPipe fromViewer) {
				this.fromViewer = fromViewer;
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (loop) {
							fromViewer.pump();
							try {
								Thread.sleep(250);
							} catch (InterruptedException ignored) {}
						}

					}
				}).start();
			}

			@Override
			public void viewClosed(String s) {
				System.out.println("viewClosed on '" + s + '\'');
				loop = false;
			}

			@Override
			public void buttonPushed(String s) {
			}

			@Override
			public void buttonReleased(String s) {
			}
		}
	}

	private class GraphInfoPanel extends JPanel {

		private final DecimalFormat DF = new DecimalFormat("0.###");
		private final MultipleLinesLabel componentsCount;
		private final RelativeCoherenceLevelPanel relativeCoherenceLevelPanel;
		private final RelativeCyclomaticNumberPanel relativeCyclomaticNumberPanel;
		private final NodeDegreePanel nodeDegreePanel;
		private final NodeDegreeTable nodeDegreeTable;
		private final VertexInfoPanel vertexInfoPanel;

		private final DenotationMath math = new DenotationMath();

		public GraphInfoPanel() {
			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			componentsCount = new MultipleLinesLabel();
			relativeCoherenceLevelPanel = new RelativeCoherenceLevelPanel();
			relativeCyclomaticNumberPanel = new RelativeCyclomaticNumberPanel();
			nodeDegreePanel = new NodeDegreePanel();
			nodeDegreeTable = new NodeDegreeTable();
			vertexInfoPanel = new VertexInfoPanel();
			int y = 0;
			add(componentsCount, new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).fill(HORIZONTAL).build());
			add(relativeCoherenceLevelPanel, new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).fill(HORIZONTAL).build());
			add(relativeCyclomaticNumberPanel, new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).fill(HORIZONTAL).build());
			add(nodeDegreePanel, new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).fill(HORIZONTAL).build());
			add(nodeDegreeTable.getTableHeader(), new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).fill(HORIZONTAL).build());
			add(nodeDegreeTable, new GridBagConstraintBuilder().gridXY(0, y++).weightX(1).fill(HORIZONTAL).build());
			add(vertexInfoPanel, new GridBagConstraintBuilder().gridXY(0, y).weightX(1).weightY(1).fill(BOTH).build());
		}

		public void onGraphChanged(Graph graph, ConnectedComponents connectedComponents, double alpha) {
			StringBuilder builder = new StringBuilder();
			final int nodeCount = graph.getNodeCount();
			final int edgeCount = graph.getEdgeCount();
			final int _componentsCount = connectedComponents.getConnectedComponentsCount();

			builder.append("Počet vrcholů v grafu je ").append(nodeCount);
			builder.append('\n').append("Počet hran v grafu je ").append(edgeCount);
			builder.append('\n').append("Počet komponent v grafu je ").append(_componentsCount);
			builder.append('\n')
				.append('\n').append("Index nespojitosti je ").append(DF.format(model.getNonContinuousIndex()));
			final double nonIsolationIndex = model.getNonIsolationIndex();
			builder.append('\n').append("Index neizolovanosti je ").append(nonIsolationIndex == Double.POSITIVE_INFINITY ? '∞' : DF.format(nonIsolationIndex));
			final double reachabilityIndex = model.getReachabilityIndex();
			builder.append('\n').append("Index dosáhnutelnosti je ").append(reachabilityIndex == Double.POSITIVE_INFINITY ? '∞' : DF.format(reachabilityIndex));

			componentsCount.setText(builder.toString());
			relativeCoherenceLevelPanel.set(alpha, nodeCount, _componentsCount, math.computeRelativeConnectionRate(nodeCount, _componentsCount));
			relativeCyclomaticNumberPanel.set(alpha, edgeCount, nodeCount, _componentsCount, math.computeRelativeCyclomaticNumber(nodeCount, _componentsCount, edgeCount));
			nodeDegreePanel.set(edgeCount, nodeCount, math.computeConnotativeConcentration(nodeCount, edgeCount));

			NodeDegreesModel nodeDegreesModel = new NodeDegreesModel();
			for (Node node : graph) {
				nodeDegreesModel.put(node);
			}
			nodeDegreeTable.setModel(nodeDegreesModel);

			APSP apsp = new APSP(graph);
			apsp.setDirected(false); // undirected graph
			apsp.compute(); // the method that actually computes shortest paths

		}
	}
}
