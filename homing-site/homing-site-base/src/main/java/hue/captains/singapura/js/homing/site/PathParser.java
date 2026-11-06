package hue.captains.singapura.js.homing.site;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/** Reads a request path ({@link Path}). */
public record PathParser() implements StatelessFunctionalObject {

    public static final PathParser INSTANCE = new PathParser();

    /**
     * Reads a request path. Leading, trailing and doubled slashes contribute
     * nothing, so {@code /}, {@code ""} and {@code //} are all {@link Path#ROOT}
     * and {@code /about/} is {@code /about}. Each segment is percent-decoded;
     * a {@code +} is a plus, not a space — that convention belongs to query
     * strings, which a {@link Query} honours.
     */
    public Path parse(String raw) {
        if (raw == null) return Path.ROOT;
        var out = new ArrayList<String>();
        for (String s : raw.split("/")) {
            if (s.isEmpty()) continue;
            out.add(decode(s));
        }
        return out.isEmpty() ? Path.ROOT : new Path(out);
    }

    private String decode(String s) {
        // And the inverse: shield a literal plus from the form decoder, which
        // would otherwise turn it into a space.
        try {
            return URLDecoder.decode(s.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException badEscape) {
            return s;
        }
    }
}
