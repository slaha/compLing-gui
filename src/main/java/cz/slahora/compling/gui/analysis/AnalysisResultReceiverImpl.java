package cz.slahora.compling.gui.analysis;

import cz.slahora.compling.gui.panels.ResultsPanel;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Frame;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 21:51</dd>
 * </dl>
 */
public class AnalysisResultReceiverImpl implements AnalysisResultReceiver {

	@Override
	public void send(Results results) {

		ResultsPanel result = results.getResultPanel();
		JFrame frame = new JFrame("Výsledek analýzy");

		JPanel panel = result.getPanel();
		JToolBar topPanel = new AnalysisReceiverToolbar(result);
		JScrollPane scrollPane = new JScrollPane(panel);

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(topPanel, BorderLayout.NORTH);
		contentPanel.add(scrollPane, BorderLayout.CENTER);

		frame.setContentPane(contentPanel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
	}
}
