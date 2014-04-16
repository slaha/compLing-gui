package cz.slahora.compling.gui.utils;

import org.apache.commons.lang3.StringUtils;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;

/**
 *
 * Utilities for working with {@code JFileChooser}
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 8:41</dd>
 * </dl>
 */
public class FileChooserUtils {

	public static File[] getFilesToOpen(File startDirectory, Component parent, String...filters) {
		JFileChooser chooser = new JFileChooser(startDirectory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Soubory " + toString(filters), filters);
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(true);
		int returnValue = chooser.showOpenDialog(parent);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFiles();
		}

		return new File[0];
	}

	public static File getFileToOpen(File startDirectory, Component parent, String...filters) {
		JFileChooser chooser = new JFileChooser(startDirectory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Soubory " + toString(filters), filters);
		chooser.setFileFilter(filter);
		int returnValue = chooser.showOpenDialog(parent);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}

		return null;
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
		StringBuilder sb = new StringBuilder();
		sb.append(filters[0]);
		for (int i = 1; i < filters.length; i++) {
			sb
				.append(", ")
				.append(filters[1]);
		}
		return sb.toString();
	}

	private static boolean hasExtension(File selectedFile, String[] filters) {
		final String fileName = selectedFile.getName();
		return StringUtils.endsWithAny(fileName, filters);
	}

}
