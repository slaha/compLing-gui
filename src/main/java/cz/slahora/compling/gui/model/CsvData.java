package cz.slahora.compling.gui.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
}
