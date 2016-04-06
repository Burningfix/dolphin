package org.dolphin.lib;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hanyanan on 2015/5/10.
 */
public class ListUtil {
    /** Returns an immutable copy of {@code list}. */
    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(list);
    }

    /** Returns an immutable list containing {@code elements}. */
    public static <T> List<T> immutableList(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements.clone()));
    }

    /** Returns an immutable copy of {@code map}. */
    public static <K, V> Map<K, V> immutableMap(Map<K, V> map) {
        return Collections.unmodifiableMap(new LinkedHashMap<K, V>(map));
    }
}
