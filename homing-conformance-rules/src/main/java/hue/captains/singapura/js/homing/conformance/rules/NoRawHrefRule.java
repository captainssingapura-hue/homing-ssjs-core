package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RFC 0044 Phase 7 — ported from {@code HrefConformanceTest} (RFC 0001 §6.2):
 * the literal {@code href} may appear in consumer JS only as the injected
 * manager identifier ({@code href.set()}, {@code href.toAttr()}, …). Raw href
 * authoring — an {@code href=} attribute, a {@code .href} property, the string
 * {@code "href"}, {@code window.location}, {@code window.open()}, or
 * {@code setAttribute("href", …)} — is forbidden; each has a {@code href.*}
 * replacement.
 *
 * <p>Comments are stripped but string literals are preserved: an HTML fragment
 * {@code '<a href="…"'} inside a JS string is exactly what this targets. The
 * auto-injected {@code import { HrefManagerInstance as href }} prologue matches
 * none of the forbidden patterns, so it never self-trips.</p>
 */
public record NoRawHrefRule() implements JsRule {

    public static final NoRawHrefRule INSTANCE = new NoRawHrefRule();

    private static final List<Pattern> FORBIDDEN = List.of(
            Pattern.compile("\\bhref\\s*="),
            Pattern.compile("\\.href\\b"),
            Pattern.compile("[\"']href[\"']"),
            Pattern.compile("\\bwindow\\.location\\b"),
            Pattern.compile("\\bwindow\\.open\\s*\\("),
            Pattern.compile("setAttribute\\s*\\(\\s*[\"']href"));

    @Override public RuleId      id()     { return new RuleId("no-raw-href"); }
    @Override public String      intent() { return "The literal 'href' may appear only as the injected manager identifier; raw href authoring is forbidden (use href.*)."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("href-manager"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        List<String> raw = module.lines();
        List<String> stripped = JsText.stripComments(raw);
        for (int i = 0; i < stripped.size(); i++) {
            for (Pattern p : FORBIDDEN) {
                if (p.matcher(stripped.get(i)).find()) {
                    findings.add(new Finding(module.moduleClass(), id(),
                            "raw href operation (use href.* API): " + raw.get(i).trim(), i));
                    break;
                }
            }
        }
        return List.copyOf(findings);
    }
}
