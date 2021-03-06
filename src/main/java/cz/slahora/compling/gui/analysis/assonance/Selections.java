package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.CompLing;
import cz.slahora.compling.gui.model.WorkingText;

import java.util.*;

public class Selections {

	private final Map<String, Selection> selections;
	private String[] allNames;

	public Selections() {
		selections = new LinkedHashMap<String, Selection>();
	}

	public String[] getAllNames() {
		if (allNames == null || allNames.length != selections.size()) {
			allNames = selections.keySet().toArray(new String[selections.size()]);
		}
		return allNames;
	}

	public void addNewName(String name) {
		selections.put(name, new Selection(name));
	}

	public void addTo(String name, WorkingText text, CompLing compling) {
		Selection selection = selections.get(name);
		if (selection == null) {

			selection = new Selection(name);
			selections.put(name, selection);
		}

		selection.addText(text, compling);
	}

	public boolean removeFrom(String group, WorkingText workingText) {
		final Selection selection = selections.get(group);
		if (selection == null) {
			return false;
		}
		return removeFrom(selection, workingText);
	}

	private boolean removeFrom(Selection selection, WorkingText workingText) {
		return selection.removeText(workingText) != null;
	}

	public int getGroupsCount() {
		return selections.size();
	}

	public Selection getGroup(String groupName) {
		return selections.get(groupName);

	}

	public List<WorkingText> removeGroup(String group) {
		final Selection selection = selections.remove(group);
		if (selection == null) {
			return Collections.emptyList();
		}
		List<WorkingText> list = new ArrayList<WorkingText>();
		final List<WorkingText> workingTexts = new ArrayList<WorkingText>(selection.texts.keySet());
		for (WorkingText text : workingTexts) {
			if (removeFrom(selection, text)) {
				list.add(text);
			}
		}
		return list;
	}

	static class Selection implements Iterable<Map.Entry<WorkingText, CompLing>> {
		private final String name;

		private final Map<WorkingText, CompLing> texts;

		public Selection(String name) {
			this.name = name;
			this.texts = new HashMap<WorkingText, CompLing>();
		}

		public void addText(WorkingText text, CompLing compling) {
			texts.put(text, compling);
		}

		public CompLing removeText(WorkingText text) {
			return texts.remove(text);
		}

		@Override
		public Iterator<Map.Entry<WorkingText, CompLing>> iterator() {
			return texts.entrySet().iterator();
		}

		public int getSize() {
			return texts.size();
		}
	}
}
