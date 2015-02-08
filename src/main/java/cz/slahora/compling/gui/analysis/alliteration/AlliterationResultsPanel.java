package cz.slahora.compling.gui.analysis.alliteration;

import cz.slahora.compling.gui.analysis.assonance.NonEditableTable;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.ui.AbstractResultsPanel;
import cz.slahora.compling.gui.ui.ResultsPanel;
import cz.slahora.compling.gui.ui.ResultsScrollablePanel;
import cz.slahora.compling.gui.ui.WrapLayout;
import cz.slahora.compling.gui.utils.HtmlLabelBuilder;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Collection;

class AlliterationResultsPanel extends AbstractResultsPanel implements ResultsPanel {

	private static final double DEFAULT_ALPHA = 0.05d;

	private static final DecimalFormat P_DECIMAL_FORMAT = new DecimalFormat("0.#####");
	private static final DecimalFormat KA_DECIMAL_FORMAT = new DecimalFormat("0.##");

	private final AlliterationTableModel tableModel;

	private double alpha = DEFAULT_ALPHA;
	private final JLabel kaSumLabel;
	private final JLabel resultKaLabel;

	public AlliterationResultsPanel(final AlliterationModel model) {
		super(new ResultsScrollablePanel());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		addToPanel(new HtmlLabelBuilder().hx(1, "Aliterace pro text %s", model.getTextName()).build());

		final SpinnerNumberModel alphaSpinnerModel = new SpinnerNumberModel(alpha, 0.001, 1, 0.01);
		JSpinner alphaSpinner = new JSpinner(alphaSpinnerModel);
		JComponent spinnerEditor = alphaSpinner.getEditor();
		Dimension prefSize = spinnerEditor.getPreferredSize();
		prefSize = new Dimension(100, prefSize.height);
		spinnerEditor.setPreferredSize(prefSize);
		alphaSpinner.setMaximumSize(new Dimension(300, 100));
		alphaSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				alpha = (Double)(alphaSpinnerModel.getValue());
				onAlphaChanged(model);
			}
		});

		JPanel alphaPanel = new JPanel(new WrapLayout(WrapLayout.LEFT));
		alphaPanel.setBackground(Color.white);
		final JComponent label = new JLabel("Aliterační charakter básně na hladině výnamnosti α=");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		alphaPanel.add(label);
		alphaPanel.add(alphaSpinner);
		addToPanel(alphaPanel);

		JLabel kaLabel = new JLabel("KA = ");
		final Font font = kaLabel.getFont().deriveFont(15f).deriveFont(Font.BOLD);
		kaLabel.setFont(font);

		kaSumLabel = new JLabel();
		kaSumLabel.setHorizontalAlignment(SwingConstants.CENTER);
		kaSumLabel.setFont(font);
		final MatteBorder fractionLine = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black);
		final Border padding = BorderFactory.createEmptyBorder(0, 10, 0, 10);
		kaSumLabel.setBorder(BorderFactory.createCompoundBorder(fractionLine, padding));

		JLabel versesCountLabel = new JLabel(P_DECIMAL_FORMAT.format(model.getVersesCount()), SwingConstants.CENTER);
		versesCountLabel.setBorder(padding);
		versesCountLabel.setFont(font);

		JLabel resultEqualLabel = new JLabel(" = ");
		resultEqualLabel.setFont(font);

		resultKaLabel = new JLabel();
		resultKaLabel.setHorizontalAlignment(SwingConstants.CENTER);
		resultKaLabel.setBorder(BorderFactory.createCompoundBorder(fractionLine, padding));
		final CompoundBorder underlinePaddingBorder = BorderFactory.createCompoundBorder(fractionLine, padding);
		final CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0), underlinePaddingBorder);
		resultKaLabel.setBorder(BorderFactory.createCompoundBorder(fractionLine, compoundBorder));
		resultKaLabel.setFont(font);

		JPanel resultKaPanel = new JPanel();
		resultKaPanel.setBackground(Color.white);
		resultKaPanel.add(resultEqualLabel);
		resultKaPanel.add(resultKaLabel);

		JPanel fraction = new JPanel(new BorderLayout());
		fraction.setBackground(Color.white);
		fraction.add(kaSumLabel, BorderLayout.NORTH);
		fraction.add(versesCountLabel, BorderLayout.SOUTH);

		JPanel equation = new JPanel();
		equation.setBackground(Color.white);
		equation.add(kaLabel);
		equation.add(fraction);



		equation.add(resultKaPanel);

		addToPanel(equation);

		addToPanel((JComponent) Box.createRigidArea(new Dimension(0, 10)));

		tableModel = new AlliterationTableModel(P_DECIMAL_FORMAT, KA_DECIMAL_FORMAT);
		JXTable table = new NonEditableTable(tableModel);
		addToPanel(table.getTableHeader());
		addToPanel(table);

		onAlphaChanged(model);
	}

	private void onAlphaChanged(AlliterationModel model) {
		kaSumLabel.setText(P_DECIMAL_FORMAT.format(model.getKaSum(alpha)));
		resultKaLabel.setText(P_DECIMAL_FORMAT.format(model.getTotalKa(alpha)));

		tableModel.clear();
		for (int verse = 1; verse <= model.getVersesCount(); verse++) {
			final Collection<String> phonemes = model.getPhonemes(verse);
			final int n = model.getWordsCountInVerse(verse);
			final int[] k = model.getKsFor(verse, phonemes);
			final double p = model.getProbability(verse);
			final double ka = model.getKA(alpha, p);

			tableModel.addRow(verse, phonemes, n, k, p, ka);
		}
		tableModel.fireTableDataChanged();
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public CsvData getCsvData() {
		CsvData data = new CsvData();
		data.addSection();
		final CsvData.CsvDataSection section = data.getCurrentSection();
		section.addHeader("Číslo verše");
		section.addHeader("Délka verše (počet hlásek)");
		section.addHeader("Aliterace ve verši");
		final int rowCount = tableModel.getRowCount();
		for (int row = 0; row < rowCount; row++) {
			final AlliterationTableModel.Row tableModelRow = tableModel.getRow(row);
			if (!tableModelRow.isContainsAlliteration()) {
				continue;
			}
			section.startNewLine();
			section.addData(tableModelRow.getVerse());
			section.addData(tableModelRow.getSize());
			for (Integer alliteration : tableModelRow.getAlliteration()) {
				section.addData(alliteration);
			}
		}
		return data;
	}
}
