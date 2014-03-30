package cz.slahora.compling.gui.utils;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 8:41</dd>
 * </dl>
 */
public class FileChooserUtils {

	public static File[] getFilesToOpen(File startDirectory, Component parent) {
		JFileChooser chooser = new JFileChooser(startDirectory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Textové soubory", "txt");
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(true);
		int returnValue = chooser.showOpenDialog(parent);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFiles();
		}

		return new File[0];
	}

	public static File getFileToSave(File startDirectory, Component parent, String...filters) {
		JFileChooser chooser = new JFileChooser(startDirectory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Soubory " + toString(filters), filters);
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(false);
		int returnValue = chooser.showSaveDialog(parent);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			if (!hasExtension(selectedFile, filters)) {
				return new File(selectedFile.getParent(), selectedFile.getName() + "." + filters[0]);
			}
			return selectedFile;
		}

		return null;
	}

	private static String toString(String[] filters) {
		StrBuilder sb = new StrBuilder();
		sb.appendWithSeparators(filters, ", ");
		return sb.toString();
	}

	private static boolean hasExtension(File selectedFile, String[] filters) {
		final String fileName = selectedFile.getName();
		return StringUtils.endsWithAny(fileName, filters);
	}

}
