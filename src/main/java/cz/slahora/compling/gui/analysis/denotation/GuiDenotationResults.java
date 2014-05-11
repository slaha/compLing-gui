package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.Spike;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.apache.commons.lang.text.StrBuilder;
import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

		int y = 0;

		//..top headline
		panel.add(
			new HtmlLabelBuilder().hx(1, "Denotační analýza").build(),
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
		);

		JXCollapsiblePane positionSpikes = new JXCollapsiblePane();
		positionSpikes.setBackground(Color.WHITE);
		positionSpikes.setLayout(new GridBagLayout());
		positionSpikes.setCollapsed(true);
		for (int spike = 1; spike <= model.getSpikesCount(); spike++) {
			positionSpikes.add(new PositionSpikePanel(spike, model.getPositionSpike(spike)),
				new GridBagConstraintBuilder().gridxy(0, spike).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
			);
		}
		ToggleHeader toggle = new ToggleHeader(positionSpikes.getActionMap().get("toggle"),
			new HtmlLabelBuilder().hx(2, "Poziční hřeby").build().getText());

		panel.add(
			toggle,
			new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1).anchor(GridBagConstraints.NORTH).build()
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
			setBackground(Color.white);

			add(
				new HtmlLabelBuilder().hx(3, "Poziční hřeb č. " + spikeNumber).build(),
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).gridxy(0, 0).insets(new Insets(0, 0, 0, 25)).build()
			);
			StrBuilder builder = new StrBuilder();
			builder.appendWithSeparators(wordNumbers, ", ");
			add(
				new JLabel(builder.toString()),
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).gridxy(1, 0).weightx(1).build()
			);

			setBorder(new EmptyBorder(0, 0, 5, 0));
		}
	}

	private class CoreSpikePanel extends JPanel {
		private final GuiDenotationResultsModel.TextCore core;
		final Insets insets = new Insets(0, 0, 0, 25);
		final JLabel spikeLabel;
		private JXCollapsiblePane outsideCorePanel;
		private JXCollapsiblePane corePanel;

		public CoreSpikePanel(GuiDenotationResultsModel.TextCore core, JLabel spikeLabel) {
			super(new GridBagLayout());

			this.core = core;
			this.spikeLabel = spikeLabel;

			setBackground(Color.white);

			refresh();
		}

		private void refresh() {
			boolean collapseCore = corePanel != null && corePanel.isCollapsed();
			boolean collapseOutsideCore = outsideCorePanel == null || outsideCorePanel.isCollapsed();
			removeAll();

			corePanel = new JXCollapsiblePane();
			corePanel.setCollapsed(collapseCore);
			addSpikes(corePanel, core.getCore());
			Action action = corePanel.getActionMap().get("toggle");
			ToggleHeader toggleHeader = new ToggleHeader(action, new HtmlLabelBuilder().hx(3, "Jádro textu").build().getText());
			add(
				toggleHeader,
				new GridBagConstraintBuilder().weighty(1).gridxy(0, 0).anchor(GridBagConstraints.NORTH).build()
			);

			add(
				corePanel,
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).weightx(1).gridxy(1, 0).build()
			);

			outsideCorePanel = new JXCollapsiblePane();
			outsideCorePanel.setCollapsed(collapseOutsideCore);
			outsideCorePanel.setLayout(new GridBagLayout());
			addSpikes(outsideCorePanel, core.getNotInCore());

			action = outsideCorePanel.getActionMap().get("toggle");
			toggleHeader = new ToggleHeader(action, new HtmlLabelBuilder().hx(3, "Hřeby mimo jádro").build().getText());
			add(
				toggleHeader,
				new GridBagConstraintBuilder().weighty(1).gridxy(0, 1).anchor(GridBagConstraints.NORTH).build()
			);
			add(
				outsideCorePanel,
				new GridBagConstraintBuilder().fill(GridBagConstraints.HORIZONTAL).gridxy(1, 1).build()
			);

			String spikeLbl = core.size() == 1 ? "hřeb" : core.size() < 5 && core.size() > 0 ? "hřeby" : "hřebů";
			spikeLabel.setText(String.format("Jádro textu obsahuje %d %s. Kardinální číslo jádra je %d.", core.size(), spikeLbl, core.getCoreCardinalNumber()));
		}

		private void addSpikes(JPanel panel, List<Spike> spikes) {
			int y = 0;
			for (Spike spike : spikes) {
				JPanel spikePanel = createSpikePanel(spike);
				spikePanel.setBackground(Color.white);

				JLabel spikeNumber = new HtmlLabelBuilder().hx(3, String.valueOf(spike.getNumber())).build();
				String content = new StrBuilder().append("<html>").appendWithSeparators(spike.getWords(), ", ").append("</html>").toString();
				JLabel spikeContent = new JLabel(content);

				spikePanel.add(
					spikeNumber,
					new GridBagConstraintBuilder().fill(GridBagConstraints.VERTICAL).insets(insets).weighty(1).gridxy(0, 0).build()
				);

				spikePanel.add(
					spikeContent,
					new GridBagConstraintBuilder().fill(GridBagConstraints.BOTH).weightx(1).weighty(1).gridxy(1, 0).build()
				);

				panel.add(
					spikePanel,
					new GridBagConstraintBuilder().weightx(1).fill(GridBagConstraints.HORIZONTAL).gridxy(0, y++).build()
				);
			}
		}

		private JPanel createSpikePanel(final Spike spike) {
			final JPanel panel = new JPanel(new GridBagLayout());

			panel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (core.isInCore(spike)) {
						core.remove(spike);
					} else {
						core.add(spike);
					}
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
			});
			return panel;
		}
	}
}
