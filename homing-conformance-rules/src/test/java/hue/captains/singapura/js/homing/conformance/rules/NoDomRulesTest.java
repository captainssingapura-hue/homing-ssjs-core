package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 7 proof — the two DOM rules and the polymorphism between them. A no-DOM
 * module ({@link JsModuleType#PURE_LOGIC} / {@link JsModuleType#SECRETARY}) is
 * held to {@link NoDomAccessRule} (no DOM reference at all); a DOM owner
 * ({@link JsModuleType#CONSUMER} / {@link JsModuleType#PRIMITIVE}) is held only
 * to {@link NoDomDestructionRule} (no wholesale wipe) and may otherwise touch
 * the DOM. The {@link DefaultJsRulePolicy} is the dispatch under test.
 */
class NoDomRulesTest {

    private static ServedModule served(String cls, JsModuleType type, String... lines) {
        return new ServedModule(cls, type, JsSource.of(lines));
    }

    @Test
    void noDomAccessFlagsAnyDomReferenceButNotComments() {
        var offending = served("demo.Store", StandardJsModuleType.PURE_LOGIC,
                "const el = document.createElement('div');",
                "el.classList.add('x');");
        assertFalse(NoDomAccessRule.INSTANCE.check(offending).isEmpty(),
                "a no-DOM module touching the DOM must be flagged");

        var clean = served("demo.Store", StandardJsModuleType.PURE_LOGIC,
                "// this reducer never touches document.createElement",
                "export function reduce(state, ev) { return { ...state }; }");
        assertTrue(NoDomAccessRule.INSTANCE.check(clean).isEmpty(),
                "a DOM mention only in a comment must not be flagged");
    }

    @Test
    void policyHoldsNoDomModulesToNoDomAccessButDomOwnersOnlyToNoWipe() {
        var policy = DefaultJsRulePolicy.INSTANCE;
        String[] domBody = {
                "const el = branch.createElement('box', 'div');",
                "host.appendChild(el);"
        };

        // A consumer legitimately builds DOM through the DomOpsParty branch — no
        // inline style, no wipe, no raw document.create*, so it is compliant.
        var consumer = served("demo.Widget", StandardJsModuleType.CONSUMER, domBody);
        assertTrue(policy.rulesFor(StandardJsModuleType.CONSUMER).checkAll(consumer).isEmpty(),
                "a consumer building DOM via the branch (no wipe) must be compliant");

        // The same body in a pure-logic module is a broken no-DOM promise
        // (NoDomAccessRule flags createElement/appendChild whatever the receiver).
        var logic = served("demo.Store", StandardJsModuleType.PURE_LOGIC, domBody);
        assertFalse(policy.rulesFor(StandardJsModuleType.PURE_LOGIC).checkAll(logic).isEmpty(),
                "the same DOM code in a no-DOM module must be flagged");

        // And a wholesale wipe trips the consumer's no-destruction rule.
        var wipe = served("demo.Widget", StandardJsModuleType.CONSUMER, "host.innerHTML = \"\";");
        assertFalse(policy.rulesFor(StandardJsModuleType.CONSUMER).checkAll(wipe).isEmpty(),
                "a wholesale wipe in a DOM owner must be flagged");
    }
}
