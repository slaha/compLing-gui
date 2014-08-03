package cz.slahora.compling.gui.panels;

import org.jfree.chart.ChartPanel;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridBagLayout;

public class ChartPanelWrapper extends JPanel {

	public ChartPanelWrapper(ChartPanel chartPanel) {
		super(new GridBagLayout());
		setBackground(Color.white);
	}
}
