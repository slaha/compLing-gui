package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
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

	private static final int BUTTON_SIZE = 21;
	private static final Dimension BUTTON_DIMENSION = new Dimension(BUTTON_SIZE, BUTTON_SIZE);
	private static final Color CLOSE_BUTTON_NORMAL_COLOR = new Color(200, 0, 0);
	private static final Color CLOSE_BUTTON_HOVER_COLOR =  new Color(128, 0, 0);

	private static final ActionListener CLOSE_LISTENER = new ActionListener() {
		@Override
		public void actionPerformed(final ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			TabPanel panel = (TabPanel) ((JButton) e.getSource()).getParent();
			panel.tabHolder.onTabClose(panel.id);
		}
		}
	};

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
				btn.setBackground(CLOSE_BUTTON_HOVER_COLOR);
				btn.setFont(btn.getFont().deriveFont(Font.BOLD));
				e.setSource(btn.getParent());
				MOUSE_ADAPTER.mouseEntered(e);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (e.getSource() instanceof JButton) {
				JButton btn = (JButton)e.getSource();
				btn.setBackground(CLOSE_BUTTON_NORMAL_COLOR);
				btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
				e.setSource(btn.getParent());
				MOUSE_ADAPTER.mouseExited(e);
			}
		}
	};

	private final JLabel nameLabel;
	private final JButton closeButton;
	private final String id;
	private final TabHolder tabHolder;
	private boolean active;

	public TabPanel(String id, String name, TabHolder tabHolder) {
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

		this.closeButton = new JButton("\u00D7");
		closeButton.setPreferredSize(BUTTON_DIMENSION);
		closeButton.setMaximumSize(BUTTON_DIMENSION);
		closeButton.setMinimumSize(BUTTON_DIMENSION);
		closeButton.addActionListener(CLOSE_LISTENER);
		closeButton.setContentAreaFilled(false);
		closeButton.setOpaque(true);
		closeButton.setBackground(CLOSE_BUTTON_NORMAL_COLOR);
		closeButton.setForeground(Color.white);
		closeButton.addMouseListener(CLOSE_BUTTON_MOUSE_ADAPTER);

		setPreferredSize(new Dimension(145, 25));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		setMinimumSize(new Dimension(120, 25));

		GridBagConstraintBuilder labelConstraints = new GridBagConstraintBuilder()
			.fill(GridBagConstraints.HORIZONTAL)
			.anchor(GridBagConstraints.LINE_START)
			.gridx(0)
			.gridy(0)
			.weightx(1)
			.insets(new Insets(0, 3, 0, 3));
		add(nameLabel, labelConstraints.build());

		GridBagConstraintBuilder buttonConstraints = new GridBagConstraintBuilder()
			.anchor(GridBagConstraints.LINE_END)
			.gridx(1)
			.gridy(0)
			.insets(new Insets(0, 3, 0, 3));
		add(closeButton, buttonConstraints.build());

		setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		addMouseListener(MOUSE_ADAPTER);
	}

	public String getId() {
		return id;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
