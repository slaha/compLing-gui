package cz.slahora.compling.gui.model;

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
	private final String name;
	private final String text;
	private String id;

	public WorkingText(String name, String text) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.text = text;
	}

	public String getName() {
		return name;
	}

	public String getText() {
		return text;
	}

	public String getId() {
		return id;
	}
}
