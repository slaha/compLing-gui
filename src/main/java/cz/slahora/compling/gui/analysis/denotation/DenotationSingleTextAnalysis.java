package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.CompLing;
import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.MapUtils;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

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
	public void analyse(JPanel mainPanel, final ResultsHandler handler, Map<WorkingText, CompLing> texts) {

		final WorkingText text = MapUtils.getFirstKey(texts);

		JFrame frame = new JFrame("Denotační analýza");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setContentPane(new DenotationAnalysis.DenotationPanel(text));

		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				handler.handleResult(DenotationSingleTextAnalysis.this);
			}
		});
	}

	@Override
	public Results getResults() {
		return new DenotationSingleTextAnalysisResults();
	}

	private static class DenotationSingleTextAnalysisResults implements Results {
		@Override
		public boolean resultsOk() {
			return false;
		}

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
