package cz.slahora.compling.gui.analysis.assonance;

import cz.compling.CompLing;
import cz.slahora.compling.gui.model.WorkingText;

import java.util.HashMap;
import java.util.Map;

public class Selections {

	private final Map<String, Selection> selections;

	public Selections() {
		selections = new HashMap<String, Selection>();
	}

	public String[] getAllNames() {
		return selections.keySet().toArray(new String[selections.size()]);
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

	static class Selection {
		private final String name;

		private final Map<WorkingText, CompLing> texts;

		public Selection(String name) {
			this.name = name;
			this.texts = new HashMap<WorkingText, CompLing>();
		}

		public void addText(WorkingText text, CompLing compling) {
			texts.put(text, compling);
		}
	}
}
