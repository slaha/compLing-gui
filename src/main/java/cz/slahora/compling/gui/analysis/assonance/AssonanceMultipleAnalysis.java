package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.CompLing;
import cz.slahora.compling.gui.analysis.Analysis;
import cz.slahora.compling.gui.analysis.MultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.Results;
import cz.slahora.compling.gui.analysis.ResultsHandler;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.ui.ResultsPanel;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import cz.slahora.compling.gui.utils.IconUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

public class AssonanceMultipleAnalysis implements MultipleTextsAnalysis {

	private final static String[] BASE_VOCALS = new String[] { "a", "á", "e", "é", "i", "í", "o", "ó", "u", "ů", "ú", "y", "ý" };
	private static final int MAX_STEPS = 15;

	private final Set<String> vocals;

	private enum AssonanceType { DIFFERENT_SHIFTS, CONSTANT_SHIFTS;
		public int shift;
	};

	public AssonanceMultipleAnalysis() {
		vocals = new LinkedHashSet<String>(Arrays.asList(BASE_VOCALS));
	}

	@Override
	public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {

		AssonanceType assonanceType = showOptionPane(mainPanel);

		if (assonanceType == null) {
			return;
		}

		switch (assonanceType) {
			case DIFFERENT_SHIFTS:
				handler.handleResult(new AssonanceDifferentShiftsAnalysis(texts, vocals));
				break;
			case CONSTANT_SHIFTS:
				Selections selections = new Selections();

				final int i = new SelectionPanel(mainPanel, texts, selections).showSelectionsPanel();
				if (i != JOptionPane.OK_OPTION) {
					return;
				}
				handler.handleResult(new AssonanceConstantShiftAnalysis(texts, selections, vocals, assonanceType.shift));
				break;
		}
	}

