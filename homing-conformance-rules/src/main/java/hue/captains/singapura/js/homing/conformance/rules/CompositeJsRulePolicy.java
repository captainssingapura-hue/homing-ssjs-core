package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.util.List;
import java.util.Map;

/**
 * RFC 0044 — a {@link JsRulePolicy} that composes the framework's fixed policy
 * with a downstream's extension types. It is the "unsealed branch" of the
 * dispatch: a {@link StandardJsModuleType} routes to the framework {@code base}
 * (its exhaustive switch); any other {@link JsModuleType} routes to the {@code
 * extensions} dictionary. This keeps the framework closed for modification
 * (standard types can't be remapped) and open for extension (a component library
 * adds its own types + rule sets).
 *
 * <p><b>Extension is not exemption.</b> An extension rule set must sit in one of
 * the framework's two <b>lanes</b>: the DOM-owner lane (it carries every rule of
 * {@link DefaultJsRulePolicy#DOM_OWNER_DISCIPLINE}) or the headless lane (it
 * carries {@link NoDomAccessRule}); either way it carries the global rules. A
 * set that does neither — a "game loop" type held only to a frame-rate rule
 * while its modules build and style DOM — is refused at construction. The
 * lane is the framework's floor; what a downstream adds on top is its own.
 * {@link DefaultJsRulePolicy#domOwnerLane} / {@link DefaultJsRulePolicy#headlessLane}
 * build a conforming set directly.</p>
 *
 * @param base       the framework policy (governs the standard types)
 * @param extensions downstream types → their rule sets, each in a lane
 */
public record CompositeJsRulePolicy(JsRulePolicy base, Map<JsModuleType, JsRuleSet> extensions)
        implements JsRulePolicy {

    public CompositeJsRulePolicy {
        extensions = Map.copyOf(extensions);
        extensions.forEach(CompositeJsRulePolicy::requireLane);
    }

    @Override
    public JsRuleSet rulesFor(JsModuleType type) {
        if (type instanceof StandardJsModuleType) {
            return base.rulesFor(type);
        }
        JsRuleSet set = extensions.get(type);
        if (set == null) {
            throw new IllegalArgumentException(
                    "no rule set registered for extension type '" + type.slug()
                            + "' — add it to the extensions map passed to extendedWith(...).");
        }
        return set;
    }

    /** True iff {@code set} sits in a lane: global rules + (DOM-owner discipline | no-DOM). */
    public static boolean inLane(JsRuleSet set) {
        List<JsRule> rules = set.rules();
        if (!rules.containsAll(DefaultJsRulePolicy.GLOBAL)) return false;
        return rules.containsAll(DefaultJsRulePolicy.DOM_OWNER_DISCIPLINE)
                || rules.contains(NoDomAccessRule.INSTANCE);
    }

    private static void requireLane(JsModuleType type, JsRuleSet set) {
        if (inLane(set)) return;
        throw new IllegalArgumentException(
                "extension rule set '" + set.id().value() + "' for type '" + type.slug()
                        + "' sits in no lane. An extension type extends the framework's discipline, it does"
                        + " not opt out of it: carry the global rules plus either the full DOM-owner discipline"
                        + " (DefaultJsRulePolicy.DOM_OWNER_DISCIPLINE — the module builds or styles DOM) or"
                        + " NoDomAccessRule (the module is headless). DefaultJsRulePolicy.domOwnerLane(...) /"
                        + " headlessLane(...) build one.");
    }
}
