package hue.captains.singapura.js.homing.core;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Compiled URL template for {@link ProxyApp#urlTemplate()}.
 *
 * <p>Compile a template against a Params record class once at boot
 * (validation is strict — invalid templates throw {@link IllegalStateException}),
 * then call {@link #render(Object)} with a Params instance to produce the
 * outgoing URL.</p>
 *
 * <p>Template DSL:</p>
 * <ul>
 *   <li>{@code {name}}            — required interpolation; {@code name} must
 *       exist on the Params record and must NOT be {@code Optional}.</li>
 *   <li>{@code {name?}}           — optional interpolation; {@code name} must
 *       exist and MUST be {@code Optional}. Substitutes empty string when absent.</li>
 *   <li>{@code {name?:default}}   — like {@code {name?}} but uses the literal
 *       default when the optional is empty.</li>
 *   <li>any other text            — literal, copied verbatim.</li>
 * </ul>
 *
 * <p>All interpolated values are URL-encoded
 * ({@code application/x-www-form-urlencoded} via {@link URLEncoder#encode(String, java.nio.charset.Charset)}).</p>
 *
 * <p>Introduced in RFC 0001 Step 03.</p>
 */
public final class UrlTemplate {

    /** A piece of the compiled template — either a literal string or a parameter reference. */
    sealed interface Segment {
        record Literal(String text) implements Segment {}
        record Param(String name, boolean optional, String defaultValue, Method accessor) implements Segment {}
    }

    private final String source;
    private final Class<?> paramsType;
    private final List<Segment> segments;

    /**
     * A template, compiled against the given Params record class.
     *
     * @param template   the template string
     * @param paramsType the record class describing valid params, or {@code Void.class}
     *                   for templates with no interpolation
     * @throws IllegalStateException if the template is malformed, references an
     *         unknown param, mis-uses optional/required, or includes
     *         interpolation when {@code paramsType == Void.class}
     */
    public UrlTemplate(String template, Class<?> paramsType) {
        this(template, paramsType, UrlTemplateParser.INSTANCE.segments(template, paramsType));
    }

    private UrlTemplate(String source, Class<?> paramsType, List<Segment> segments) {
        this.source = source;
        this.paramsType = paramsType;
        this.segments = List.copyOf(segments);
    }

    /** Source template, exactly as supplied. */
    public String source() { return source; }

    /** Names of all interpolation params, in order of first appearance. */
    public List<String> paramNames() {
        var seen = new LinkedHashMap<String, Boolean>();
        for (var seg : segments) {
            if (seg instanceof Segment.Param p) seen.putIfAbsent(p.name(), true);
        }
        return List.copyOf(seen.keySet());
    }

    /**
     * Render the template against a Params instance.
     *
     * @param params the Params record instance, or {@code null} if {@code paramsType == Void.class}
     * @return the resolved URL
     */
    public String render(Object params) {
        StringBuilder out = new StringBuilder();
        for (var seg : segments) {
            switch (seg) {
                case Segment.Literal lit -> out.append(lit.text());
                case Segment.Param p -> {
                    Object value = readValue(p, params);
                    if (value == null) {
                        // optional+empty: substitute default (if any) or empty string
                        if (p.defaultValue() != null) {
                            out.append(encode(p.defaultValue()));
                        }
                        // else nothing
                    } else {
                        out.append(encode(value.toString()));
                    }
                }
            }
        }
        return out.toString();
    }

    private Object readValue(Segment.Param p, Object params) {
        if (params == null) {
            throw new IllegalStateException(
                    "Template '" + source + "' references param '" + p.name() + "' "
                  + "but no Params instance was supplied");
        }
        try {
            Object raw = p.accessor().invoke(params);
            if (p.optional()) {
                if (raw == null) return null;
                Optional<?> opt = (Optional<?>) raw;
                return opt.isPresent() ? opt.get() : null;
            }
            return raw;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to read param '" + p.name() + "' from " + params.getClass().getName(), e);
        }
    }

    private String encode(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    // -----------------------------------------------------------------------
    // JS code generation (for the writer, RFC 0001 Step 05)
    // -----------------------------------------------------------------------

    /**
     * Emit a JS function body (without the surrounding {@code function (p) { ... }}
     * wrapper) that constructs the same URL this template would render — but in
     * the browser, against a runtime params object named {@code paramVar}.
     *
     * <p>Output is a single JS expression like:
     * {@code "https://github.com/" + encodeURIComponent(p.repo) + "/" + (p.path != null ? encodeURIComponent(p.path) : "")}.</p>
     */
    public String toJsExpression(String paramVar) {
        if (segments.isEmpty()) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (var seg : segments) {
            if (!first) sb.append(" + ");
            first = false;
            switch (seg) {
                case Segment.Literal lit -> {
                    sb.append("\"").append(jsEscape(lit.text())).append("\"");
                }
                case Segment.Param p -> {
                    String access = paramVar + "." + p.name();
                    if (p.optional()) {
                        sb.append("(").append(access).append(" != null ? encodeURIComponent(")
                          .append(access).append(") : ");
                        if (p.defaultValue() != null) {
                            sb.append("encodeURIComponent(\"").append(jsEscape(p.defaultValue())).append("\")");
                        } else {
                            sb.append("\"\"");
                        }
                        sb.append(")");
                    } else {
                        sb.append("encodeURIComponent(").append(access).append(")");
                    }
                }
            }
        }
        return sb.toString();
    }

    /** Escape a string literal for safe inclusion in a JS double-quoted string. */
    private String jsEscape(String s) {
        StringBuilder out = new StringBuilder(s.length() + 4);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"'  -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '/'  -> out.append("\\/");   // safe inside script tags too
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }

}
