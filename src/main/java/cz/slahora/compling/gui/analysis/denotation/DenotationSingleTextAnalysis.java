package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.CompLing;
import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingText;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 8:39</dd>
 * </dl>
 */
public class DenotationSingleTextAnalysis implements SingleTextAnalysis<CharacterFrequency> {

	@Override
	public void analyse(JPanel mainPanel, CompLing compLing, WorkingText text) {
		JFrame frame = new JFrame("Denotační analýza");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setContentPane(new DenotationAnalysis.DenotationPanel(text));

		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public CharacterFrequency getResults() {
		return null;
	}
}
