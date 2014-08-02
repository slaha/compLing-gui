package cz.slahora.compling.gui.panels.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.WorkingText;
import gnu.trove.map.TObjectIntMap;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Table model for frequencies of words in text(s)
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 2.8.14 12:00</dd>
 * </dl>
 */
public class WordFrequencyTableModel extends AbstractTableModel {

	private final List<String> words;
	private final List<WorkingText> texts;
	private final Map<WorkingText, IWordFrequency> wordFrequencies;
	private final TObjectIntMap<String> wordsToFrequencies;

	public WordFrequencyTableModel(final TObjectIntMap<String> wordsToFreq, Map<WorkingText, IWordFrequency> wordFrequencies) {
		this.wordsToFrequencies = wordsToFreq;
		this.words = new ArrayList<String>(wordsToFreq.keySet());
		this.wordFrequencies = wordFrequencies;
		this.texts = new ArrayList<WorkingText>(wordFrequencies.keySet());
	}

	private boolean hasOverview() {
		return wordFrequencies.size() > 1;
	}
	@Override
	public int getRowCount() {
		return words.size();
	}

	@Override
	public int getColumnCount() {
		//..column for word + optionally for all texts + column for each text
		return 1 + (hasOverview() ? 1 : 0) + wordFrequencies.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return words.get(rowIndex);
			case 1:
				if (!hasOverview()) {
					//..no overview panel
					return getValueForText(rowIndex, columnIndex);
				} else {
					//..overview column
					return getValueForOverview(rowIndex);
				}
			default:
				return getValueForText(rowIndex, hasOverview() ? columnIndex - 1 : columnIndex);
		}
	}



	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) {
			return "Slovo";
		}
		//columnIndex - 1 because 0 is "Character" column
		if (columnIndex == 1 && hasOverview()) {
			return "Přehled";
		}
		int minus = 1 + (hasOverview() ? 1 : 0);
		return texts.get(columnIndex - minus).getName();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return String.class;
		}
		return Integer.class;
	}

	private Object getValueForText(int rowIndex, int columnIndex) {
		final String wordOnRow = words.get(rowIndex);

		final WorkingText textInColumn = texts.get(columnIndex - 1);

		final IWordFrequency wordFrequency = wordFrequencies.get(textInColumn);
		return wordFrequency.getWordFrequency().getFrequencyFor(wordOnRow);

	}

	private Object getValueForOverview(int rowIndex) {
		final String wordOnRow = words.get(rowIndex);
		return wordsToFrequencies.get(wordOnRow);
	}
}
