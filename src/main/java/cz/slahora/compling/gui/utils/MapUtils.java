package cz.slahora.compling.gui.utils;

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
}
