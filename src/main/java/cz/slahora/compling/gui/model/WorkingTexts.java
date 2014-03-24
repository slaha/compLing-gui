package cz.slahora.compling.gui.model;

import java.util.*;

/**
 *
 * TODO
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 23.3.14 9:31</dd>
 * </dl>
 */
public class WorkingTexts {

	private final Map<String, WorkingText> texts;

	public WorkingTexts() {
		this.texts = new LinkedHashMap<String, WorkingText>();
	}

	public WorkingText add(String name, String text) {
		WorkingText workingText = new WorkingText(name, text);
		texts.put(workingText.getId(), workingText);
		return workingText;
	}

	public Collection<WorkingText> getTexts() {
		return texts.values();
	}

	public WorkingText get(String id) {
		WorkingText workingText = texts.get(id);
		if (workingText == null) {
			throw new IllegalArgumentException("Text with id " + id + " not found");
		}
		return workingText;
	}

	public void remove(String id) {
		texts.remove(id);
	}
}
