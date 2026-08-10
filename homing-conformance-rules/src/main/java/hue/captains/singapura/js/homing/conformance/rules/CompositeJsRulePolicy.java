package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

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
 * @param base       the framework policy (governs the standard types)
 * @param extensions downstream types → their rule sets
 */
public record CompositeJsRulePolicy(JsRulePolicy base, Map<JsModuleType, JsRuleSet> extensions)
        implements JsRulePolicy {

    public CompositeJsRulePolicy {
        extensions = Map.copyOf(extensions);
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
}
