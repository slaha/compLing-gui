package cz.slahora.compling.gui.utils;

import cz.slahora.compling.gui.model.WorkingText;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectIntProcedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Utilities for work with {@code java.util.Map}s
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 29.3.14 10:48</dd>
 * </dl>
 */
public class MapUtils {

	/**
	 * Returns class of the first value object in the map
	 *
	 * @param map map to find value class in. Cannot be null or empty
	 * @return class of first value in the map
	 * @throws java.lang.IllegalArgumentException if map is null or empty map
	 */
	public static <T> Class<?> getValueClass(Map<?, T> map) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException("null or empty map supplied as parameter 'map'");
		}
		return map.values().iterator().next().getClass();
	}

	/**
	 * Returns first value object in the map
	 *
	 * @param map map to find value in. Cannot be null or empty
	 * @return first value in the map
	 * @throws java.lang.IllegalArgumentException if map is null or empty map
	 */
	public static<T> T getFirstValue(Map<?, T> map) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException("null or empty map supplied as parameter 'map'");
		}
		return map.values().iterator().next();
	}

	/**
	 * Returns first value object in the map
	 *
	 * @param map map to find value in. Cannot be null or empty
	 * @return first value in the map
	 * @throws java.lang.IllegalArgumentException if map is null or empty map
	 */
	public static <T> T getFirstKey(Map<T, ?> map) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException("null or empty map supplied as parameter 'map'");
		}
		return map.keySet().iterator().next();
	}

	/**
	 * Finds max value in TObjectIntMap<?>
	 *
	 * @param map map to find maximum value in
	 * @return maximum value from values in map
	 */
	public static int findMaxValue(TObjectIntMap<?> map) {
		final int max[] = new int[] { map.values()[0] };

		map.forEachValue(new TIntProcedure() {
			@Override
			public boolean execute(int i) {
				if (i > max[0]) {
					max[0] = i;
				}
				return true;
			}
		});
		return max[0];
	}

	/**
	 * Finds max value in TObjectIntMap<?>
	 *
	 * @param map map to find maximum value in
	 * @return maximum value from values in map
	 */
	public static int findMaxValue(TIntIntMap map) {
		final int max[] = new int[] { map.values()[0] };

		map.forEachValue(new TIntProcedure() {
			@Override
			public boolean execute(int i) {
				if (i > max[0]) {
					max[0] = i;
				}
				return true;
			}
		});
		return max[0];
	}

	public static <T> List<T> getAllKeysWithValue(TObjectIntMap<T> map, final int value) {
		final List<T> list = new ArrayList<T>();
		map.forEachEntry(new TObjectIntProcedure<T>() {
			@Override
			public boolean execute(T s, int i) {
				if (i == value) {
					list.add(s);
				}
				return true;
			}
		});
		return list;
	}

	public static List<Integer> getAllKeysWithValue(TIntIntMap map, final int value) {
		final List<Integer> list = new ArrayList<Integer>();
		map.forEachEntry(new TIntIntProcedure() {
			@Override
			public boolean execute(int length, int frequency) {
				if (frequency == value) {
					list.add(length);
				}
				return true;
			}
		});
		return list;
	}

	public static String getAllTextNames(Map<WorkingText, ?> map) {
		if (map.size() == 1) {
			return MapUtils.getFirstKey(map).getName();
		} else {
			StringBuilder s = new StringBuilder();
			for (WorkingText workingText : map.keySet()) {
				s.append(s.length() == 0 ? "" : ", ").append(workingText.getName());
			}
			return s.toString();
		}
	}
}
