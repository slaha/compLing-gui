package cz.slahora.compling.gui.panels;

import org.jfree.chart.ChartPanel;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridBagLayout;

public class ChartPanelWrapper extends JPanel {


	private final ChartPanel chartPanel;

	public ChartPanelWrapper(ChartPanel chartPanel) {
		super(new GridBagLayout());
		this.chartPanel = chartPanel;
		setBackground(Color.white);
	}

	public ChartPanel getChartPanel() {
		return chartPanel;
	}
}
