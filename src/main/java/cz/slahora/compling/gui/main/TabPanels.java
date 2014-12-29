package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.model.WorkingText;

import java.util.*;

/**
 *
 * Holder of {@code TabPanel}s
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 24.3.14 22:50</dd>
 * </dl>
 */
public class TabPanels {

	private final Map<String, TabPanel> panels;
	private final Stack<String> stack;
	private final MainWindowController mainWindowController;
	private TabPanel currentPanel;

	public TabPanels(MainWindowController mainWindowController) {
		this.mainWindowController = mainWindowController;
		panels = new LinkedHashMap<String, TabPanel>();
		this.stack = new Stack<String>();
	}

	public void addPanel(WorkingText text, TabHolder tabHolder) {
		TabPanel panel = new TabPanel(text.getId(), text.getName(), tabHolder, mainWindowController);
		panels.put(text.getId(), panel);
	}

	public void removePanel(String id) {
		panels.remove(id);
		stack.remove(id);
		if (currentId(id)) {
			pickNewCurrent();
		}
	}

	public TabPanel getPanel(String id) {
		return panels.get(id);
	}

	public Collection<TabPanel> getAll() {
		return panels.values();
	}

	public boolean currentId(String id) {
		return id.equals(currentPanel.getId());
	}

	public void setCurrent(TabPanel current) {
		if (this.currentPanel != null) {
			this.currentPanel.setActive(false);
		}
		this.currentPanel = current;
		if (this.currentPanel != null) {
			this.stack.add(current.getId());
			this.currentPanel.setActive(true);
		}
	}

	public String getCurrentId() {
		if (currentPanel == null) {
			return pickNewCurrent();
		}
		return currentPanel.getId();
	}

	private String pickNewCurrent() {
		if (stack.empty()) {
			Iterator<TabPanel> iterator = panels.values().iterator();
			if (iterator.hasNext()) {
				this.currentPanel = iterator.next();
			} else {
				this.currentPanel = null;
			}
		} else {
			String id = stack.pop();
			this.currentPanel = getPanel(id);
		}
		return currentPanel != null ? currentPanel.getId() : null;
	}
}
