package cz.slahora.compling.gui.analysis;

import cz.slahora.compling.gui.utils.IconUtils;
import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public class ToggleHeader extends JButton implements ActionListener {

	private static final String EXPAND = "Rozbalit";
	private static final String COLLAPSE = "Sbalit";

	private final JXCollapsiblePane panel;

	public ToggleHeader(JXCollapsiblePane panel, String text) {
		this.panel = panel;
		setHorizontalTextPosition(SwingConstants.LEFT);
		setHorizontalAlignment(SwingConstants.LEFT);
		setHideActionText(true);
		setFocusPainted(false);
		setMargin(new Insets(0, 0, 0, 0));
		setContentAreaFilled(false);
		setBorderPainted(false);
		setOpaque(false);
		setAlignmentX(LEFT);
		setText(text);
		setBackground(Color.white);
		setIcon(IconUtils.getIcon(panel.isCollapsed() ? IconUtils.Icon.RIGHT : IconUtils.Icon.DOWN));
		setIconTextGap(10);
		addActionListener(this);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setToolTipText(panel.isCollapsed() ? EXPAND : COLLAPSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Action toggle = panel.getActionMap().get("toggle");
		toggle.actionPerformed(e);
		setIcon(IconUtils.getIcon(panel.isCollapsed() ? IconUtils.Icon.RIGHT : IconUtils.Icon.DOWN));
		setToolTipText(panel.isCollapsed() ? EXPAND : COLLAPSE);
		invalidate();
		repaint();
	}
}
