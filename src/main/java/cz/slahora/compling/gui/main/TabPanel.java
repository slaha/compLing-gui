package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.IconUtils;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.Charset;

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

	public static final Icon ICON_NORMAL = IconUtils.getIcon(IconUtils.Icon.CLOSE);
	public static final Icon ICON_HOVERED = IconUtils.getIcon(IconUtils.Icon.CLOSE_HOVERED);

	private final JLabel nameLabel;
	private final TabHolder tabHolder;
	private final ActionListener closeAction;
	private final ActionListener renameAction;
	private final ActionListener saveAction;
	private final ActionListener loadAgainAction;
	private final TabPanelMouseAdapter tabPanelMouseAdapter;
	private final WorkingText text;

	private Font nameLabelActiveFont;
	private final Font nameLabelInactiveFont;

	private boolean active;

	public TabPanel(final WorkingText text, final TabHolder tabHolder, final MainWindowController mainWindowController) {
		super(new GridBagLayout());

		this.text = text;
		this.tabHolder = tabHolder;

		this.nameLabel = new JLabel(text.getName());
		nameLabelInactiveFont = nameLabel.getFont();
		nameLabelActiveFont = nameLabelInactiveFont.deriveFont(Font.BOLD);

		nameLabel.setOpaque(false);

		closeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tabHolder.onTabClose(text.getId());
			}
		};

		JButton closeButton = new JButton(ICON_NORMAL);
		closeButton.setPreferredSize(BUTTON_DIMENSION);
		closeButton.setMaximumSize(BUTTON_DIMENSION);
		closeButton.setMinimumSize(BUTTON_DIMENSION);

		closeButton.addActionListener(closeAction);
		closeButton.setContentAreaFilled(false);
		closeButton.setOpaque(false);
		closeButton.addMouseListener(new CloseButtonMouseAdapter(closeButton));

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
		tabPanelMouseAdapter = new TabPanelMouseAdapter();
		addMouseListener(tabPanelMouseAdapter);

		renameAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newName = MainWindowUtils.renameTabDialog(TabPanel.this, text.getId(), nameLabel.getText());
				if (newName != null) {
					mainWindowController.renameText(text.getId(), newName);
				}
			}
		};

		saveAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainWindowController.save(text.getId(), false);
			}
		};

		loadAgainAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String loadedText = FileUtils.readFileToString(text.getFile(), Charset.forName("utf-8"));
					text.setText(loadedText);
					text.onSave(text.getFile());
					mainWindowController.onTabContentChanged(text);
					updateTextName();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Nepodařilo se nahrát text ze souboru " + text.getFile(), "Chyba", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		setComponentPopupMenu(new TabPanelPopUp());
	}

	public String getId() {
		return text.getId();
	}

	public void setActive(boolean active) {
		if (this.active == active) {
			return;
		}
		this.active = active;
		if (active) {
			nameLabel.setFont(nameLabelActiveFont);
		} else {
			nameLabel.setFont(nameLabelInactiveFont);
		}
	}

	public void updateTextName() {
		String name = text.getName() + (text.isDirty() ? " *" : "");
		nameLabel.setText(name);
		TabPanelPopUp popUp = (TabPanelPopUp) getComponentPopupMenu();
		popUp.loadAgain.setEnabled(text.getFile() != null);
	}

	private class TabPanelPopUp extends JPopupMenu {

		private final JMenuItem loadAgain;

		public TabPanelPopUp() {
			JMenuItem rename = new JMenuItem("Přejmenovat", IconUtils.getIcon(IconUtils.Icon.RENAME));
			rename.addActionListener(renameAction);
			add(rename);

			JMenuItem save = new JMenuItem("Uložit", IconUtils.getIcon(IconUtils.Icon.DOCUMENT_SAVE));
			save.addActionListener(saveAction);
			add(save);

			loadAgain = new JMenuItem("Nahrát znovu", IconUtils.getIcon(IconUtils.Icon.REFRESH));
			loadAgain.addActionListener(loadAgainAction);
			add(loadAgain);

			if (text.getFile() == null) {
				loadAgain.setEnabled(false);
			}

			JMenuItem close = new JMenuItem("Zavřít", IconUtils.getIcon(IconUtils.Icon.CLOSE));
			close.addActionListener(closeAction);
			add(close);
		}
	}

	private class CloseButtonMouseAdapter extends MouseAdapter {

		private final JButton closeButton;

		public CloseButtonMouseAdapter(JButton closeButton) {
			this.closeButton = closeButton;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			closeButton.setIcon(ICON_HOVERED);
			closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD));
			tabPanelMouseAdapter.mouseEntered(null);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			closeButton.setIcon(ICON_NORMAL);
			closeButton.setFont(closeButton.getFont().deriveFont(Font.PLAIN));
			tabPanelMouseAdapter.mouseExited(null);
		}
	}

	private class TabPanelMouseAdapter extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			tabHolder.onTabChange(getId());
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			setBackground(new Color(139, 163, 232));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			setBackground(UIManager.getColor("Panel.background"));
		}
	};
}
