package net.bplo.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    // ------------------------------------------------------------------------
    // Logging
    // ------------------------------------------------------------------------

    public static final DateTimeFormatter TIME_FMT_SIMPLE = DateTimeFormatter.ofPattern("hh:mm:ss");

    public static void log(String msg) {
        log("", msg);
    }

    public static void log(String tag, Object object, Function<Object, String> toString) {
        log(tag, toString.apply(object));
    }

    public static void log(String tag, String msg) {
        var time = TIME_FMT_SIMPLE.format(LocalDateTime.now());
        Gdx.app.log("%s %s".formatted(time, tag), msg);
    }

    // ------------------------------------------------------------------------
    // Stream helpers
    // ------------------------------------------------------------------------

    public static Collector<CharSequence, ?, String> join() {
        return Util.join(", ", "[", "]");
    }

    public static Collector<CharSequence, ?, String> join(String delimiter, String prefix, String suffix) {
        return Collectors.joining(delimiter, prefix, suffix);
    }

    // ------------------------------------------------------------------------
    // LibGDX collection type adapters for jdk collection interfaces
    // ------------------------------------------------------------------------

    /**
     * Adapts libGDX {@link Array} to the Java {@link List} interface,
     * as an <strong>unmodifiable list</strong>. This is convenient for use
     * in {@link Stream} operations since they don't modify the underlying collection.
     */
    public static <T> List<T> asList(Array<T> array) {
        return new AbstractList<>() {
            @Override
            public T get(int index) {
                return array.get(index);
            }

            @Override
            public int size() {
                return array.size;
            }
        };
    }

    /**
     * Adapts libGDX {@link ObjectMap} to the Java {@link Map} interface,
     * as an <strong>unmodifiable map</strong>. This is convenient for use
     * in {@link Stream} operations since they don't modify the underlying collection.
     */
    public static <K, V> Map<K, V> asMap(ObjectMap<K, V> map) {
        return new AbstractMap<>() {
            @Override
            public Set<Entry<K, V>> entrySet() {
                var entries = new HashSet<Entry<K, V>>();
                for (var entry : map.entries()) {
                    entries.add(new AbstractMap.SimpleEntry<>(entry.key, entry.value));
                }
                return entries;
            }
        };
    }


}
