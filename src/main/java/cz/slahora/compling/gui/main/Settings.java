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
	private JSpinner graphNodeSize;
	private JSpinner graphFontSize;
	private JSpinner graphStrokeWidth;
	private SettingsManager settingsManager;

	public Settings(MainWindowController controller) {
		this.controller = controller;
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		settingsManager = SettingsManager.getInstance();

		fontSizeSpinner.setModel(new SpinnerNumberModel(settingsManager.getFontSize(), 0.5d, 10d, 0.1d));

		graphNodeSize.setModel(new SpinnerNumberModel(settingsManager.getGraphNodeSize(), 5, 100, 1));
		graphFontSize.setModel(new SpinnerNumberModel(settingsManager.getGraphNodeTextSize(), 5, 100, 1));
		graphStrokeWidth.setModel(new SpinnerNumberModel(settingsManager.getGraphStrokeWidth(), 1, 20, 1));

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

		Number value = (Number) fontSizeSpinner.getValue();
		if (settingsManager.writeFontSize(value.doubleValue())) {
			controller.settingsChanged();
		}

		value = (Number) graphNodeSize.getValue();
		settingsManager.writeGraphNodeSize(value.intValue());

		value = (Number) graphFontSize.getValue();
		settingsManager.writeGraphNodeTextSize(value.intValue());

		value = (Number) graphStrokeWidth.getValue();
		settingsManager.writeGraphStrokeWidth(value.intValue());

		dispose();
	}

	private void onCancel() {
		dispose();
	}
}
