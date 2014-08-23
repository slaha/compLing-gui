package cz.slahora.compling.gui.panels;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

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
}
