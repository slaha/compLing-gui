package cz.slahora.compling.gui.analysis.denotation;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

class RelativeCoherenceLevelPanel extends JPanel implements Observer {

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###");

	private final JLabel alphaLabel;
	private final JLabel nominator;
	private final JLabel denominator;
	private final JLabel resultLabel;

	public RelativeCoherenceLevelPanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT));

		final JLabel kappa = new JLabel("κ");
		Font font = kappa.getFont().deriveFont(15f);
		font = font.deriveFont(Font.BOLD);

		kappa.setFont(font.deriveFont(Font.ITALIC).deriveFont(20f));
		add(kappa);

		alphaLabel = new JLabel();

		JLabel rel = new JLabel("rel");
		rel.setFont(getFont().deriveFont(Font.ITALIC));

		JPanel alphaRel = new JPanel(new BorderLayout());
		alphaRel.add(alphaLabel, BorderLayout.NORTH);
		alphaRel.add(rel, BorderLayout.SOUTH);

		add(alphaRel);

		JLabel equals = new JLabel("=");
		equals.setFont(font);
		add(equals);


		nominator = new JLabel();
		nominator.setHorizontalAlignment(SwingConstants.CENTER);
		nominator.setFont(font);
		final MatteBorder fractionLine = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black);
		final Border padding = BorderFactory.createEmptyBorder(0, 10, 0, 10);
		nominator.setBorder(BorderFactory.createCompoundBorder(fractionLine, padding));

		denominator = new JLabel();
		denominator.setHorizontalAlignment(SwingConstants.CENTER);
		denominator.setBorder(padding);
		denominator.setFont(font);

		JPanel fraction = new JPanel(new BorderLayout());
		fraction.add(nominator, BorderLayout.NORTH);
		fraction.add(denominator, BorderLayout.SOUTH);

		add(fraction);

		equals = new JLabel("=");
		equals.setFont(font);
		add(equals);

		resultLabel = new JLabel();
		resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final CompoundBorder underlinePaddingBorder = BorderFactory.createCompoundBorder(fractionLine, padding);
		final CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0), underlinePaddingBorder);
		resultLabel.setBorder(BorderFactory.createCompoundBorder(fractionLine, compoundBorder));
		resultLabel.setFont(font);
		add(resultLabel);
	}

	public void set(double alpha, int nodeCount, int componentsCount, double result) {
		final String alphaText = DECIMAL_FORMAT.format(alpha);
		alphaLabel.setText('(' + alphaText + ')');

		nominator.setText(nodeCount + " − " + componentsCount);
		denominator.setText(nodeCount + " − 1");

		resultLabel.setText(DECIMAL_FORMAT.format(result));

	}

	@Override
	public void update(Observable o, Object arg) {

	}
}
