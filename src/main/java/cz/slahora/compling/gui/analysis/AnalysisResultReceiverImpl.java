package cz.slahora.compling.gui.analysis;

import cz.slahora.compling.gui.ui.ResultsPanel;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.Locale;

public class AnalysisResultReceiverImpl implements AnalysisResultReceiver {

	@Override
	public void send(Results results) {

		ResultsPanel result = results.getResultPanel();
		JFrame frame = new JFrame(String.format(Locale.getDefault(), "Výsledek analýzy – %s", results.getAnalysisName()));

		JPanel panel = result.getPanel();
		JToolBar topPanel = new AnalysisReceiverToolbar(result);
		topPanel.setFloatable(false);
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
