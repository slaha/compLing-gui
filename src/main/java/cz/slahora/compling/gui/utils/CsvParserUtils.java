package cz.slahora.compling.gui.utils;

import cz.slahora.compling.gui.model.Csv;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * This class contains very useful methods for parsing csv file
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 11.4.14 15:59</dd>
 * </dl>
 */
public class CsvParserUtils {

	/**
	 * Tries to parse {@code o} as int
	 *
	 * @param o object to parse. Cannot be null
	 * @return the o as int
	 * @throws Csv.CsvParserException if it is not possible to parse {@code o } as integer or o is null
	 */
	public static int getAsInt(Object o) throws Csv.CsvParserException {
		if (o == null) {
			throw new Csv.CsvParserException("Cannot parse Object as integer because Object is null");
		}
		try {
			return Integer.parseInt(o.toString());
		} catch (NumberFormatException nfe) {
			throw new Csv.CsvParserException(nfe.getMessage());
		}
	}

	public static String getAsString(Object o) throws Csv.CsvParserException {
		if (o == null) {
			throw new Csv.CsvParserException("Cannot parse Object as string because Object is null");
		}
		return o.toString();
	}


	/**
	 * Gets {@code o } as list of strings. Use {@code splitter} to split values.
	 *
	 * @param o object object to parse. Cannot be null
	 * @param splitter the splitter to split value of {@code o}. Cannot be null
	 * @return value of {@code o} as list of string
	 * @throws Csv.CsvParserException when {@code o} or {@code splitter} is null
	 */
	public static Collection<String> getAsStringList(Object o, CollectionSplitter splitter) throws Csv.CsvParserException {
		if (o == null) {
			throw new Csv.CsvParserException("Cannot parse Object as collection because Object is null");
		} else if (splitter == null) {
			throw new Csv.CsvParserException("Cannot parse Object as collection because splitter is null");
		}

		String oString = o.toString();
		String[] split = StringUtils.split(oString, splitter.getSplitter());
		return Arrays.asList(split);
	}

	public static Collection<Integer> getAsIntList(Object o, CollectionSplitter splitter) throws Csv.CsvParserException {
		Collection<String> stringList = getAsStringList(o, splitter);
		Collection<Integer> ints = new ArrayList<Integer>(stringList.size());
		for (String s : stringList) {
			ints.add(getAsInt(s));
		}
		return ints;
	}

	public static boolean getAsBool(Object o) throws Csv.CsvParserException {
		if (o == null) {
			throw new Csv.CsvParserException("Cannot parse Object as integer because Object is null");
		}
		return Boolean.parseBoolean(o.toString());
	}

	public static <T> Collection<T> getAsList(Object o, CollectionSplitter splitter, CollectionParser<T> parser) throws Csv.CsvParserException {
		Collection<String> stringList = getAsStringList(o, splitter);
		Collection<T> list = new ArrayList<T>();
		for (String s : stringList) {
			parser.parse(s, list);
		}
		return list;
	}

	public static interface CollectionSplitter {
		String getSplitter();
	}

	public static interface CollectionParser<T> {
		void parse(String toParse, Collection<T> toAdd) throws Csv.CsvParserException;
	}
}
