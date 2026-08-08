package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RFC 0044 Phase 7 — ported from {@code CssConformanceTest}: a module that has
 * the type-safe {@code css} manager available must not bypass it with raw class
 * operations ({@code .className =}, {@code .classList.add/remove/toggle/replace/
 * contains(}). Use {@code css.setClass() / addClass() / removeClass() /
 * toggleClass()} with the generated typed class constants instead.
 *
 * <p>Scoped exactly as the original was ("modules with non-empty cssGroups()"):
 * the rule fires only when the served prologue shows the {@code css} manager was
 * auto-injected — {@code import { CssClassManagerInstance as css }}. A module
 * with no typed classes to use has no {@code css.*} alternative and is not
 * policed.</p>
 */
public record NoRawCssRule() implements JsRule {

    public static final NoRawCssRule INSTANCE = new NoRawCssRule();

    /** The served-text marker that the {@code css} manager was injected (see EsModuleGetAction). */
    private static final String CSS_INJECTED = "CssClassManagerInstance as css";

    private static final List<Pattern> RAW_CSS = List.of(
            Pattern.compile("\\.className\\s*="),
            Pattern.compile("\\.classList\\s*\\.\\s*add\\s*\\("),
            Pattern.compile("\\.classList\\s*\\.\\s*remove\\s*\\("),
            Pattern.compile("\\.classList\\s*\\.\\s*toggle\\s*\\("),
            Pattern.compile("\\.classList\\s*\\.\\s*replace\\s*\\("),
            Pattern.compile("\\.classList\\s*\\.\\s*contains\\s*\\("));

    @Override public RuleId      id()     { return new RuleId("no-raw-css"); }
    @Override public String      intent() { return "A module with the css manager available must use the css.* API, not raw className/classList operations."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("type-safe-css"); }

    @Override
    public List<Finding> check(ServedModule module) {
        if (!module.text().contains(CSS_INJECTED)) return List.of();
        var findings = new ArrayList<Finding>();
        List<String> raw = module.lines();
        List<String> stripped = JsText.stripComments(raw);
        for (int i = 0; i < stripped.size(); i++) {
            for (Pattern p : RAW_CSS) {
                if (p.matcher(stripped.get(i)).find()) {
                    findings.add(new Finding(module.moduleClass(), id(),
                            "raw CSS operation (use css.* API): " + raw.get(i).trim(), i));
                    break;
                }
            }
        }
        return List.copyOf(findings);
    }
}
