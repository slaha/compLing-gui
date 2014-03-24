package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.AppContext;
import cz.slahora.compling.gui.model.WorkingText;

import javax.swing.*;

/**
 *
 * TODO 
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
}
