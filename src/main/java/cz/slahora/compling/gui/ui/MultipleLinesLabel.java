package cz.slahora.compling.gui.ui;

import javax.swing.JTextArea;
import java.awt.Dimension;

/**
 *
 * Label (actually {@link JTextArea}) which can display multiple lines of text and wraps its content
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 23.8.14 12:52</dd>
 * </dl>
 */
public class MultipleLinesLabel extends JTextArea{

	public MultipleLinesLabel() {
		adjustUI();
	}

	private void adjustUI() {
		this.setEditable(false);
		this.setCursor(null);
		this.setOpaque(false);
		this.setFocusable(false);
		this.setLineWrap(true);
		this.setMinimumSize(new Dimension(10, 10));
		this.setWrapStyleWord(true);

	}

	public MultipleLinesLabel(String text) {
		super(text);
		adjustUI();
	}

	public MultipleLinesLabel(String text, Object...args) {
		super(String.format(getDefaultLocale(), text, args));
		adjustUI();
	}
}
