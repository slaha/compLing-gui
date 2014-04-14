package cz.slahora.compling.gui.model;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 29.3.14 19:41</dd>
 * </dl>
 */
public class CsvData {

	private static final List<Object> EMPTY_LINE = new ArrayList<Object>() {
		@Override
		public String toString() {
			return "-------------------------";
		}
	};

	private TIntObjectMap<CsvDataSection> sections;
	private CsvDataSection currentSection;
	private int currentSectionNumber;

	public CsvData() {
		this.sections = new TIntObjectHashMap<CsvDataSection>();
	}

	/**
	 * Merge constructor
	 */
	public CsvData(CsvData...datas) {
		this();

		for (CsvData data : datas) {
			for (CsvDataSection section : data.getSections()) {
				addSection();
				currentSection.addHeader(section.getHeaders());
				currentSection.addDataLines(section.getDataLines());
			}
		}
	}

	private Iterable<? extends CsvDataSection> getSections() {
		return sections.valueCollection();
	}

	public CsvData(CsvDataSection section) {
		this();

		addSection(section);
	}

	public void addSection() {
		addSection(new CsvDataSection());
	}

	private void addSection(CsvDataSection section) {
		currentSection = section;
		sections.put(currentSectionNumber++, currentSection);
	}

	public CsvDataSection getSection(int sectionNumber) {
		return sections.get(sectionNumber);
	}

	public CsvDataSection getCurrentSection() {
		return currentSection;
	}

	public CsvData(Iterable<String> lines) {
		this();

		Iterator<String> iterator = lines.iterator();
		//..header of first section
		addHeader(iterator);

		//..body
		while (iterator.hasNext()) {
			String line = iterator.next();
			if (EMPTY_LINE.toString().equals(line)) {
				addHeader(iterator);
			} else {
				currentSection.startNewLine();
				currentSection.addLine(currentSection.currentRow, line);
			}
		}
	}

	private boolean addHeader(Iterator<String> iterator) {
		addSection();
		while (iterator.hasNext()) {
			String header = iterator.next();
			if (StringUtils.isNotBlank(header)) {
				currentSection.addLine(currentSection.header, header);
				return true;
			}
		}
		return false;
	}

	public Collection<?> toLines() {
		int[] sectionNumbers = sections.keys();
		Collection<Object> lines = new ArrayList<Object>();
		Arrays.sort(sectionNumbers);
		if (sectionNumbers.length > 0) {
			lines.addAll(sections.get(sectionNumbers[0]).toLines());
			for (int i = 1; i < sectionNumbers.length; i++) {
				lines.add(EMPTY_LINE);
				lines.addAll(sections.get(sectionNumbers[i]).toLines());
			}
		}
		return lines;
	}

	public class CsvDataSection {
		private final List<Object> header;
		private final List<List<Object>> data;
		private List<Object> currentRow;

		public CsvDataSection() {
			this.header = new ArrayList<Object>() {
				@Override
				public String toString() {
					return CsvDataSection.this.toString(this);
				}
			};
			this.data = new ArrayList<List<Object>>();
		}


		private void addDataLines(List<List<Object>> dataLines) {
			for (List<Object> line : dataLines) {
				startNewLine();
				currentRow.addAll(line);
			}
		}

		private void addLine(List<Object> collection, String line) {
			String[] values = line.split(";(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			for (String value : values) {
				value = StringUtils.replace(value, "\"\"", "\""); //..remove double quotes
				if (value.startsWith("\"")) {
					value = value.substring(1);
				}
				if (value.endsWith("\"")) {
					value = value.substring(0, value.length() - 1);
				}
				collection.add(value);
			}
		}

		public void addHeader(int index, Object headerData) {
			header.add(index, headerData);
		}

		public void addHeader(Object headerData) {
			header.add(headerData);
		}

		public void addHeader(Collection<?> headerData) {
			header.addAll(headerData);
		}

		public void addData(Object data) {
			currentRow.add(data);
		}

		public void startNewLine() {
			currentRow = new ArrayList<Object>() {
				@Override
				public String toString() {
					return CsvDataSection.this.toString(this);
				}
			};
			data.add(currentRow);
		}

		public Collection<Object> getHeaders() {
			return Collections.unmodifiableCollection(header);
		}

		public Collection<?> toLines() {
			List<List<Object>> lines = new ArrayList<List<Object>>();
			lines.add(header);
			for (List<Object> objects : data) {
				lines.add(objects);
			}
			return Collections.unmodifiableCollection(lines);
		}

		/**
		 */
		private String toString(List<Object> list) {
			StringBuilder sb = new StringBuilder();
			
			for (Object o : list) {
				escape(sb, o);
			}

			return sb.toString();
		}

		private void escape(StringBuilder sb, Object o) {
			o = (o == null) ? null : StringUtils.replace(o.toString(), "\"", "\"\"");
			sb.append('"').append(o).append("\";");
		}

		public List<List<Object>> getDataLines() {
			return data;
		}
	}
}
