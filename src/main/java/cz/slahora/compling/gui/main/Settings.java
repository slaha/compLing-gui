package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.settings.SettingsManager;

import javax.swing.*;
import java.awt.event.*;

public class Settings extends JDialog {
	private final MainWindowController controller;
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JSpinner fontSizeSpinner;
	private SettingsManager settingsManager;

	public Settings(MainWindowController controller) {
		this.controller = controller;
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		settingsManager = SettingsManager.getInstance();

		fontSizeSpinner.setModel(new SpinnerNumberModel(settingsManager.getFontSize(), 0.5d, 10d, 0.1d));

		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void onOK() {

		final Object value = fontSizeSpinner.getValue();
		if (value instanceof Number) {
			settingsManager.writeFontSize(((Number) value).doubleValue());
			controller.settingsChanged();
		}


		dispose();
	}

	private void onCancel() {
		dispose();
	}
}
