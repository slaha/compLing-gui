package cz.slahora.compling.gui.model;

import cz.compling.model.CharacterFrequency;
import cz.compling.utils.TrooveUtils;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.javatuples.Pair;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.*;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 29.3.14 11:06</dd>
 * </dl>
 */
public class CharacterFrequencyModel {

	public static final Comparator<String> CHARACTERS_FIRST_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			boolean o1alpha = StringUtils.isAlpha(o1);
			boolean o2alpha = StringUtils.isAlpha(o2);
			if (o1alpha && o2alpha){
				return o1.compareTo(o2);
			} else if (!o1alpha && o2alpha) {
				return 1;
			} else if (o1alpha) {
				return -1;
			}
			return o1.compareTo(o2);
		}
	};

	private final Map<WorkingText, CharacterFrequency> characterFrequency;
	private final Set<String> allCharacters;
	private final SelectedCharacters selectedCharacters;
	private TObjectIntHashMap<String> sums;
	private List<String> maxOccurrences;
	private int maxOccurrence;

	public CharacterFrequencyModel(Map<WorkingText, CharacterFrequency> characterFrequency) {
		this.characterFrequency = characterFrequency;
		this.allCharacters = charactersFromAll(characterFrequency);
		selectedCharacters = new SelectedCharacters();
	}

	private Set<String> charactersFromAll(Map<WorkingText, CharacterFrequency> characterFrequency) {
		Set<String> allCharacters = new HashSet<String>();

		for (CharacterFrequency frequency : characterFrequency.values()) {
			for (Pair<String, Integer> pair : frequency.getAllByFrequency(TrooveUtils.SortOrder.DESCENDING)) {
				allCharacters.add(pair.getValue0());
			}
		}

		return allCharacters;
	}

	public TableModel getTableModel() {
		return new CharacterFrequencyTableModel(allCharacters, characterFrequency);
	}

	public int getTextsCount() {
		return characterFrequency.size();
	}

	public Iterable<WorkingText> getWorkingTexts() {
		return characterFrequency.keySet();
	}

	public int getCharactersCount() {
		return allCharacters.size();

	}

	public PieDataset getPieDataSet() {
		if (sums == null) {
			doSums();
		}
		final DefaultPieDataset dataset = new DefaultPieDataset();

		sums.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String character, int sum) {
				dataset.setValue(character, sum);
				return true;
			}
		});

		return dataset;
	}

	public CategoryDataset getBarDataSet() {
		if (sums == null) {
			doSums();
		}
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		sums.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String character, int sum) {
				dataset.setValue(sum, "Četnost", character);
				return true;
			}
		});
		return dataset;
	}

	public CategoryDataset getBarDataSetFor(String...ss) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		Arrays.sort(ss, CHARACTERS_FIRST_COMPARATOR);
		for (String s : ss) {
			for (Map.Entry<WorkingText, CharacterFrequency> entry : characterFrequency.entrySet()) {
				dataset.setValue(entry.getValue().getFrequencyFor(s), s, entry.getKey().getName());
			}
		}
		return dataset;
	}

	private void doSums() {
		if (sums != null) {
			return;
		}
		sums = new TObjectIntHashMap<String>(allCharacters.size());
		maxOccurrences = new ArrayList<String>();
		int maxOccurrence = 0;
		for (String character : allCharacters) {
			int sum = 0;

			for (CharacterFrequency frequency : characterFrequency.values()) {
				sum += frequency.getFrequencyFor(character);
			}

			sums.put(character, sum);
			if (sum == maxOccurrence) {
				maxOccurrences.add(character);
			} else if (sum > maxOccurrence) {
				maxOccurrences.clear();
				maxOccurrences.add(character);
				maxOccurrence = sum;
			}
		}
		this.maxOccurrence = maxOccurrence;
	}

	public List<String> getMostOftenCharacter() {
		if (sums == null) {
			doSums();
		}

		return maxOccurrences;
	}

	public int getMaxOccurrence() {
		return maxOccurrence;
	}

	public CsvData getCsvData() {
		CsvData csvData = new CsvData();

		//..add all characters to header
		csvData.addHeader(allCharacters);
		for (Map.Entry<WorkingText, CharacterFrequency> entry : characterFrequency.entrySet()) { //..for each text
			csvData.startNewLine();
			csvData.addData(entry.getKey().getName());//..put name as first thing in line (it is not in header now)
			for (Object o : csvData.getHeaders()) {
				int frequencyFor = entry.getValue().getFrequencyFor(o.toString());
				csvData.addData(frequencyFor);
			}
		}
		csvData.addHeader(0, "Text");

		return csvData;
	}

	public Set<String> getAllCharacters() {
		return allCharacters;
	}

	public void removeComparePlotCategory(String item) {
		selectedCharacters.remove(item);
	}

	public Set<String> getAllCompareChartCategories() {
		return selectedCharacters.getAll();
	}

	public void addCompareChartCategory(String item) {
		selectedCharacters.add(item);
	}

	public boolean isInCompareChartCategories(String item) {
		return selectedCharacters.contains(item);
	}

	private static class SelectedCharacters {

		/** Selected characters for displaying on {@code compareChartPanel} plot */
		private final Set<String> compareChartPanelStrings;
		private final List<String> notUsedStrings;

		private SelectedCharacters() {
			this.compareChartPanelStrings = new HashSet<String>() {

				@Override
				public String toString() {
					StrBuilder sb = new StrBuilder();
					sb.appendWithSeparators(this, ", ");
					return sb.toString();
				}
			};
			this.notUsedStrings = new ArrayList<String>();
		}

		public void remove(String item) {
			compareChartPanelStrings.remove(item);
			if (notUsedStrings.contains(item)) {
				//..it was selected in more combos. Now we need to put it back
				add(item);
			}
		}

		public Set<String> getAll() {
			return compareChartPanelStrings;
		}

		public void add(String item) {
			if (!compareChartPanelStrings.add(item)) {
				notUsedStrings.add(item);
			}
		}

		public boolean contains(String item) {
			return compareChartPanelStrings.contains(item);
		}
	}

	private static class CharacterFrequencyTableModel implements TableModel {

		private final List<String> rows;
		private final List<WorkingText> workingTexts;
		private final List<CharacterFrequency> characterFrequencies;

		private CharacterFrequencyTableModel(Set<String> rows,
		                                     Map<WorkingText, CharacterFrequency> characterFrequency) {

			this.rows = new ArrayList<String>(rows);
			this.workingTexts = new ArrayList<WorkingText>(characterFrequency.size());
			this.characterFrequencies = new ArrayList<CharacterFrequency>(characterFrequency.size());
			for (Map.Entry<WorkingText, CharacterFrequency> entry : characterFrequency.entrySet()) {
				workingTexts.add(entry.getKey());
				characterFrequencies.add(entry.getValue());
			}
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return characterFrequencies.size() + 1;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Znak";
			}
			//columnIndex - 1 because 0 is "Character" column
			return workingTexts.get(columnIndex - 1).getName();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return String.class;
			}
			return Integer.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String s = rows.get(rowIndex);
			if (columnIndex == 0) {
				return s;
			}
			return characterFrequencies.get(columnIndex - 1).getFrequencyFor(s);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			//TODO implement

		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			//TODO implement

		}
	}
}
