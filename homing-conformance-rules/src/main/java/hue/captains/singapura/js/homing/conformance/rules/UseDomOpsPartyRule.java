package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RFC 0044 — a view must build its DOM through the <b>DomOpsParty branch</b>
 * ({@code branch.createElement(name, tag)} / {@code branch.createBranch(name)}),
 * not raw {@code document.createElement} and friends. Branch-minted elements are
 * <i>owned</i>: they carry stable names/handles and are torn down automatically
 * when the branch dissolves, which is what drives all widget cleanup. Raw
 * {@code document.create*} produces unowned nodes outside that lifecycle — the
 * party can't name, find, or reliably reclaim them.
 *
 * <p>Flags the DOM <b>factories</b> on {@code document}: {@code createElement},
 * {@code createElementNS}, {@code createTextNode}, {@code createDocumentFragment}.
 * The {@code document.} receiver is required, so {@code branch.createElement}
 * (the sanctioned owned factory) is never matched. Lookups
 * ({@code getElementById}/{@code querySelector}) are covered separately by
 * {@link ViewDoctrineRule}; this rule is about construction.</p>
 *
 * <p><b>Ungated</b>, like {@link NoInlineStyleRule}: it fires regardless of what
 * the module imports, so a view that mints DOM raw can't slip through by
 * importing nothing.</p>
 */
public record UseDomOpsPartyRule() implements JsRule {

    public static final UseDomOpsPartyRule INSTANCE = new UseDomOpsPartyRule();

    // document.createElement( / .createElementNS( / .createTextNode( / .createDocumentFragment(
    // Requires the `document` receiver, so `branch.createElement(...)` is not matched.
    private static final Pattern RAW_FACTORY = Pattern.compile(
            "document\\s*\\.\\s*(createElement|createElementNS|createTextNode|createDocumentFragment)\\s*\\(");

    @Override public RuleId      id()     { return new RuleId("use-dom-ops-party"); }
    @Override public String      intent() { return "A view must build DOM through the DomOpsParty branch (branch.createElement / branch.createBranch), not raw document.createElement - so elements are owned and reclaimed on branch dissolution."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("dom-ops-party"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        // Strip comments (positions preserved) so a comment mentioning the token
        // doesn't false-positive; only real calls count.
        List<String> lines = JsText.stripComments(module.lines());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (RAW_FACTORY.matcher(line).find()) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "raw DOM factory (use branch.createElement via the DomOpsParty, not document.create*): "
                                + line.trim(), i));
            }
        }
        return List.copyOf(findings);
    }
}
