package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RFC 0044 — a DOM owner styles through the <b>typed</b> {@code css} manager and
 * a served {@code CssGroup}: never raw class operations ({@code .className =},
 * {@code .classList.add/remove/toggle/replace/contains(}), and never a
 * stylesheet minted from JavaScript (a {@code <style>} element whose text is a
 * CSS string). Use {@code css.setClass() / addClass() / removeClass() /
 * toggleClass()} with the generated class constants, and author the sheet as a
 * {@code CssGroup} the server serves.
 *
 * <p><b>Ungated.</b> This rule used to fire only when the served prologue showed
 * the {@code css} manager had been injected — i.e. only if the module imported
 * a {@code CssGroup}. That gate was the primitive exemption in disguise: the
 * modules with no {@code CssGroup} import were exactly the ones minting their
 * own {@code <style>} tag and writing {@code className} raw, and they passed
 * green. Like {@link NoInlineStyleRule}, it now fires regardless of what the
 * module imports; the module that has no typed class to use is precisely the
 * one being told to adopt one.</p>
 *
 * <p>The manager implementations themselves ({@code CssClassManager}) are the
 * sanctioned exception, carried as an explicit {@link Allowance} — not as a
 * pattern the rule silently skips.</p>
 */
public record NoRawCssRule() implements JsRule {

    public static final NoRawCssRule INSTANCE = new NoRawCssRule();

    private static final List<Pattern> RAW_CSS = List.of(
            Pattern.compile("\\.className\\s*="),
            Pattern.compile("\\.classList\\s*\\.\\s*add\\s*\\("),
            Pattern.compile("\\.classList\\s*\\.\\s*remove\\s*\\("),
            Pattern.compile("\\.classList\\s*\\.\\s*toggle\\s*\\("),
            Pattern.compile("\\.classList\\s*\\.\\s*replace\\s*\\("),
            Pattern.compile("\\.classList\\s*\\.\\s*contains\\s*\\("));

    // A <style> element minted from JS — raw (document.createElement('style'))
    // or branch-minted (branch.createElement(name, 'style')). Either way the
    // sheet is an untyped CSS string the theme system never sees.
    private static final Pattern STYLE_ELEMENT = Pattern.compile(
            "\\.createElement\\s*\\(\\s*(?:[^,()]+,\\s*)?['\"]style['\"]\\s*\\)");

    @Override public RuleId      id()     { return new RuleId("no-raw-css"); }
    @Override public String      intent() { return "A DOM owner must use the css.* API with typed CssGroup classes - no raw className/classList operations, and no <style> element minted from JS."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("type-safe-css"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        List<String> raw = module.lines();
        List<String> stripped = JsText.stripComments(raw);
        for (int i = 0; i < stripped.size(); i++) {
            String line = stripped.get(i);
            if (STYLE_ELEMENT.matcher(line).find()) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "stylesheet minted from JS (author a CssGroup the server serves, not a <style> element): "
                                + raw.get(i).trim(), i));
                continue;
            }
            for (Pattern p : RAW_CSS) {
                if (p.matcher(line).find()) {
                    findings.add(new Finding(module.moduleClass(), id(),
                            "raw CSS operation (use css.* API): " + raw.get(i).trim(), i));
                    break;
                }
            }
        }
        return List.copyOf(findings);
    }
}
