package cz.slahora.compling.gui.model;

import java.util.ArrayList;
import java.util.Collection;

public class StringableArrayList<T> extends ArrayList<T> {

	private final String delimeter;

	public StringableArrayList(int initialCapacity, String delimeter) {
		super(initialCapacity);
		this.delimeter = delimeter;
	}

	public StringableArrayList(String delimeter) {
		this.delimeter = delimeter;
	}

	public StringableArrayList(Collection<? extends T> c, String delimeter) {
		super(c);
		this.delimeter = delimeter;
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(get(0));
		for (int i = 1; i < size(); i++) {
			sb.append(delimeter).append(get(i));
		}
		return sb.toString();
	}
}
