package hue.captains.singapura.js.homing.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the typed {@code nav} const for an EsModule's compiled JS.
 *
 * <p>For every {@link AppLink} in the module's imports, an entry is added to a
 * frozen {@code nav} object. The shape differs slightly between target kinds:</p>
 *
 * <ul>
 *   <li><b>{@link AppModule} target</b> — calls a shared {@code _homingBuildAppUrl}
 *       helper that emits {@code /app?app=&lt;simpleName&gt;&...params...}: the
 *       params the caller passed and nothing else. It used to carry the current
 *       page's {@code theme}/{@code locale} over; RFC 0064 D20 — a link is what
 *       its author wrote — retired that stamp from {@code HrefManager}, and this
 *       was its other half. A {@code theme} or {@code locale} the caller passes
 *       is written like any other param.</li>
 *   <li><b>{@link ProxyApp} target</b> — interpolates the proxy's
 *       {@link ProxyApp#urlTemplate()} via {@link UrlTemplate#toJsExpression(String)},
 *       producing a function that builds the external URL in the browser.</li>
 * </ul>
 *
 * <p>The JS-side identifier of each entry is the simple Java class name of the
 * target Linkable — {@code nav.PitchDeck}, {@code nav.GitHubProxy}, etc.
 * Tracks Java refactors automatically.</p>
 *
 * <p>If no {@code AppLink} imports are present, returns an empty list — no nav
 * block is emitted (and no helper either).</p>
 *
 * <p>Introduced in RFC 0001 Step 05.</p>
 */
public final class NavWriter {

    private final Map<Importable, ModuleImports<?>> allImports;

    public NavWriter(Map<Importable, ModuleImports<?>> allImports) {
        this.allImports = allImports;
    }

    public List<String> write() {
        // Collect AppLink targets, deduplicated, in insertion order.
        Map<Linkable, Boolean> targets = new LinkedHashMap<>();
        for (var entry : allImports.entrySet()) {
            if (!(entry.getKey() instanceof Linkable target)) continue;
            for (var exp : entry.getValue().allImports()) {
                if (exp instanceof AppLink<?>) {
                    targets.putIfAbsent(target, true);
                    break;
                }
            }
        }
        if (targets.isEmpty()) return List.of();

        List<String> out = new ArrayList<>();
        out.add("// === homing generated nav (RFC 0001) ===");
        out.add("function _homingBuildAppUrl(simpleName, params) {");
        out.add("    var u = \"/app?app=\" + encodeURIComponent(simpleName);");
        out.add("    if (params) for (var k in params) {");
        out.add("        if (params[k] != null) u += \"&\" + encodeURIComponent(k) + \"=\" + encodeURIComponent(String(params[k]));");
        out.add("    }");
        out.add("    return u;");
        out.add("}");
        out.add("const nav = Object.freeze({");

        var entries = new ArrayList<>(targets.keySet());
        for (int i = 0; i < entries.size(); i++) {
            Linkable target = entries.get(i);
            String identifier = target.getClass().getSimpleName();
            String comma = (i < entries.size() - 1) ? "," : "";
            switch (target) {
                case AppModule<?, ?> ignored -> {
                    out.add("    " + identifier
                          + ": function(p) { return _homingBuildAppUrl(\""
                          + jsString(target.simpleName()) + "\", p); }" + comma);
                }
                case ProxyApp<?, ?> proxy -> {
                    var tpl = UrlTemplate.compile(proxy.urlTemplate(), proxy.paramsType());
                    String body = tpl.toJsExpression("p");
                    out.add("    " + identifier
                          + ": function(p) { return " + body + "; }" + comma);
                }
                default -> throw new IllegalStateException(
                        "Unexpected Linkable subtype: " + target.getClass().getName());
            }
        }
        out.add("});");
        out.add("// === end homing generated nav ===");
        return out;
    }

    /** Escape a string for safe inclusion in a JS double-quoted literal. */
    private static String jsString(String s) {
        StringBuilder out = new StringBuilder(s.length() + 4);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"'  -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }
}
