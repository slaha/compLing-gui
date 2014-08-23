package cz.slahora.compling.gui.model;

import cz.compling.model.CharacterFrequency;
import cz.compling.utils.TrooveUtils;
import cz.slahora.compling.gui.panels.Selection;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.text.DecimalFormat;
import java.util.*;

public class CharacterFrequencyModel implements Csv<CharacterFrequencyModel> {



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
	private final Selection<String> selectedCharacters;
	private TObjectIntHashMap<String> sums;
	private List<String> maxOccurrences;
	private int maxOccurrence;
	private int allCharactersInAllTexts;
	private TObjectDoubleHashMap<String> relativeFrequencies;

	private double odchylka;

	public CharacterFrequencyModel(Map<WorkingText, CharacterFrequency> characterFrequency) {
		this.characterFrequency = characterFrequency;
		this.allCharacters = charactersFromAll(characterFrequency);
		selectedCharacters = new Selection<String>();
		this.odchylka = 0.005;
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
		if (sums == null) {
			doSums();
		}
		if (relativeFrequencies == null) {
			doRelativeFrequencies();
		}
		return new CharacterFrequencyTableModel(allCharacters, characterFrequency, relativeFrequencies);
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

	public int getTotalCharactersCount() {
		if (sums == null) {
			doSums();
		}
		return allCharactersInAllTexts;
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

	public CategoryDataset getAbsoluteBarDataSet() {
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

	public CategoryDataset getRelativeBarDataSet() {
		if (relativeFrequencies == null) {
			doRelativeFrequencies();
		}
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		//..iterate over sums to ensure the same order as in #getAbsoluteBarDataSet
		sums.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String character, int sum) {
				double percents = relativeFrequencies.get(character) * 100d;
				dataset.setValue(percents, "Relativní četnost", character);
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
		int allCharactersInAllTexts = 0;
		for (String character : allCharacters) {
			int sum = 0;

			for (CharacterFrequency frequency : characterFrequency.values()) {
				sum += frequency.getFrequencyFor(character);
			}

			sums.put(character, sum);
			allCharactersInAllTexts += sum;

			if (sum == maxOccurrence) {
				maxOccurrences.add(character);
			} else if (sum > maxOccurrence) {
				maxOccurrences.clear();
				maxOccurrences.add(character);
				maxOccurrence = sum;
			}
		}
		this.maxOccurrence = maxOccurrence;
		this.allCharactersInAllTexts = allCharactersInAllTexts;
	}

	private void doRelativeFrequencies() {
		if (sums == null) {
			doSums();
		}

		relativeFrequencies = new TObjectDoubleHashMap<String>();
		final double allCharactersInAllTexts = (double) this.allCharactersInAllTexts;
		sums.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String character, int sum) {
				double relative = (sum / allCharactersInAllTexts);
				relativeFrequencies.put(character, relative);
				return true;
			}
		});
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

	@Override
	public CsvSaver<CharacterFrequencyModel> getCsvSaver() {
		return new CharacterFrequencyModelSaver();
	}

	@Override
	public boolean supportsCsvImport() {
		return false;
	}

	@Override
	public CsvLoader<CharacterFrequencyModel> getCsvLoader() {
		throw new UnsupportedOperationException("This class does not support loading from csv. Check it using supportsCsvImport() method");
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

	public String getIntroLabelText() {

		StringBuilder str = new StringBuilder();

		String intro = String.format(
			getTextsCount() == 1 ? "Byl analyzován %d text" :
				getTextsCount() >= 5 ? "Bylo analyzováno %d textů" : "Byly analyzovány %d texty"

			, getTextsCount()
		);

		str.append(intro).append(": ");
		str.append("\n\n");

		for (WorkingText wt : getWorkingTexts()) {
			str.append("\t\u2022 ").append(wt.getName()).append("\n");
		}
		str.append("\n");

		final int totalCharacters = getTotalCharactersCount();
		final int charactersCount = getCharactersCount();

		String text = (getTextsCount() == 1 ? "Tento textu obsahoval" : "Tyto texty obsahovaly");
		text += " celkem %d " + (totalCharacters == 1 ? "znak" : totalCharacters >= 5 ? "znaků" : "znaky") + ". Z nich ";


		text += (charactersCount == 1 ? " byl "
			: charactersCount >= 5 ? " bylo " : " byly "
		) + " %d" + (charactersCount == 1 ? " znak unikátní."
			: charactersCount >= 5 ? " znaků unikátních." : " znaky unikátní.");

		str.append(String.format(text, totalCharacters, charactersCount));

		str.append("\n\n").append("Pro reprezentativnost výběru s průměrnou směrodatonou odchylkou r = ")
			.append(odchylka).append(" by bylo potřeba analyzovat výběr obsahující alespoň N = ")
			.append(new DecimalFormat("0").format(getN())).append(" znaků.")
			.append(" To znamená, že výběr ").append((getN() <= totalCharacters ? "je" : "není")).append(" reprezentativní.");

		java.util.List<String> mostOftenCharacters = getMostOftenCharacter();
		StringBuilder mostOftenCharsBuilder = new StringBuilder();
		for (String s : mostOftenCharacters) {
			mostOftenCharsBuilder.append('\'').append(s).append("', ");
		}
		if (mostOftenCharsBuilder.length() >= 2) {
			mostOftenCharsBuilder.setLength(mostOftenCharsBuilder.length() - 2);
		}
		String mostOftenChar = "Nejčastěji nalezeným znakem " + (mostOftenCharacters.size() == 1 ? "byl znak" : "byly znaky") + " %s";
		mostOftenChar += ", " + (mostOftenCharacters.size() == 1 ? "který byl nalezen celkem %d×." : "které se vyskytly celkem  %d×.");

		str.append("\n\n").append(String.format(mostOftenChar, mostOftenCharsBuilder.toString(), getMaxOccurrence()));

		return str.toString();

	}

	private double getN() {
		if (relativeFrequencies == null) {
			doRelativeFrequencies();
		}

		final int k = getCharactersCount();
		double sum = 0d;
		for (String character : allCharacters) {
			double p_i = relativeFrequencies.get(character);
			sum += Math.log(p_i);
		}
		double fraction = 1d / ((double)k - 1d);
		double afterMultiply = fraction * sum;

		double lnN = afterMultiply - 2 * Math.log(odchylka);

		return Math.exp(lnN);
	}

	public double getOdchylka() {
		return odchylka;
	}

	public void setOdchylka(double odchylka) {
		this.odchylka = odchylka;
	}

	private static class CharacterFrequencyTableModel extends AbstractTableModel {

		private final List<String> rows;
		private final List<WorkingText> workingTexts;
		private final List<CharacterFrequency> characterFrequencies;
		private final TObjectDoubleHashMap<String> relativeFrequencies;

		private CharacterFrequencyTableModel(Set<String> rows,
		                                     Map<WorkingText, CharacterFrequency> characterFrequency,
		                                     TObjectDoubleHashMap<String> relativeFrequencies) {
			this.relativeFrequencies = relativeFrequencies;

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
			return characterFrequencies.size() + 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Znak";
			} else if (columnIndex == 1) {
				return "Relativní četnost výskytu znaku";
			}
			//columnIndex - 2 because 0 is "Character" and 1 is "relative ..." column
			return workingTexts.get(columnIndex - 2).getName();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return String.class;
			} else if (columnIndex == 1) {
				return Double.class;
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
			if (columnIndex == 1) {
				double relative = relativeFrequencies.get(s);
				return relative * 100d; //..to percents
			}
			//columnIndex - 2 because 0 is "Character" and 1 is "relative ..." column
			return characterFrequencies.get(columnIndex - 2).getFrequencyFor(s);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		}
	}

	private static class CharacterFrequencyModelSaver extends CsvSaver<CharacterFrequencyModel> {
		@Override
		public CsvData saveToCsv(CharacterFrequencyModel object, Object... params) {
			CsvData csvData = new CsvData();
			csvData.addSection();
			//..add all characters to header
			csvData.getCurrentSection().addHeader(object.allCharacters);
			for (Map.Entry<WorkingText, CharacterFrequency> entry : object.characterFrequency.entrySet()) { //..for each text
				csvData.getCurrentSection().startNewLine();
				csvData.getCurrentSection().addData(entry.getKey().getName());//..put name as first thing in line (it is not in header now)
				for (Object o : csvData.getCurrentSection().getHeaders()) {
					int frequencyFor = entry.getValue().getFrequencyFor(o.toString());
					csvData.getCurrentSection().addData(frequencyFor);
				}
			}
			csvData.getCurrentSection().addHeader(0, "Text");

			return csvData;
		}
	}
}
