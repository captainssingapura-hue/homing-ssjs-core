package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RFC 0045 — colour comes from the theme via {@code var(--token)}; a served
 * module must not bake a literal colour value. This is the value-level twin of
 * {@link NoInlineStyleRule}: same ungated consumer scope, same reasoning. The
 * placement rules ({@code no-inline-style}, {@code no-raw-css}) stop styling in
 * the wrong place, but a hex that migrates into a typed {@code CssGroup} class
 * body escapes them entirely while leaving the widget unthemeable. This rule
 * follows the value wherever it lives.
 *
 * <p><b>Matches</b> hex colours ({@code #fff}, {@code #B0741A}, {@code #B0741Aff}
 * — exactly 3/6/8 hex digits at a word boundary) and functional colours
 * ({@code rgb(}, {@code rgba(}, {@code hsl(}, {@code hsla(}).</p>
 *
 * <p><b>Does not match</b> {@code var(--…)}, {@code transparent},
 * {@code currentColor}, {@code inherit}, {@code none}, or <b>named colours</b>
 * ({@code red}, {@code white}) — those read as ordinary words and would
 * false-positive on prose, ids, and data. The 3/6/8-digit + word-boundary shape
 * means non-colour {@code '#…'} uses (selectors like {@code '#root'}, fragment
 * hrefs, id concatenation) never match. Comment-stripped and ungated.</p>
 */
public record NoLiteralColorRule() implements JsRule {

    public static final NoLiteralColorRule INSTANCE = new NoLiteralColorRule();

    // Exactly 3, 6, or 8 hex digits, at a word boundary (so #root / #fragment don't match).
    private static final Pattern HEX = Pattern.compile(
            "#([0-9a-fA-F]{8}|[0-9a-fA-F]{6}|[0-9a-fA-F]{3})\\b");
    // Functional colour notations.
    private static final Pattern FUNC = Pattern.compile("\\b(rgba?|hsla?)\\s*\\(");

    @Override public RuleId      id()     { return new RuleId("no-literal-color"); }
    @Override public String      intent() { return "Colour must come from a theme token (var(--...)), not a baked literal - no hex (#fff / #B0741A) or rgb()/rgba()/hsl()/hsla() in a served module."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("theme-tokens-not-literals"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        // Strip comments (positions preserved) so a comment mentioning a colour
        // doesn't false-positive; only real literals count.
        List<String> lines = JsText.stripComments(module.lines());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (HEX.matcher(line).find() || FUNC.matcher(line).find()) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "literal colour (use a theme token var(--...), not a baked hex/rgb): " + line.trim(), i));
            }
        }
        return List.copyOf(findings);
    }
}
