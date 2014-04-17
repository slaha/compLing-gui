package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.CompLing;
import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;

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
	public Results getResults() {
		return new DenotationSingleTextAnalysisResults();
	}

	//TODO
	private static class DenotationSingleTextAnalysisResults implements Results {
		@Override
		public ResultsPanel getResultPanel() {
			return new ResultsPanel() {
				@Override
				public JPanel getPanel() {
					return new JPanel();
				}

				@Override
				public CsvData getCsvData() {
					return new CsvData();
				}
			};
		}
	}
}
