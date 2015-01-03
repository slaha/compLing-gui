package cz.slahora.compling.gui.main;

import cz.compling.CompLing;
import cz.slahora.compling.gui.AppContext;
import cz.slahora.compling.gui.analysis.*;
import cz.slahora.compling.gui.model.LastDirectory;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.model.WorkingTexts;
import cz.slahora.compling.gui.utils.FileChooserUtils;
import cz.slahora.compling.gui.utils.FileIOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 *
 * Implementation of Main Window Controller
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
	private LastDirectory lastDirectory;
	private final List<OnTabSelected> onTabSelectedListeners;
	private TabHolder tabHolder;

	private final TabPanels tabPanels;
	private AnalysisResultReceiver resultReceiver;
	private JPanel mainPanel;

	private final ResultsHandler RESULTS_HANDLER = new ResultsHandler() {

		@Override
		public void handleResult(Analysis analysis) {
			handleResults(analysis.getResults());
		}
	};

	public MainWindowControllerImpl(AppContext appContext, WorkingTexts workingTexts) {
		this.appContext = appContext;
		this.workingTexts = workingTexts;
		this.tabPanels = new TabPanels(this);
		this.lastDirectory = LastDirectory.getInstance();
		this.resultReceiver = new AnalysisResultReceiverImpl();
		this.onTabSelectedListeners = new ArrayList<OnTabSelected>();
	}

	@Override
	public List<WorkingText> openFileUsingDialog(JComponent parent) {

		File[] files= FileChooserUtils.getFilesToOpen(lastDirectory.getLastDirectory(), parent, "txt");
		if (files.length > 0) {
			List<WorkingText> newTexts = new ArrayList<WorkingText>(files.length);
			for (File file : files) {
				try {
					String text = FileUtils.readFileToString(file, UTF8_CHARSET);
					WorkingText workingText = workingTexts.add(file.getName(), text, file);
					newTexts.add(workingText);
					tabPanels.addPanel(workingText, tabHolder);
				} catch (IOException e) {

				}
			}
			this.lastDirectory.setLastDirectory(files[0].getParentFile());

			notifyNewTab(newTexts);
			return newTexts;
		}
		return Collections.emptyList();
	}

	@Override
	public boolean removeTab(String id) {
		if (workingTexts.get(id).isDirty()) {
			int option = JOptionPane.showConfirmDialog(mainPanel,
				"Text byl změněn. Chcete změny uložit?",
				"Uložit změny",
				JOptionPane.YES_NO_CANCEL_OPTION);
			if (option == JOptionPane.CANCEL_OPTION) {
				return false;
			} else if (option == JOptionPane.YES_OPTION) {
				save(id, false);
			}
		}
		workingTexts.remove(id);
		tabPanels.removePanel(id);
		return true;
	}

	@Override
	public WorkingText onTabChange(String id) {
		this.tabPanels.setCurrent(id != null ? tabPanels.getPanel(id) : null);
		notifyTabChanged();
		if (id == null) {
			return null;
		}
		return workingTexts.get(id);
	}

	

	@Override
	public WorkingText newEmptyTab(JComponent parent) {
		String name = MainWindowUtils.enterTabDialog(parent);
		if (name != null) {
			WorkingText workingText = workingTexts.add(name, "", null);
			tabPanels.addPanel(workingText, tabHolder);
			notifyNewTab(Collections.singletonList(workingText));
			return workingText;
		}
		return null;
	}

	private void analyse(Analysis analysis, Map<WorkingText, CompLing> toAnalyse) {
		analysis.analyse(mainPanel, RESULTS_HANDLER, toAnalyse);
	}

	private void handleResults(Results results) {
		if (!results.resultsOk()) {
			return;
		}
		if (resultReceiver == null) {
			throw new IllegalStateException("No results receiver registered");
		}

		resultReceiver.send(results);
	}

	@Override
	public <T> void analyse(SingleTextAnalysis analysis) {
		WorkingText text = workingTexts.get(tabPanels.getCurrentId());
		analyse(analysis, Collections.singletonMap(text, text.getCompLing()));
	}

	@Override
	public <T> void analyse(MultipleTextsAnalysis analysis) {
		Map<WorkingText, CompLing> toAnalyse = new HashMap<WorkingText, CompLing>();
		for (WorkingText workingText : workingTexts.getTexts()) {
			toAnalyse.put(workingText, workingText.getCompLing());
		}
		analyse(analysis, toAnalyse);
	}

	@Override
	public void registerOnTabChange(OnTabSelected callback) {
		this.onTabSelectedListeners.add(callback);
	}

	@Override
	public void registerTabHolder(TabHolder tabHolder) {
		this.tabHolder = tabHolder;

	}

	@Override
	public TabPanel getPanel(String id) {
		return tabPanels.getPanel(id);
	}

	@Override
	public Iterable<? extends TabPanel> getAllPanels() {
		return tabPanels.getAll();
	}

	@Override
	public String getCurrentPanelId() {
		return tabPanels.getCurrentId();
	}

	@Override
	public void setMainPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;

	}

	@Override
	public void textChanged(String newText) {
		workingTexts.get(getCurrentPanelId()).setText(newText);
		tabPanels.getPanel(getCurrentPanelId()).updateTextName();
	}

	@Override
	public void renameText(String id, String newName) {
		workingTexts.get(id).setName(newName);
		tabPanels.getPanel(id).updateTextName();
		tabHolder.onTabChange(id);
	}

	@Override
	public void save(final String id, boolean saveAs) {
		final WorkingText text = workingTexts.get(id);

		File file = text.getFile();
		if (saveAs || file == null) {
			File dir = LastDirectory.getInstance().getLastDirectory();
			file = FileChooserUtils.getFileToSave(dir, mainPanel, "txt");
		}

		if (FileIOUtils.checkFile(file, mainPanel, !saveAs)) { //..don't notify file exists when saving existing file
			FileIOUtils.perform(new FileIOUtils.IoOperation(file) {
				@Override
				public void perform() throws IOException {
					IOUtils.write(text.getText(), new FileOutputStream(file), cz.slahora.compling.gui.utils.FileUtils.UTF8);
					text.onSave(file);
					tabPanels.getPanel(id).updateTextName();
				}
			}, mainPanel);
		}
	}

	@Override
	public void onTabContentChanged(WorkingText text) {
		if (text.getId().equals(getCurrentPanelId())) {
			tabHolder.onTabChange(getCurrentPanelId());
		}
	}

	private void notifyNewTab(List<WorkingText> newTexts) {
		if (tabHolder != null) {
			List<String> ids = new ArrayList<String>();
			for (WorkingText text : newTexts) {
				ids.add(text.getId());
			}
			tabHolder.onNewTab(ids);
		}
	}

	private void notifyTabChanged() {
		for (OnTabSelected onTabSelectedListener : onTabSelectedListeners) {
			onTabSelectedListener.onTabSelected(tabPanels.getCurrentId());
		}
	}

	@Override
	public void exit(int code) {
		appContext.exit(code);
	}

	@Override
	public void settingsChanged() {
		appContext.settingsChanged();
	}


}
