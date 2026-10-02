package hue.captains.singapura.js.homing.site;

import hue.captains.singapura.js.homing.core.QueryString;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0066 Episode 3 — the query parameters a page was reached with. Possibly
 * none, which is {@link #NONE}: a page with nothing to be told is still a page.
 *
 * <p>A key may carry several values, so the shape is the core's
 * {@code Map<String, List<String>>}, read through {@link QueryString#parse}
 * — the same parser the flat {@code /app} route uses, so a site and a studio
 * read one address the same way. What the values mean is the navigable's
 * business; the query only carries them.</p>
 *
 * @param all every key with every value it was given, in address order
 */
public record Query(Map<String, List<String>> all) {

    /** No parameters at all. */
    public static final Query NONE = new Query(Map.of());

    public Query {
        Objects.requireNonNull(all, "Query.all");
        var copy = new LinkedHashMap<String, List<String>>();
        all.forEach((k, v) -> copy.put(k, List.copyOf(v)));
        all = Collections.unmodifiableMap(copy);
    }

    /** Reads the part after the {@code ?}; null and blank are {@link #NONE}. */
    public static Query parse(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) return NONE;
        return new Query(QueryString.parse(rawQuery));
    }

    /** A one-key query, for tests and links. */
    public static Query of(String key, String value) {
        return new Query(QueryString.of(key, value));
    }

    public boolean isEmpty() { return all.isEmpty(); }

    /** The first value under {@code key}, when the key was given at all. */
    public Optional<String> first(String key) {
        var vs = all.get(key);
        return (vs == null || vs.isEmpty()) ? Optional.empty() : Optional.of(vs.get(0));
    }

    /** Every value under {@code key}; empty when the key was not given. */
    public List<String> values(String key) {
        return all.getOrDefault(key, List.of());
    }

    /** The address form, without the leading {@code ?}; empty for {@link #NONE}. */
    @Override
    public String toString() {
        return QueryString.encode(all);
    }
}
