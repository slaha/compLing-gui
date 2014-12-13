package cz.slahora.compling.gui.ui.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.WorkingText;
import gnu.trove.map.TIntIntMap;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WordLengthFrequencyTableModel extends AbstractTableModel {

	private final TIntIntMap lengthToFrequencies;
	private final Map<WorkingText, IWordFrequency> wordFrequencies;
	private final int[] frequencies;
	private final List<WorkingText> texts;

	public WordLengthFrequencyTableModel(TIntIntMap lengthToFrequencies, Map<WorkingText, IWordFrequency> wordFrequencies) {
		this.lengthToFrequencies = lengthToFrequencies;
		this.wordFrequencies = wordFrequencies;
		this.texts = new ArrayList<WorkingText>(wordFrequencies.keySet());
		frequencies = lengthToFrequencies.keys();
		Arrays.sort(frequencies);
	}

	private boolean hasOverview() {
		return wordFrequencies.size() > 1;
	}

	@Override
	public int getRowCount() {
		return lengthToFrequencies.size();
	}

	@Override
	public int getColumnCount() {
		int count = 1 + wordFrequencies.size();
		if (hasOverview()) {
			count++;
		}
		return count;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
			case 0:
				return "Délka slova";
			default:
				if (hasOverview()) {
					if (column == 1) {
						return "Přehled";
					}
					column--;
				}
				column--;
				return texts.get(column).getName();
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return frequencies[rowIndex];
			case 1:
				if (hasOverview()) {
					return getValueOverview(rowIndex);
				} else {
					return getValue(rowIndex, columnIndex - 1);
				}
			default:
				if (hasOverview()) {
					columnIndex--;
				}
				return getValue(rowIndex, columnIndex - 1);
		}
	}

	private Object getValue(int rowIndex, int columnIndex) {
		final int freqOnRow = frequencies[rowIndex];

		final WorkingText textInColumn = texts.get(columnIndex);
		final IWordFrequency wordFrequency = wordFrequencies.get(textInColumn);

		return wordFrequency.getWordFrequency().getFrequencyFor(freqOnRow);
	}

	private Object getValueOverview(int rowIndex) {
		final int key = frequencies[rowIndex];
		return lengthToFrequencies.get(key);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return Integer.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {	}
}
