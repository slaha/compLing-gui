package cz.slahora.compling.gui.main;

import cz.compling.CompLing;
import cz.slahora.compling.gui.AppContext;
import cz.slahora.compling.gui.analysis.AnalysisResultReceiver;
import cz.slahora.compling.gui.analysis.AnalysisResultReceiverImpl;
import cz.slahora.compling.gui.analysis.MultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.LastDirectory;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.model.WorkingTexts;
import cz.slahora.compling.gui.utils.FileChooserUtils;
import cz.slahora.compling.gui.utils.MapUtils;
import org.apache.commons.io.FileUtils;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

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
	private LastDirectory lastDirectory;
	private final List<OnTabSelected> onTabSelectedListeners;
	private TabHolder tabHolder;

	private final TabPanels tabPanels;
	private AnalysisResultReceiver resultReceiver;
	private JPanel mainPanel;

	public MainWindowControllerImpl(AppContext appContext, WorkingTexts workingTexts) {
		this.appContext = appContext;
		this.workingTexts = workingTexts;
		this.tabPanels = new TabPanels();
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
					WorkingText workingText = workingTexts.add(file.getName(), text);
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
	public void removeTab(String id) {
		workingTexts.remove(id);
		tabPanels.removePanel(id);
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
		String name = JOptionPane.showInputDialog(parent, "Zadejte prosím jméno nového textu", "Jméno nového textu", JOptionPane.QUESTION_MESSAGE);
		if (name != null) {
			WorkingText workingText = workingTexts.add(name, "");
			tabPanels.addPanel(workingText, tabHolder);
			notifyNewTab(Collections.singletonList(workingText));
			return workingText;
		}
		return null;
	}

	@Override
	public <T> void analyse(SingleTextAnalysis<T> analysis) {
		WorkingText text = workingTexts.get(tabPanels.getCurrentId());
		analysis.analyse(mainPanel, text.getCompLing(), text);
		T results = analysis.getResults();
		if (results == null || resultReceiver == null) {
			return;
		}
		if (resultReceiver.canReceive(results.getClass())) {
			resultReceiver.send(Collections.singletonMap(text, results));
		} else {
			throw new AnalysisResultReceiver.TypeNotSupportedException(results.getClass());
		}
	}

	@Override
	public <T> void analyse(MultipleTextsAnalysis<T> analysis) {
		Map<WorkingText, CompLing> toAnalyse = new HashMap<WorkingText, CompLing>();
		for (WorkingText workingText : workingTexts.getTexts()) {
			toAnalyse.put(workingText, workingText.getCompLing());
		}
		analysis.analyse(mainPanel, toAnalyse);
		Map<WorkingText, T> results = analysis.getResults();
		if (results == null || results.isEmpty() || resultReceiver == null) {
			return;
		}

		if (resultReceiver.canReceive(MapUtils.getValueClass(results))) {
			resultReceiver.send(results);
		} else {
			throw new AnalysisResultReceiver.TypeNotSupportedException(results.getClass());
		}

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
}
