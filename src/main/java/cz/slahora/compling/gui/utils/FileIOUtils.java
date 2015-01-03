package cz.slahora.compling.gui.utils;

import cz.slahora.compling.gui.model.LastDirectory;

import javax.swing.JOptionPane;
import java.awt.Container;
import java.io.File;
import java.io.IOException;

public class FileIOUtils {
	public static boolean checkFile(File file, Container parent, boolean skipFileExists) {
		if (file == null) {
			return false;
		} else if (!skipFileExists && file.exists()) {
			int i = JOptionPane.showConfirmDialog(parent, "Soubor " + file.getName() + " již existuje. Chcete jej přepsat?", "Soubor již existuje", JOptionPane.YES_NO_OPTION);
			if (i != JOptionPane.YES_OPTION) {
				return false;
			}
		}
		return true;
	}

	public static void perform(IoOperation ioOperation, Container parent) {
		try {
			ioOperation.perform();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(parent, "Chyba při ukládání souboru " + ioOperation.file.getName(), "Chyba", JOptionPane.ERROR_MESSAGE);
		} finally {
			final LastDirectory lastDirectory = LastDirectory.getInstance();
			lastDirectory.setLastDirectory(ioOperation.file.getParentFile());
		}

	}

	public static abstract class IoOperation {

		public final File file;

		public IoOperation(File file) {
			if (file == null) {
				throw new NullPointerException("file == null");
			}
			this.file = file;
		}

		public abstract void perform() throws IOException;
	}
}
