package cz.slahora.compling.gui.panels.words;

import cz.slahora.compling.gui.panels.ChartPanelWrapper;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.CategoryPlot;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.*;

public class ResizableChartPanelWrapper extends ChartPanelWrapper{

	private JPanel footer;

	public ResizableChartPanelWrapper(ChartPanel chartPanel) {
		super(chartPanel);
		footer = new JPanel(new GridBagLayout());
		footer.setBackground(Color.white);
	}

	public JComponent getPanel() {

		JPanel p = new JPanel(new BorderLayout());

		final JScrollPane scrollPane = new JScrollPane(panel);
		panel.putClientProperty("panel", panel.getParent());

		p.add(scrollPane, BorderLayout.NORTH);
		p.add(footer, BorderLayout.SOUTH);


		if (chartPanel.getChart().getPlot() instanceof CategoryPlot) {

		}


		return p;
	}

	public void addFooter(JPanel toAdd, GridBagConstraints constraints) {
		footer.add(toAdd, constraints);
	}

	public void resizeChart(int widthAdd, int heightAdd) {

		Dimension size = chartPanel.getSize();

		final Dimension dimension = new Dimension(size.width + widthAdd, size.height + heightAdd);
		panel.setPreferredSize(dimension);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				((JComponent)panel.getClientProperty("panel")).revalidate();
			}
		});

	}
}
