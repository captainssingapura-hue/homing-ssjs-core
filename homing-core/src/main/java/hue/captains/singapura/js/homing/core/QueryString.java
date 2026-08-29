package hue.captains.singapura.js.homing.core;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RFC 0051 — the one place query strings are encoded and decoded.
 *
 * <p>The wire shape is {@code Map<String, List<String>>} everywhere, with a
 * singleton list for the ordinary single-valued key. Uniform cardinality is
 * the point: a codec that never has to ask "is this key repeated?" cannot get
 * the answer wrong, and the repeated-key case stops being a special case
 * nobody tested.</p>
 *
 * <p>Before this, five sites hand-rolled {@link URLEncoder} calls with their
 * own ideas about ordering, empty values and the {@code space-as-plus}
 * question. A URL that round-trips is not a property five implementations can
 * be relied on to share.</p>
 */
public final class QueryString {

    private QueryString() {}

    /** The uniform wire shape: every key maps to one or more raw values. */
    public static Map<String, List<String>> params() {
        return new LinkedHashMap<>();
    }

    /** A single-valued map, the common case, in declaration order. */
    public static Map<String, List<String>> of(String key, String value) {
        var m = params();
        put(m, key, value);
        return m;
    }

    /** Append one value under {@code key}, creating the list if needed. */
    public static void put(Map<String, List<String>> params, String key, String value) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(key, "key");
        if (value == null) return;   // an absent value is an absent key, not "null"
        params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    /**
     * The first value for a key, or null when the key is absent or empty.
     * Most codecs want exactly this; the ones that accept repeats read the
     * list directly.
     */
    public static String first(Map<String, List<String>> params, String key) {
        List<String> values = params == null ? null : params.get(key);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    /**
     * Encode to {@code a=1&b=2}, without a leading {@code ?}. Insertion order
     * is preserved, and repeated keys are emitted once per value.
     *
     * <p>Ordering is deliberate rather than incidental: two calls with equal
     * params must produce byte-identical strings, or URLs stop being
     * comparable and caches stop being able to tell one address from
     * another.</p>
     */
    public static String encode(Map<String, List<String>> params) {
        if (params == null || params.isEmpty()) return "";
        var sb = new StringBuilder();
        for (var e : params.entrySet()) {
            for (String value : e.getValue()) {
                if (sb.length() > 0) sb.append('&');
                sb.append(enc(e.getKey())).append('=').append(enc(value));
            }
        }
        return sb.toString();
    }

    /** Encode and prefix with {@code ?}, or the empty string when there is
     *  nothing to say — so callers can append unconditionally. */
    public static String encodeSuffix(Map<String, List<String>> params) {
        String s = encode(params);
        return s.isEmpty() ? "" : "?" + s;
    }

    /**
     * Read {@code a=1&b=2} back into the wire shape. Accepts a full URL, a
     * leading {@code ?}, or the bare query.
     *
     * <p>Total by construction — there is no such thing as a malformed query
     * string at this layer. A key with no {@code =} maps to the empty string,
     * and an undecodable escape keeps its raw text rather than throwing:
     * deciding whether a value is acceptable belongs to the codec that knows
     * what the value is supposed to mean, and raising here would turn every
     * odd URL into a 500 before anything could answer it properly.</p>
     */
    public static Map<String, List<String>> parse(String query) {
        var out = params();
        if (query == null) return out;
        String s = query;
        int q = s.indexOf('?');
        if (q >= 0) s = s.substring(q + 1);
        int hash = s.indexOf('#');
        if (hash >= 0) s = s.substring(0, hash);
        if (s.isBlank()) return out;
        for (String pair : s.split("&")) {
            if (pair.isEmpty()) continue;
            int eq = pair.indexOf('=');
            String rawKey = eq < 0 ? pair : pair.substring(0, eq);
            String rawVal = eq < 0 ? ""   : pair.substring(eq + 1);
            String key = dec(rawKey);
            if (key.isEmpty()) continue;
            out.computeIfAbsent(key, k -> new ArrayList<>()).add(dec(rawVal));
        }
        return out;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String dec(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException badEscape) {
            return s;
        }
    }
}
