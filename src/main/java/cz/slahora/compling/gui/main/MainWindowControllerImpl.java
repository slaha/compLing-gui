package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.AppContext;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.model.WorkingTexts;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 22.3.14 11:43</dd>
 * </dl>
 */
public class MainWindowControllerImpl implements MainWindowController {

	private final AppContext appContext;
	private final WorkingTexts workingTexts;

	private final static Charset UTF8_CHARSET = Charset.forName("utf-8");
	private File lastDirectory;

	public MainWindowControllerImpl(AppContext appContext, WorkingTexts workingTexts) {
		this.appContext = appContext;
		this.workingTexts = workingTexts;
	}

	@Override
	public List<WorkingText> openFileUsingDialog(JComponent parent) {

		JFileChooser chooser = new JFileChooser(lastDirectory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Textové soubory", "txt");
        chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(true);
		int returnValue = chooser.showOpenDialog(parent);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File[] files = chooser.getSelectedFiles();
			List<WorkingText> newTexts = new ArrayList<WorkingText>(files.length);
			for (File file : files) {
				try {
					String text = FileUtils.readFileToString(file, UTF8_CHARSET);
					newTexts.add(workingTexts.add(file.getName(), text));
				} catch (IOException e) {

				}
			}
			if (files.length > 0) {
				this.lastDirectory = files[0].getParentFile();
			}
			return newTexts;
		}
		return Collections.emptyList();
	}

	@Override
	public void removeTab(String id) {
		workingTexts.remove(id);
	}

	@Override
	public WorkingText onTabChange(String id) {
		return workingTexts.get(id);
	}

	@Override
	public WorkingText newEmptyTab(JComponent parent) {
		String name = JOptionPane.showInputDialog(parent, "Zadejte prosím jméno nového textu", "Jméno nového textu", JOptionPane.QUESTION_MESSAGE);
		if (name != null) {
			return workingTexts.add(name, "");
		}
		return null;
	}

	@Override
	public void exit(int code) {
		appContext.exit(code);
	}
}
