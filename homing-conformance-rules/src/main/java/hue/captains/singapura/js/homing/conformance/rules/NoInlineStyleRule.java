package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RFC 0044 — a view must style through <b>typed CSS classes</b> (a {@code
 * CssGroup} applied via {@code css.addClass(...)}), never inline. Inline styling
 * (writing {@code element.style.*} or {@code setAttribute('style', …)}) bypasses
 * the theme system entirely: the value is baked into the DOM, so it ignores the
 * active theme's semantic tokens, breaks light/dark, and sits outside the shared
 * visual language. A widget that styles fully inline is invisible to theming —
 * which is exactly why it must be forbidden for new work.
 *
 * <p><b>Ungated on purpose.</b> Unlike {@link NoRawCssRule} (which only applies
 * once the css manager is injected), this rule fires regardless: a module that
 * styles inline and imports <i>no</i> {@code CssGroup} is precisely the case that
 * used to pass silently. Forcing the finding pushes it to adopt a typed
 * {@code CssGroup}.</p>
 *
 * <p><b>Escape hatch for genuinely dynamic values:</b> a runtime-computed value
 * that can't be a static class (a meter width, a transform from live data) sets
 * a CSS custom property via {@code element.style.setProperty('--x', v)} — a
 * method call, not a {@code .style.x =} write — which a typed class then consumes
 * as {@code var(--x)}. {@code setProperty} is deliberately NOT matched here.</p>
 */
public record NoInlineStyleRule() implements JsRule {

    public static final NoInlineStyleRule INSTANCE = new NoInlineStyleRule();

    // .style.color = , .style.cssText = , .style.width = …  (an assignment, so
    // `=` but not `==`; setProperty(...) is a call, not an assignment, so excluded).
    private static final Pattern STYLE_ASSIGN =
            Pattern.compile("\\.style\\.[A-Za-z][A-Za-z0-9]*\\s*=(?!=)");
    // setAttribute('style', …) / setAttribute("style", …)
    private static final Pattern SET_ATTR_STYLE =
            Pattern.compile("\\.setAttribute\\(\\s*['\"]style['\"]");
    // RFC 0045 — the setProperty escape hatch is for CUSTOM properties only.
    // element.style.setProperty('color', …) is a raw inline write in disguise;
    // element.style.setProperty('--x', v) (a custom prop a typed class consumes
    // as var(--x)) is the sanctioned dynamic path and is NOT matched.
    private static final Pattern SET_PROPERTY_NON_VAR =
            Pattern.compile("\\.style\\.setProperty\\(\\s*['\"](?!--)");

    @Override public RuleId      id()     { return new RuleId("no-inline-style"); }
    @Override public String      intent() { return "A view must style via typed CSS classes (css.addClass + a CssGroup), not inline element.style / setAttribute('style') - so themes and the shared visual language apply."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("typed-css-not-inline"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        // Strip comments (positions preserved) so a comment mentioning .style
        // doesn't false-positive; only real writes count.
        List<String> lines = JsText.stripComments(module.lines());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (STYLE_ASSIGN.matcher(line).find()
                    || SET_ATTR_STYLE.matcher(line).find()
                    || SET_PROPERTY_NON_VAR.matcher(line).find()) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "inline style write (use a typed css class via css.addClass, not element.style): "
                                + line.trim(), i));
            }
        }
        return List.copyOf(findings);
    }
}
