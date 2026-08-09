package hue.captains.singapura.js.homing.conformance.rules;

import java.util.List;

/**
 * RFC 0044 — the first <b>global</b> rule: a served JS module must not exceed
 * {@value #LIMIT} <b>effective</b> lines — lines that are neither blank nor
 * comment-only, counted on the comment-stripped served text. Keeps modules small
 * and focused; a module that outgrows the limit is a prompt to split it.
 *
 * <p>Global because it holds regardless of a module's type (it sits in the
 * policy's shared/global layer, applied to every non-exempt type — only a
 * bundled external, which ships vendored, is out of scope). A whole-module
 * finding: not tied to a single line.</p>
 */
public record MaxEffectiveLinesRule() implements JsRule {

    public static final MaxEffectiveLinesRule INSTANCE = new MaxEffectiveLinesRule();

    /** The ceiling on effective (non-blank, non-comment) served lines. */
    public static final int LIMIT = 250;

    @Override public RuleId      id()     { return new RuleId("max-effective-lines"); }
    @Override public String      intent() { return "A served JS module must not exceed " + LIMIT + " effective (non-blank, non-comment) lines — keep modules small and focused."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("focused-served-modules"); }

    @Override
    public List<Finding> check(ServedModule module) {
        int effective = 0;
        for (String line : JsText.stripComments(module.lines())) {
            if (!line.isBlank()) effective++;
        }
        if (effective > LIMIT) {
            // Count-independent message: the finding fingerprint (used for baselining)
            // must not shift when the module's size changes — otherwise shrinking an
            // over-limit module toward the ceiling would spuriously re-fail the build.
            return List.of(new Finding(module.moduleClass(), id(),
                    "module exceeds the " + LIMIT + " effective-line limit — split it", -1));
        }
        return List.of();
    }
}
