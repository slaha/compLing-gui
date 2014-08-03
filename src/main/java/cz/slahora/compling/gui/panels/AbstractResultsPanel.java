package cz.slahora.compling.gui.panels;

import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridBagConstraints;

public abstract class AbstractResultsPanel {

	protected final JPanel panel;

	protected AbstractResultsPanel(JPanel panel) {
		this.panel = panel;
		panel.setBackground(Color.WHITE);
	}

	protected void putChartPanel(int y, ChartPanelWrapper chartPanel) {

		changeChartPanel(y, null, chartPanel);
	}

	protected void changeChartPanel(ChartPanelWrapper old, ChartPanelWrapper chartPanel) {
		int currentY = (Integer)old.getClientProperty("y");

		changeChartPanel(currentY, old, chartPanel);
	}

	private void changeChartPanel(int y, JPanel old, ChartPanelWrapper newChartPanel) {

		if (old != null) {
			panel.remove(old);
		}

		newChartPanel.putClientProperty("y", y);
		panel.add(
			newChartPanel,
			new GridBagConstraintBuilder().gridxy(0, y).fill(GridBagConstraints.BOTH).anchor(GridBagConstraints.NORTH).weightx(1).weighty(1).build()
		);
		panel.validate();
	}
}
