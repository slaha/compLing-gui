package cz.slahora.compling.gui.analysis;

import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

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
	public boolean canReceive(Class<?> aClass) {
		return CharacterFrequency.class.equals(aClass);
	}

	@Override
	public void send(Results results) throws TypeNotSupportedException {

		ResultsPanel result = results.getResultPanel();
		JFrame frame = new JFrame("Výsledek analýzy");

		JPanel panel = result.getPanel();
		JToolBar topPanel = new AnalysisReceiverToolbar(result);
		JScrollPane scrollPane = new JScrollPane(panel);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.add(topPanel, new GridBagConstraintBuilder().gridxy(0, 0).fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
		contentPanel.add(scrollPane, new GridBagConstraintBuilder().gridxy(0, 1).fill(GridBagConstraints.BOTH).weighty(1).weightx(1).build());

		frame.setContentPane(contentPanel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
	}
}
