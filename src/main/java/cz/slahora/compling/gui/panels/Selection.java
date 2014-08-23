package cz.slahora.compling.gui.panels;

import org.apache.commons.lang3.text.StrBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Selection<T> {

	/** Selected characters for displaying on {@code compareChartPanel} plot */
	private final Set<T> compareChartPanelStrings;
	private final List<T> notUsedStrings;

	public Selection() {
		this.compareChartPanelStrings = new HashSet<T>() {

			@Override
			public String toString() {
				StrBuilder sb = new StrBuilder();
				sb.appendWithSeparators(this, ", ");
				return sb.toString();
			}
		};
		this.notUsedStrings = new ArrayList<T>();
	}

	public void remove(T item) {
		compareChartPanelStrings.remove(item);
		if (notUsedStrings.contains(item)) {
			//..it was selected in more combos. Now we need to put it back
			add(item);
			notUsedStrings.remove(item);
		}
	}

	public Set<T> getAll() {
		return compareChartPanelStrings;
	}

	public void add(T item) {
		if (!compareChartPanelStrings.add(item)) {
			notUsedStrings.add(item);
		}
	}

	public boolean contains(T item) {
		return compareChartPanelStrings.contains(item);
	}
}
