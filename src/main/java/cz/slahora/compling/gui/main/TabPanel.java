package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.IconUtils;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * Tab
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 24.3.14 7:57</dd>
 * </dl>
 */
public class TabPanel extends JPanel {

	private static final int BUTTON_SIZE = 16;
	private static final Dimension BUTTON_DIMENSION = new Dimension(BUTTON_SIZE, BUTTON_SIZE);

	private static final MouseAdapter MOUSE_ADAPTER = new MouseAdapter() {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() instanceof TabPanel) {
				TabPanel panel = (TabPanel) e.getSource();
				panel.tabHolder.onTabChange(panel.getId());
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (e.getSource() instanceof TabPanel) {
				TabPanel btn = (TabPanel)e.getSource();
				btn.setBackground(new Color(139, 163, 232));
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (e.getSource() instanceof TabPanel) {
				TabPanel btn = (TabPanel)e.getSource();
				btn.setBackground(UIManager.getColor("Panel.background"));
			}
		}
	};

	private static final MouseAdapter CLOSE_BUTTON_MOUSE_ADAPTER = new MouseAdapter() {

		@Override
		public void mouseEntered(MouseEvent e) {
			if (e.getSource() instanceof JButton) {
				JButton btn = (JButton)e.getSource();
				btn.setIcon(ICON_HOVERED);
				btn.setFont(btn.getFont().deriveFont(Font.BOLD));
				e.setSource(btn.getParent());
				MOUSE_ADAPTER.mouseEntered(e);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (e.getSource() instanceof JButton) {
				JButton btn = (JButton)e.getSource();
				btn.setIcon(ICON_NORMAL);
				btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
				e.setSource(btn.getParent());
				MOUSE_ADAPTER.mouseExited(e);
			}
		}
	};
	public static final Icon ICON_NORMAL = IconUtils.getIcon(IconUtils.Icon.CLOSE);
	public static final Icon ICON_HOVERED = IconUtils.getIcon(IconUtils.Icon.CLOSE_HOVERED);

	private final JLabel nameLabel;
	private final String id;
	private final TabHolder tabHolder;
	private final ActionListener closeAction;
	private final ActionListener renameAction;
	private boolean active;

	public TabPanel(final String id, String name, final TabHolder tabHolder, final MainWindowController mainWindowController) {
		super(new GridBagLayout());

		this.id = id;
		this.tabHolder = tabHolder;

		this.nameLabel = new JLabel(name) {

			@Override
			public void paint(Graphics g) {
				if (active) {
					setFont(getFont().deriveFont(Font.BOLD));
				} else {
					setFont(getFont().deriveFont(Font.PLAIN));
				}
				super.paint(g);
			}
		};
		nameLabel.setOpaque(false);

		closeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tabHolder.onTabClose(id);
			}
		};

		JButton closeButton = new JButton(ICON_NORMAL);
		closeButton.setPreferredSize(BUTTON_DIMENSION);
		closeButton.setMaximumSize(BUTTON_DIMENSION);
		closeButton.setMinimumSize(BUTTON_DIMENSION);

		closeButton.addActionListener(closeAction);
		closeButton.setContentAreaFilled(false);
		closeButton.setOpaque(false);
		closeButton.addMouseListener(CLOSE_BUTTON_MOUSE_ADAPTER);

		setPreferredSize(new Dimension(145, 25));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		setMinimumSize(new Dimension(120, 25));

		GridBagConstraintBuilder labelConstraints = new GridBagConstraintBuilder()
			.fill(GridBagConstraints.HORIZONTAL)
			.anchor(GridBagConstraints.LINE_START)
			.gridX(0)
			.gridY(0)
			.weightX(1)
			.insets(new Insets(0, 3, 0, 3));
		add(nameLabel, labelConstraints.build());

		GridBagConstraintBuilder buttonConstraints = new GridBagConstraintBuilder()
			.anchor(GridBagConstraints.LINE_END)
			.gridX(1)
			.gridY(0)
			.insets(new Insets(0, 3, 0, 3));
		add(closeButton, buttonConstraints.build());

		setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		addMouseListener(MOUSE_ADAPTER);

		renameAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newName = MainWindowUtils.renameTabDialog(TabPanel.this, id, nameLabel.getText());
				if (newName != null) {
					mainWindowController.renameText(id, newName);
				}
			}
		};
		setComponentPopupMenu(new TabPanelPopUp());
	}

	public String getId() {
		return id;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setNewTextName(String newTextName) {
		nameLabel.setText(newTextName);
	}

	private class TabPanelPopUp extends JPopupMenu {

		public TabPanelPopUp() {
			JMenuItem rename = new JMenuItem("Přejmenovat", IconUtils.getIcon(IconUtils.Icon.RENAME));
			rename.addActionListener(renameAction);
			add(rename);

			JMenuItem close = new JMenuItem("Zavřít", IconUtils.getIcon(IconUtils.Icon.CLOSE));
			close.addActionListener(closeAction);
			add(close);
		}
	}
}
