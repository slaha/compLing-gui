package cz.slahora.compling.gui.model;

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

	private final List<Object> header;
	private final List<List<Object> > data;
	private List<Object> currentRow;

	public CsvData() {
		this.header = new ArrayList<Object>() {
			@Override
			public String toString() {
				return CsvData.this.toString(this, true);
			}
		};
		this.data = new ArrayList<List<Object>>();
	}

	public CsvData(Iterable<String> lines) {
		this();

		Iterator<String> iterator = lines.iterator();
		//..header
		if (iterator.hasNext()) {
			String header = iterator.next();
			if (StringUtils.isNotBlank(header)) {
				addLine(this.header, header);
			}
		}

		//..body
		while (iterator.hasNext()) {
			String line = iterator.next();
			startNewLine();
			addLine(currentRow, line);
		}
		startNewLine(); //..for last line
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
		if (currentRow != null) {
			data.add(currentRow);
		}
		currentRow = new ArrayList<Object>() {
			@Override
			public String toString() {
				return CsvData.this.toString(this, true);
			}
		};
	}

	public Iterable<Object> getHeaders() {
		return Collections.unmodifiableCollection(header);
	}

	public Collection<?> toLines() {
		List<List<Object> > lines = new ArrayList<List<Object>>();
		lines.add(header);
		for (List<Object> objects : data) {
			lines.add(objects);
		}
		return Collections.unmodifiableCollection(lines);
	}

	/**
	 * @param escapeQuotes if true quotes will be doubled (from " will be ...;""""). Set to false if it is not necessary to double quotes because it is slow
	 */
	private String toString(List<Object> list, boolean escapeQuotes) {
		StringBuilder sb = new StringBuilder();
		for (Object o : list) {
			if (escapeQuotes) {
				o = (o == null) ? null : StringUtils.replace(o.toString(),"\"","\"\"");
			}
			sb.append('"').append(o).append("\";");
		}

		return sb.toString();
	}

	public List<List<Object>> getDataLines() {
		return data;
	}
}
