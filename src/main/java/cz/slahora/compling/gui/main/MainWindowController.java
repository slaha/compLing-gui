package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.AppContext;
import cz.slahora.compling.gui.analysis.MultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.SingleTextAnalysis;
import cz.slahora.compling.gui.model.WorkingText;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * Interface for controller of {@code MainWindow}
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 22.3.14 11:41</dd>
 * </dl>
 */
public interface MainWindowController extends AppContext {

	java.util.List<WorkingText> openFileUsingDialog(JComponent parent);

	void removeTab(String id);

	WorkingText onTabChange(String id);

	WorkingText newEmptyTab(JComponent parent);

	<T> void  analyse(SingleTextAnalysis singleTextAnalysis);

	<T> void  analyse(MultipleTextsAnalysis multipleTextsAnalysis);

	void registerOnTabChange(OnTabSelected callback);

	void registerTabHolder(TabHolder tabHolder);

	TabPanel getPanel(String id);

	Iterable<? extends TabPanel> getAllPanels();

	String getCurrentPanelId();

	void setMainPanel(JPanel mainPanel);

	void textChanged(String newText);

	public interface OnTabSelected {
		void onTabSelected(String id);
	}
}
