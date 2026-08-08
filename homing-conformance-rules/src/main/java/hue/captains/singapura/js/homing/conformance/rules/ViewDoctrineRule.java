package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RFC 0044 Phase 7 — ported from {@code DoctrineConformanceTest}: the two
 * universal view doctrines on consumer JS.
 *
 * <ul>
 *   <li><b>Pure-Component Views</b> — no HTML tag literals ({@code '<div}, {@code
 *       "<a}, …) and no {@code innerHTML}/{@code outerHTML} writes (except
 *       clearing to {@code ""}, which the no-destruction rule governs).</li>
 *   <li><b>Owned References</b> — no stringly-typed DOM lookups
 *       ({@code document.getElementById}, {@code .querySelector},
 *       {@code .querySelectorAll}).</li>
 * </ul>
 *
 * <p>Runs on the served text with comments stripped (string literals preserved —
 * an HTML literal lives in a string). The SPA-scoped <i>Managed DOM Ops</i> and
 * the non-static <i>Methods Over Props</i> doctrines are deliberately not here.</p>
 */
public record ViewDoctrineRule() implements JsRule {

    public static final ViewDoctrineRule INSTANCE = new ViewDoctrineRule();

    private static final List<Pattern> HTML_LITERAL = List.of(
            Pattern.compile("['\"]<[a-zA-Z!]"));

    // Possessive quantifiers (\s*+) so the whitespace can't back-track and leave
    // the empty-string lookahead evaluating at a space — `innerHTML = ""` (the
    // permitted clear, governed by the no-destruction rule) must NOT match.
    private static final List<Pattern> HTML_WRITE = List.of(
            Pattern.compile("\\.innerHTML\\s*+=\\s*+(?![\"']\\s*+[\"'])"),
            Pattern.compile("\\.outerHTML\\s*+=\\s*+(?![\"']\\s*+[\"'])"));

    private static final List<Pattern> LOOKUP = List.of(
            Pattern.compile("document\\s*\\.\\s*getElementById\\s*\\("),
            Pattern.compile("\\.querySelector\\s*\\("),
            Pattern.compile("\\.querySelectorAll\\s*\\("));

    @Override public RuleId      id()     { return new RuleId("view-doctrine"); }
    @Override public String      intent() { return "Consumer views must be pure (no HTML literals, no innerHTML/outerHTML writes) and use owned references (no DOM lookups)."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("pure-component-views"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        List<String> raw = module.lines();
        List<String> stripped = JsText.stripComments(raw);
        for (int i = 0; i < stripped.size(); i++) {
            String line = stripped.get(i);
            String reason = null;
            if (matchesAny(line, HTML_LITERAL))    reason = "HTML tag literal (Pure-Component Views)";
            else if (matchesAny(line, HTML_WRITE)) reason = "innerHTML/outerHTML write (Pure-Component Views)";
            else if (matchesAny(line, LOOKUP))     reason = "DOM lookup (Owned References)";
            if (reason != null) {
                findings.add(new Finding(module.moduleClass(), id(),
                        reason + ": " + raw.get(i).trim(), i));
            }
        }
        return List.copyOf(findings);
    }

    private static boolean matchesAny(String line, List<Pattern> patterns) {
        for (Pattern p : patterns) if (p.matcher(line).find()) return true;
        return false;
    }
}
