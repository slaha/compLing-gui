package cz.slahora.compling.gui.model;

import java.text.ParseException;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 9.4.14 6:41</dd>
 * </dl>
 */
public interface Csv<T> {

	CsvSaver<T> getCsvSaver();

	boolean supportsCsvImport();

	CsvLoader<T> getCsvLoader();

	public abstract class CsvLoader<T> {

		public abstract void loadFromCsv(CsvData csv, T objectToLoad, Object... params) throws ParseException;
	}

	public abstract class CsvSaver<T> {

		public abstract CsvData saveToCsv(T object, Object... params);
	}
}