	private AssonanceType showOptionPane(JPanel mainPanel) {


		final JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));

		JLabel headline = new HtmlLabelBuilder().b("Zvolte hypotézu, která má být testována:").build();
		headline.setAlignmentX(Component.LEFT_ALIGNMENT);
		headline.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

		ButtonGroup group = new ButtonGroup();

		JRadioButton differentShifts = new JRadioButton("<html>H<sub>0</sub>: Střední hodnoty jsou vzhledem k různým posunům stejné.</html>", true);
		differentShifts.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel differentShiftsDesc = new JLabel("<html>Na všech textech bude testována asonance pro posuny o 1 až 15 vokálů. V případě zamítnutí hypotézy H<sub>0</sub> jsou nalezeny posuny, které se od sebe vzájemně liší.<html>");
		differentShiftsDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
		differentShiftsDesc.setBorder(BorderFactory.createEmptyBorder(5, 30, 10, 0));

		final JRadioButton constantShift = new JRadioButton("<html>H<sub>0</sub>: Střední hodnoty jsou vzhledem ke konstantnímu posunu stejné.</html>");
		constantShift.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel constantShiftDesc = new JLabel("<html>Texty budou rozděleny do jednotlivých výběrů. V případě zamítnutí hypotézy H<sub>0</sub> jsou nalezeny výběry, které se od sebe vzájemně liší.<html>");
		constantShiftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
		constantShiftDesc.setBorder(BorderFactory.createEmptyBorder(5, 30, 1, 0));

		final JSpinner shiftSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
		shiftSpinner.setEnabled(false);
		shiftSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

		final JLabel shiftLbl = new JLabel("Posun: ");
		shiftLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		spinnerPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 10, 0));
		spinnerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		spinnerPanel.add(shiftLbl);
		spinnerPanel.add(shiftSpinner);

		group.add(differentShifts);
		group.add(constantShift);

		options.add(headline);
		options.add(differentShifts);
		options.add(differentShiftsDesc);
		options.add(constantShift);
		options.add(constantShiftDesc);
		options.add(spinnerPanel);

		JPanel vocalsContainerPanel = new JPanel();
		vocalsContainerPanel.setLayout(new BoxLayout(vocalsContainerPanel, BoxLayout.Y_AXIS));
		vocalsContainerPanel.setBorder(BorderFactory.createTitledBorder("Nastavení vokálů"));

		JLabel vocalsLabel = new HtmlLabelBuilder().i("Kliknutím na vokál jej odeberete. Pro přidání vokálu stiskněte tlačítko [+]").build();
		vocalsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		vocalsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JPanel vocalsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		vocalsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JButton add = new JButton(IconUtils.getIcon(IconUtils.Icon.ADD));
		final String vocalsCount = "Počet vokálů: %d";

		final JLabel vocalsCountLbl = new JLabel(String.format(vocalsCount, vocals.size()));
		vocalsCountLbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
		for (final String vocal : vocals) {
			final JButton b = createButton(vocal, vocalsPanel, vocalsCountLbl, vocalsCount);
			vocalsPanel.add(b);
		}

		vocalsContainerPanel.add(vocalsLabel);
		JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		addPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		addPanel.add(add);
		vocalsContainerPanel.add(addPanel);
		vocalsContainerPanel.add(vocalsPanel);

		options.add(vocalsContainerPanel);

		JOptionPane pane = new JOptionPane(options, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

		final JDialog dialog = pane.createDialog(mainPanel, "Nastavení analýzy asonance");
		dialog.setResizable(true);

		constantShift.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getSource() == constantShift) {
					final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
					shiftSpinner.setEnabled(selected);
				}
			}
		});

		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String vocal = JOptionPane.showInputDialog(options, "Zadejte nový vokál:", "Nový vokál", JOptionPane.QUESTION_MESSAGE);
				if (StringUtils.isNotBlank(vocal)) {
					if (vocals.add(vocal)) {
						final JButton b = createButton(vocal, vocalsPanel, vocalsCountLbl, vocalsCount);
						vocalsPanel.add(b);
						vocalsCountLbl.setText(String.format(vocalsCount, vocals.size()));
						vocalsPanel.revalidate();
					}
				}
			}
		});

		dialog.pack();
		dialog.setVisible(true);
		dialog.dispose();

		Object selectedValue = pane.getValue();

		if (selectedValue == null) {
			selectedValue = JOptionPane.CLOSED_OPTION;
		}
		if (!selectedValue.equals(JOptionPane.OK_OPTION)) {
			return null;
		}

		AssonanceType.CONSTANT_SHIFTS.shift = ((Number)shiftSpinner.getValue()).intValue();
		return differentShifts.isSelected() ? AssonanceType.DIFFERENT_SHIFTS : AssonanceType.CONSTANT_SHIFTS;
	}

	private JButton createButton(final String vocal, final JPanel vocalsPanel, final JLabel vocalsCountLbl, final String vocalsCount) {
		final JButton b = new JButton(vocal);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				vocals.remove(vocal);
				vocalsCountLbl.setText(String.format(vocalsCount, vocals.size()));
				vocalsPanel.remove(b);
				vocalsPanel.repaint();
			}
		});
		return b;
	}

	@Override
	public Results getResults() {
		return null;
	}

	private class AssonanceDifferentShiftsAnalysis implements Analysis {
		private final Map<WorkingText, CompLing> texts;
		private final String[] vocals;

		public AssonanceDifferentShiftsAnalysis(Map<WorkingText, CompLing> texts, Collection<String> vocals) {
			this.texts = texts;
			this.vocals = vocals.toArray(new String[vocals.size()]);
		}

		@Override
		public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {
		}

		@Override
		public Results getResults() {
			return new Results() {
				@Override
				public boolean resultsOk() {
					return true;
				}

				@Override
				public ResultsPanel getResultPanel() {
					DifferentShiftsModel model = new DifferentShiftsModel(texts, vocals, MAX_STEPS);

					return new DifferentShiftsResultsPanel(model);
				}

				@Override
				public String getAnalysisName() {
					return "Asonance pro posuny o 1 až 15 vokálů";
				}
			};
		}
	}

	private class AssonanceConstantShiftAnalysis implements Analysis {
		private final Map<WorkingText, CompLing> texts;
		private final Selections selections;
		private final int shift;
		private final String[] vocals;

		public AssonanceConstantShiftAnalysis(Map<WorkingText, CompLing> texts, Selections selections, Collection<String> vocals, int shift) {
			this.texts = texts;
			this.selections = selections;
			this.shift = shift;
			this.vocals = vocals.toArray(new String[vocals.size()]);
		}

		@Override
		public void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts) {
		}

		@Override
		public Results getResults() {
			return new Results() {
				@Override
				public boolean resultsOk() {
					return true;
				}

				@Override
				public ResultsPanel getResultPanel() {

					OneShiftModel model = new OneShiftModel(shift, selections, texts, vocals);

					return new OneShiftResultsPanel(model);
				}

				@Override
				public String getAnalysisName() {
					return "Asonance pro fixní posun o " + shift + " " + (shift == 1 ? "vokál" : shift > 4 ? "vokálů" : "vokály");
				}
			};
		}
	}
}
