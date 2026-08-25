package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.Map;

/**
 * RFC 0051 — render an app's params as a JS value the server puts into the
 * page, so the client never parses its own URL.
 *
 * <p>Today {@code ParamsWriter} emits JS that reads
 * {@code window.location.search} at load time. The server already knows the
 * params — it resolved them to answer the request at all — so the client
 * re-deriving them is a second implementation of the same decision, running
 * against a string the server has already interpreted. Stamping removes the
 * second one.</p>
 *
 * <p><b>Values, not a schema.</b> D5 chose a typed-constructor stamp — a
 * frozen ES class mirroring the Params record. That class is produced by
 * walking the record's components, and doing that per request is exactly the
 * "schema-by-reflection at request time" the Codegen Over Reflection doctrine
 * bans. So this emits values only, built straight from
 * {@link ParamCodec#to}: no reflection on the request path. The typed class
 * belongs with the module's generated JS, where {@code ParamsWriter}'s
 * build-time reflection already lives, and the two should be unified when
 * that writer is retired.</p>
 */
public final class StampedParams {

    private StampedParams() {}

    private static final String BACKSLASH = String.valueOf((char) 92);

    /**
     * A frozen JS object literal for these params: single values as strings,
     * repeated keys as arrays.
     *
     * <p>Cardinality is preserved rather than flattened, so a page reading a
     * repeated key sees what the URL actually carried.</p>
     */
    public static String jsObject(Map<String, List<String>> params) {
        if (params == null || params.isEmpty()) return "Object.freeze({})";
        var sb = new StringBuilder("Object.freeze({");
        boolean first = true;
        for (var e : params.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(jsString(e.getKey())).append(':');
            List<String> values = e.getValue();
            if (values.size() == 1) {
                sb.append(jsString(values.get(0)));
            } else {
                sb.append("Object.freeze([");
                for (int i = 0; i < values.size(); i++) {
                    if (i > 0) sb.append(',');
                    sb.append(jsString(values.get(i)));
                }
                sb.append("])");
            }
        }
        return sb.append("})").toString();
    }

    /**
     * A JS string literal that is safe inside a {@code <script>} element.
     *
     * <p>Three hazards, none of which ordinary JSON escaping covers:</p>
     *
     * <ul>
     *   <li>{@code </script} ends the element no matter where it appears —
     *       inside a string literal included. The {@code /} is escaped so the
     *       sequence cannot form.</li>
     *   <li>{@code <!--} opens an HTML comment, which legacy script parsing
     *       still honours and which can swallow the rest of the block.</li>
     *   <li>U+2028 and U+2029 are line terminators in ECMAScript but not in
     *       JSON, so a value containing one parses as JSON and then breaks
     *       the script it was embedded in.</li>
     * </ul>
     *
     * <p>A value reaching here is user-controlled — it came off a query
     * string — so this is the boundary between a parameter and executable
     * page content, and it is a law with a test rather than a habit.</p>
     */
    public static String jsString(String value) {
        if (value == null) return "null";
        var sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                // Breaks "</script" and "<!--" without altering what the
                // string means once parsed.
                case '/'  -> sb.append("\\/");
                case '<'  -> sb.append("\\u003C");
                case '>'  -> sb.append("\\u003E");
                case '&'  -> sb.append("\\u0026");
                // Built from a char code so no escape survives Java's own
                // unicode preprocessing on the way to the emitted JS.
                case 0x2028 -> sb.append(BACKSLASH).append("u2028");
                case 0x2029 -> sb.append(BACKSLASH).append("u2029");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04X", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }
}
