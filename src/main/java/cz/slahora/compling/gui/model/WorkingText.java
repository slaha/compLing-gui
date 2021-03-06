package cz.slahora.compling.gui.model;

import cz.compling.CompLing;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.UUID;

/**
 *
 * One opened text
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

	private final String id;
	private File file;

	private String name;
	private String text;

	private boolean textChanged;

	public WorkingText(String name, String text, File file) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.text = text;
		this.file = file;
	}

	public String getName() {
		if (name.length() > TXT_SUFFIX_LENGTH && StringUtils.endsWithIgnoreCase(name, TXT_SUFFIX)) {
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
		textChanged = true;
	}

	@Override
	public String toString() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFile() {
		return file;
	}

	public void onSave(File file) {
		this.file = file;
		textChanged = false;
	}

	public boolean isDirty() {
		return textChanged;
	}

}
