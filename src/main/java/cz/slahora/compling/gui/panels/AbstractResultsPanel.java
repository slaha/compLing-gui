package cz.slahora.compling.gui.panels;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

public abstract class AbstractResultsPanel {

	protected final JPanel panel;

	protected AbstractResultsPanel(JPanel panel) {
		this.panel = panel;
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10)); //..padding
	}

	protected JComponent changeChartPanel(JComponent oldChart, JComponent chartParent, ChartPanelWrapper newChartPanel) {

		if (oldChart != null) {
			chartParent.remove(oldChart);
		}

		final JComponent comp = newChartPanel.getPanel();
		comp.setAlignmentX(Component.LEFT_ALIGNMENT);
		chartParent.add(comp, BorderLayout.CENTER);
		chartParent.validate();

		return comp;
	}

	protected Component createVerticalSpace() {
		return createVerticalSpace(30);
	}
	protected Component createVerticalSpace(int h) {
		final Component rigidArea = Box.createRigidArea(new Dimension(1, h));
		rigidArea.setBackground(Color.white);
		return rigidArea;
	}
}
