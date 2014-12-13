package cz.slahora.compling.gui.ui;

import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import org.jfree.chart.ChartPanel;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class ChartPanelWrapper {

	protected final JPanel panel;
	protected final ChartPanel chartPanel;

	public ChartPanelWrapper(ChartPanel chartPanel) {
		panel =  new JPanel(new GridBagLayout());
		this.chartPanel = chartPanel;
		panel.setBackground(Color.white);
	}

	public ChartPanelWrapper addPlot() {
		return addPlot(new GridBagConstraintBuilder().gridXY(0, 0).fill(GridBagConstraints.HORIZONTAL).weightX(1).build());
	}

	public ChartPanelWrapper addPlot(GridBagConstraints constraints) {
		panel.add(chartPanel, constraints);
		return this;
	}

	public JComponent getPanel() {
		return panel;
	}

	public ChartPanelWrapper add(Component component) {
		panel.add(component);
		return this;
	}
	public ChartPanelWrapper add(Component component, Object constraints) {
		panel.add(component, constraints);
		return this;
	}
}
