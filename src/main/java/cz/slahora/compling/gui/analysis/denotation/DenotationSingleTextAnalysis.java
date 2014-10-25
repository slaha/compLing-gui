package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.CompLing;
import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.MapUtils;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
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

	JFrame frame;
	private final DenotationSingleTextAnalysisResults results = new DenotationSingleTextAnalysisResults();
	private WorkingText text;
	private DenotationAnalysis.DenotationPanel denotationPanel;

	@Override
	public void analyse(JPanel mainPanel, final ResultsHandler handler, Map<WorkingText, CompLing> texts) {

		this.text = MapUtils.getFirstKey(texts);

	    frame = new JFrame("Denotační analýza");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		denotationPanel = new DenotationAnalysis.DenotationPanel(this, frame, text);
		frame.setContentPane(denotationPanel);

		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (denotationPanel.isAnySpikeInTheTable()) {
					final int confirmDialog = showConfirmExitDialog(frame);

					switch (confirmDialog) {
						case JOptionPane.YES_OPTION:
							denotationPanel.save();
							//..fall through
						case JOptionPane.NO_OPTION:
							frame.dispose();
						//..do nothing on cancel
					}
				} else {
					frame.dispose();
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
				handler.handleResult(DenotationSingleTextAnalysis.this);
			}
		});

	}

	private int showConfirmExitDialog(JFrame parent) {
		return JOptionPane.showConfirmDialog(
			parent,
			"Tabulka obsahuje denotační elementy. Skutečně chcete uzavřít okno?\n\n" +
				"Kliknutím na 'ano' nejprve uložíte rozpracovanou analýzu a poté bude okno uzavřeno,\n" +
				"Kliknutím na 'ne' uzavřete okno bez uložení,\n" +
				"Kliknutím na 'cancel' uzavřete tento dialog, ale ne okno s analýzou.\n\n" +
				"Uložit před uzavřením?",
			"Uložit před uzavřením?",
			JOptionPane.YES_NO_CANCEL_OPTION
		);
	}

	@Override
	public Results getResults() {
		return results;
	}

	public void done() {
		results.analysisComplete = true;
		results.text = text;
		results.denotation = denotationPanel.getDenotation();
		frame.dispose();
	}

	private static class DenotationSingleTextAnalysisResults implements Results {
		private boolean analysisComplete;
		private WorkingText text;
		private IDenotation denotation;

		@Override
		public boolean resultsOk() {
			return analysisComplete;
		}

		@Override
		public ResultsPanel getResultPanel() {
			return new ResultsPanel() {
				@Override
				public JPanel getPanel() {
					return new GuiDenotationResults(text, denotation).getPanel();
				}

				@Override
				public CsvData getCsvData() {
					return new CsvData();
				}
			};
		}

		@Override
		public String getAnalysisName() {
			return "Denotační analýza textu " + text.getName();
		}
	}
}
