package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.ui.WrapLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class TextStatusBar extends JPanel {

	private final JLabel textLength;
	private final JLabel wordCount;

	public TextStatusBar() {
		super(new WrapLayout(WrapLayout.LEFT));

		add(new JLabel("Počet znaků:"));
		textLength = new JLabel("0");
		add(textLength);

		add(new JPanel());
		add(new JLabel("Počet slov:"));
		wordCount = new JLabel("0");
		add(wordCount);
	}

	public void onTextSelected(int charLength, int wordsCount) {
		textLength.setText(String.valueOf(charLength));
		wordCount.setText(String.valueOf(wordsCount));
	}

	public void onNoText() {
		onTextSelected(0, 0);
	}
}
