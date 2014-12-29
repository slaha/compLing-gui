package cz.slahora.compling.gui.main;

import javax.swing.JOptionPane;
import java.awt.Component;

public class MainWindowUtils {

	public static String renameTabDialog(Component parent, String currentPanelId, String oldName) {
		if (currentPanelId == null) {
			JOptionPane.showMessageDialog(parent,
				"Žádný text není zvolen",
				"Chyba",
				JOptionPane.ERROR_MESSAGE);
			return null;
		}

		String newName = (String) JOptionPane.showInputDialog(parent,
			"Zvolte nové jméno pro text '" + oldName + "'",
			"Přejmenování textu",
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			oldName
		);
		if (newName == null || newName.equals(oldName)) {
			return null;
		}
		return newName;

	}
}
