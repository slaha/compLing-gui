package cz.slahora.compling.gui;

import javax.swing.JTextArea;
import java.awt.Dimension;

/**
 *
 * TODO 
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
}
