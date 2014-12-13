package cz.slahora.compling.gui.analysis.alliteration;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class AlliterationTableModel  extends AbstractTableModel {

	private static final int COLUMNS_COUNT = 6;

	private final List<Row> rows = new ArrayList<Row>();
	private final DecimalFormat pFormat;
	private final DecimalFormat kaFormat;

	AlliterationTableModel(DecimalFormat pDecimalFormat, DecimalFormat kaDecimalFormat) {
		this.pFormat = pDecimalFormat;
		this.kaFormat = kaDecimalFormat;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case VERSE_NUMBER_INDEX:
				return "Verš";
			case PHONEMES_INDEX:
				return "Fonémy";
			case COUNT_OF_WORDS_INDEX:
				return "n";
			case OCCURRENCES_INDEX:
				return "k1; k2…";
			case PROBABILITY_INDEX:
				return "p";
			case KA_INDEX:
				return "KA";
			default:
				throw new IllegalArgumentException("Cannot get value for columnIndex " + columnIndex);
		}
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMNS_COUNT;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Row row = rows.get(rowIndex);
		return row.getColumnValue(columnIndex);
	}

	public void addRow(int verse, Collection<String> phonemes, int n, int[] k, double p, double ka) {
		rows.add(new Row(verse, phonemes, n, ArrayUtils.toObject(k), p, ka));
	}

	public Row getRow(int row) {
		return rows.get(row);
	}

	private static final int VERSE_NUMBER_INDEX = 0;
	private static final int PHONEMES_INDEX = VERSE_NUMBER_INDEX + 1;
	private static final int COUNT_OF_WORDS_INDEX = PHONEMES_INDEX + 1;
	private static final int OCCURRENCES_INDEX = COUNT_OF_WORDS_INDEX + 1;
	private static final int PROBABILITY_INDEX = OCCURRENCES_INDEX + 1;
	private static final int KA_INDEX = PROBABILITY_INDEX + 1;

	public class Row {

		private final boolean containsAlliteration;

		private final int verse;
		private final Collection<String> phonemes;
		private final int n;
		private final Integer[] k;
		private final double p;
		private final double ka;

		public Row(int verse, Collection<String> phonemes, int n, Integer[] k, double p, double ka) {
			this.containsAlliteration = !phonemes.isEmpty();

			this.verse = verse;
			this.phonemes = phonemes;
			this.n = n;
			this.k = k;
			this.p = p;
			this.ka = ka;
		}

		public Object getColumnValue(int columnIndex) {
			if (!containsAlliteration
				&& (columnIndex != VERSE_NUMBER_INDEX
					&& columnIndex != COUNT_OF_WORDS_INDEX
					&& columnIndex != KA_INDEX )) {
				return "–";
			}

			switch (columnIndex) {
				case VERSE_NUMBER_INDEX:
					return verse;
				case PHONEMES_INDEX:
					return new StrBuilder().appendWithSeparators(phonemes, ", ").toString();
				case COUNT_OF_WORDS_INDEX:
					return n;
				case OCCURRENCES_INDEX:
					return new StrBuilder().appendWithSeparators(k, ", ").toString();
				case PROBABILITY_INDEX:
					String formatted = pFormat.format(p);
					if ("0".equals(formatted)) {
						return "~" + formatted;
					}
					return formatted;
				case KA_INDEX:
					return kaFormat.format(ka);
				default:
					throw new IllegalArgumentException("Cannot get value for columnIndex " + columnIndex);
			}
		}

		public boolean isContainsAlliteration() {
			return containsAlliteration;
		}

		public int getVerse() {
			return verse;
		}

		public Integer[] getAlliteration() {
			return k;
		}

		public int getSize() {
			return n;
		}
	}
}
