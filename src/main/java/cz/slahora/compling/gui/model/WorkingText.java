package cz.slahora.compling.gui.model;

import cz.compling.CompLing;

import java.util.UUID;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 23.3.14 20:38</dd>
 * </dl>
 */
public class WorkingText {
	private static final String TXT_SUFFIX = ".txt";
	private static final int TXT_SUFFIX_LENGTH = TXT_SUFFIX.length();

	private final String name;
	private String text;
	private String id;

	public WorkingText(String name, String text) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.text = text;
	}

	public String getName() {
		if (name.length() > TXT_SUFFIX_LENGTH && name.endsWith(TXT_SUFFIX)) {
			return name.substring(0, name.length() - TXT_SUFFIX_LENGTH);
		}

		return name;
	}

	public String getText() {
		return text;
	}

	public String getId() {
		return id;
	}

	public CompLing getCompLing() {
		return CompLing.getInstance(text);
	}

	public void setText(String newText) {
		text = newText;
	}
}
