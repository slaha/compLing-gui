package cz.slahora.compling.gui.model;

import java.util.ArrayList;
import java.util.Collection;

/**
*
* TODO
*
* <dl>
* <dt>Created by:</dt>
* <dd>slaha</dd>
* <dt>On:</dt>
* <dd> 13.4.14 10:17</dd>
* </dl>
*/
public class PipeArrayList<T> extends ArrayList<T> {

	public PipeArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public PipeArrayList() {
	}

	public PipeArrayList(Collection<? extends T> c) {
		super(c);
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(get(0));
		for (int i = 1; i < size(); i++) {
			sb.append(SPLITTER).append(get(i));
		}
		return sb.toString();
	}

	public static final String SPLITTER = "|";
}
