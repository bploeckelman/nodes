package net.bplo.nodes;

import com.badlogic.gdx.Gdx;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
}
