package cz.slahora.compling.gui.analysis;

import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.Action;
import javax.swing.JButton;
import java.awt.Insets;

/**
*
* TODO
*
* <dl>
* <dt>Created by:</dt>
* <dd>slaha</dd>
* <dt>On:</dt>
* <dd> 24.8.14 12:28</dd>
* </dl>
*/
public class ToggleHeader extends JButton {

	public ToggleHeader(JXCollapsiblePane panel, String text) {
		this(panel.getActionMap().get("toggle"), text);
	}

	public ToggleHeader(Action action, String text) {
		setHideActionText(true);
		setFocusPainted(false);
		setMargin(new Insets(0, 0, 0, 0));
		setContentAreaFilled(false);
		setBorderPainted(false);
		setOpaque(false);
		setAction(action);
		setText(text);
		setAlignmentX(LEFT);
	}
}
