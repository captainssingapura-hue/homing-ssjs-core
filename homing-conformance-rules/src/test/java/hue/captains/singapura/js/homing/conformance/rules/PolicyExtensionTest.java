package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RFC 0044 — the open classification: a downstream component library defines its
 * own {@link JsModuleType}s (implementing the interface) and registers each with
 * a rule set via {@code extendedWith}. Standard types keep their sealed-switch
 * dispatch through the framework policy (and can't be remapped); custom types
 * dispatch by dictionary lookup.
 */
class PolicyExtensionTest {

    /** A downstream's own module type — co-exists with the library's code. */
    enum LibType implements JsModuleType {
        GAME_LOOP("Game loop", "game-loop");
        private final String label, slug;
        LibType(String label, String slug) { this.label = label; this.slug = slug; }
        @Override public String label() { return label; }
        @Override public String slug()  { return slug; }
    }

    private static final JsRuleSet GAME_RULES =
            new JsRuleSet(new RuleSetId("game-loop"), "Game loop", List.of(NoCdnImportRule.INSTANCE));

    @Test
    void extendedPolicyRoutesStandardToFrameworkAndCustomToItsRuleSet() {
        JsRulePolicy policy = DefaultJsRulePolicy.INSTANCE
                .extendedWith(Map.of(LibType.GAME_LOOP, GAME_RULES));

        // A standard type still routes through the framework's exhaustive switch...
        assertEquals("consumer", policy.rulesFor(StandardJsModuleType.CONSUMER).id().value());
        // ...and cannot be remapped by an extension.
        assertSame(DefaultJsRulePolicy.INSTANCE.rulesFor(StandardJsModuleType.PURE_LOGIC),
                policy.rulesFor(StandardJsModuleType.PURE_LOGIC));

        // The downstream type routes to its own rule set (dictionary lookup).
        assertSame(GAME_RULES, policy.rulesFor(LibType.GAME_LOOP));
    }

    @Test
    void anUnregisteredExtensionTypeFailsLoud() {
        // The framework policy alone knows nothing of a downstream type.
        assertThrows(IllegalArgumentException.class,
                () -> DefaultJsRulePolicy.INSTANCE.rulesFor(LibType.GAME_LOOP));
        // Nor a composite that didn't register it.
        JsRulePolicy empty = DefaultJsRulePolicy.INSTANCE.extendedWith(Map.of());
        assertThrows(IllegalArgumentException.class, () -> empty.rulesFor(LibType.GAME_LOOP));
    }
}
