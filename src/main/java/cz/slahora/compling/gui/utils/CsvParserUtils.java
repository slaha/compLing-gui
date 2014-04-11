package cz.slahora.compling.gui.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 11.4.14 15:59</dd>
 * </dl>
 */
public class CsvParserUtils {
	public static int getAsInt(Object o) throws ParseException {
		if (o == null) {
			throw new ParseException("Cannot parse Object as integer because Object is null", -1);
		}
		try {
			return Integer.parseInt(o.toString());
		} catch (NumberFormatException nfe) {
			throw new ParseException(nfe.getMessage(), -1);
		}
	}

	public static Collection<String> getAsStringList(Object o, CollectionSplitter splitter) throws ParseException {
		if (o == null) {
			throw new ParseException("Cannot parse Object as collection because Object is null", -1);
		} else if (splitter == null) {
			throw new ParseException("Cannot parse Object as collection because splitter is null", -1);
		}

		String oString = o.toString();
		String[] split = StringUtils.split(oString, splitter.getSplitter());
		return Arrays.asList(split);
	}

	public static Collection<Integer> getAsIntList(Object o, CollectionSplitter splitter) throws ParseException {
		Collection<String> stringList = getAsStringList(o, splitter);
		Collection<Integer> ints = new ArrayList<Integer>(stringList.size());
		for (String s : stringList) {
			ints.add(getAsInt(s));
		}
		return ints;
	}

	public static boolean getAsBool(Object o) throws ParseException {
		if (o == null) {
			throw new ParseException("Cannot parse Object as integer because Object is null", -1);
		}
		return Boolean.parseBoolean(o.toString());
	}

	public static interface CollectionSplitter {
		String getSplitter();
	}
}
