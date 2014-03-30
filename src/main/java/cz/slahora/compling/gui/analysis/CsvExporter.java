package cz.slahora.compling.gui.analysis;

import cz.slahora.compling.gui.model.CsvData;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
public class CsvExporter {
	private final CsvData csvData;

	public CsvExporter(CsvData csvData) {
		this.csvData = csvData;
	}

	public void export(File csvFile) throws IOException {
		IOUtils.writeLines(csvData.toLines(), null, new FileOutputStream(csvFile));
	}
}
