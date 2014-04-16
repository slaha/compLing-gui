package cz.slahora.compling.gui.model;

/**
 *
 * Interface that must be implemented by classes that support exporting to and (optionally) importing from CSV file
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 9.4.14 6:41</dd>
 * </dl>
 */
public interface Csv<T> {

	/**
	 * Gets CsvSaver object for the class
	 *
	 * @return the csv saver
	 */
	CsvSaver<T> getCsvSaver();


	/**
	 * Check if the class supports import from csv.
	 *
	 * @return the true if import is supported; false otherwise
	 */
	boolean supportsCsvImport();


	/**
	 * Gets CsvLoader for the class
	 *
	 * @return the csv loader
	 * @throws UnsupportedOperationException if importing is not supported
	 */
	CsvLoader<T> getCsvLoader();

	public class CsvParserException extends Exception {

		public CsvParserException(String message) {
			super(message);
		}
	}

	public abstract class CsvLoader<T> {


		/**
		 * Load from csv.
		 *
		 * @param csv the csv to load from
		 * @param objectToLoad the object to load into
		 * @param params params (optional)
		 * @throws CsvParserException if something is wrong and it is not possible to load
		 */
		public abstract void loadFromCsv(CsvData csv, T objectToLoad, Object... params) throws CsvParserException;
	}

	public abstract class CsvSaver<T> {

		/**
		 * Save to csv.
		 *
		 * @param object the object to save
		 * @param params params (optional)
		 * @return the csv data that will be written to the file
		 */
		public abstract CsvData saveToCsv(T object, Object... params);
	}
}
