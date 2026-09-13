package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0044 — the open classification: a downstream component library defines its
 * own {@link JsModuleType}s (implementing the interface) and registers each with
 * a rule set via {@code extendedWith}. Standard types keep their sealed-switch
 * dispatch through the framework policy (and can't be remapped); custom types
 * dispatch by dictionary lookup — and must sit in a lane: extension is not
 * exemption.
 */
class PolicyExtensionTest {

    /** A downstream's own module types — co-exist with the library's code. */
    enum LibType implements JsModuleType {
        GAME_LOOP("Game loop", "game-loop"),
        PRICER("Pricer", "pricer");
        private final String label, slug;
        LibType(String label, String slug) { this.label = label; this.slug = slug; }
        @Override public String label() { return label; }
        @Override public String slug()  { return slug; }
    }

    /** A stand-in for the library's own rule. */
    private record FrameRateRule() implements JsRule {
        static final FrameRateRule INSTANCE = new FrameRateRule();
        @Override public RuleId      id()     { return new RuleId("frame-rate"); }
        @Override public String      intent() { return "stand-in"; }
        @Override public DoctrineRef basis()  { return new DoctrineRef("stand-in"); }
        @Override public List<Finding> check(ServedModule m) { return List.of(); }
    }

    private static final JsRuleSet GAME_RULES = DefaultJsRulePolicy.domOwnerLane(
            new RuleSetId("game-loop"), "Game loop", FrameRateRule.INSTANCE);

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

    @Test
    void anExtensionRuleSetOutsideBothLanesIsRefused() {
        // Globals + the library's own rule, and nothing about the DOM either way:
        // a DOM-building module declared this type would escape every DOM rule.
        JsRuleSet noLane = new JsRuleSet(new RuleSetId("game-loop"), "Game loop",
                List.of(NoCdnImportRule.INSTANCE, MaxEffectiveLinesRule.INSTANCE, FrameRateRule.INSTANCE));
        assertFalse(CompositeJsRulePolicy.inLane(noLane));
        var ex = assertThrows(IllegalArgumentException.class,
                () -> DefaultJsRulePolicy.INSTANCE.extendedWith(Map.of(LibType.GAME_LOOP, noLane)));
        assertTrue(ex.getMessage().contains("sits in no lane"), ex.getMessage());

        // The empty set — the bundled-external shape — is likewise not a downstream's to grant.
        assertThrows(IllegalArgumentException.class, () -> DefaultJsRulePolicy.INSTANCE.extendedWith(
                Map.of(LibType.GAME_LOOP, JsRuleSet.empty(new RuleSetId("free"), "Free"))));

        // The no-DOM rule alone is not a lane without the globals.
        assertThrows(IllegalArgumentException.class, () -> DefaultJsRulePolicy.INSTANCE.extendedWith(
                Map.of(LibType.PRICER, new JsRuleSet(new RuleSetId("pricer"), "Pricer",
                        List.of(NoDomAccessRule.INSTANCE)))));
    }

    @Test
    void bothLanesAreAccepted() {
        // DOM-owner lane: the full discipline + the library's rule.
        assertTrue(CompositeJsRulePolicy.inLane(GAME_RULES));
        assertTrue(GAME_RULES.rules().containsAll(DefaultJsRulePolicy.DOM_OWNER_DISCIPLINE));
        assertTrue(GAME_RULES.rules().contains(FrameRateRule.INSTANCE));

        // Headless lane: globals + no-DOM + the library's rule — hand-built or by factory.
        JsRuleSet pricer = DefaultJsRulePolicy.headlessLane(new RuleSetId("pricer"), "Pricer", FrameRateRule.INSTANCE);
        JsRuleSet handBuilt = new JsRuleSet(new RuleSetId("pricer"), "Pricer",
                List.of(NoCdnImportRule.INSTANCE, MaxEffectiveLinesRule.INSTANCE, NoDomAccessRule.INSTANCE));
        assertTrue(CompositeJsRulePolicy.inLane(pricer));
        assertTrue(CompositeJsRulePolicy.inLane(handBuilt));
        JsRulePolicy policy = DefaultJsRulePolicy.INSTANCE.extendedWith(
                Map.of(LibType.GAME_LOOP, GAME_RULES, LibType.PRICER, pricer));
        assertSame(pricer, policy.rulesFor(LibType.PRICER));
    }
}
