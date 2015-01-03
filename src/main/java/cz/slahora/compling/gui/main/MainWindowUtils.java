package cz.slahora.compling.gui.main;

import org.apache.commons.lang3.StringUtils;

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

		return validate(newName, oldName, parent);
	}

	private static String validate(String name, String oldName, Component parent) {
		if (name == null) {
			return null;
		}
		if (StringUtils.isBlank(name.trim())) {
			JOptionPane.showMessageDialog(parent, "Jméno nového textu nesmí být prázdné", "Chyba", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if (oldName != null && oldName.equals(name)) {
			return null;
		}
		return name;
	}

	public static String enterTabDialog(Component parent) {
		String name = JOptionPane.showInputDialog(parent, "Zadejte prosím jméno nového textu", "Jméno nového textu", JOptionPane.QUESTION_MESSAGE);
		return validate(name, null, parent);
	}
}
