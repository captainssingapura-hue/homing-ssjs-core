package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.util.List;
import java.util.Map;

/**
 * <b>The</b> framework policy — the fixed, opinionated mapping from each {@link
 * StandardJsModuleType} to its {@link JsRuleSet} (RFC 0044). Because the standard
 * types are a sealed set (an enum), this dispatch is an exhaustive {@code switch}
 * — the compiler flags a missing case when a standard type is added.
 *
 * <p>The DOM rules split by type: <b>DOM owners</b> ({@code CONSUMER} /
 * {@code PRIMITIVE}) may touch the DOM but never <em>wipe</em> branch-owned DOM
 * ({@link NoDomDestructionRule}); <b>no-DOM modules</b> ({@code SECRETARY} /
 * {@code PURE_LOGIC}) must touch no DOM at all ({@link NoDomAccessRule}); managers
 * and generated CSS carry the base only; a bundled external is exempt.</p>
 *
 * <p><b>Extend, don't patch.</b> This policy is not a configuration surface — a
 * downstream never edits the framework's types or rules. Instead it defines its
 * own {@link JsModuleType}s (implementing the interface) with their own rule
 * sets and composes them on top via {@link #extendedWith}: standard types keep
 * dispatching through this switch, downstream types through a dictionary lookup
 * ({@link CompositeJsRulePolicy}). Functional Object: one {@code INSTANCE}.</p>
 */
public record DefaultJsRulePolicy() implements JsRulePolicy {

    public static final DefaultJsRulePolicy INSTANCE = new DefaultJsRulePolicy();

    /**
     * The <b>global</b> layer — rules every non-exempt module is held to, whatever
     * its type. This is the shared root every type's rule set is composed from.
     */
    private static final List<JsRule> GLOBAL = List.of(
            NoCdnImportRule.INSTANCE,
            MaxEffectiveLinesRule.INSTANCE);

    /** DOM owners: global + may-not-wipe branch-owned DOM. */
    private static final List<JsRule> DOM_DISCIPLINE = concat(GLOBAL,
            NoDomDestructionRule.INSTANCE);

    /**
     * The full consumer discipline: DOM ownership plus the ported view/manager
     * scanners — typed css, the href manager, no import redeclaration, and the
     * pure-view doctrines.
     */
    private static final List<JsRule> CONSUMER_DISCIPLINE = concat(DOM_DISCIPLINE,
            NoRawCssRule.INSTANCE,
            NoInlineStyleRule.INSTANCE,
            NoRawHrefRule.INSTANCE,
            NoManagerRedeclarationRule.INSTANCE,
            ViewDoctrineRule.INSTANCE);

    /** No-DOM modules: global + may-not-touch-the-DOM-at-all. */
    private static final List<JsRule> NO_DOM = concat(GLOBAL,
            NoDomAccessRule.INSTANCE);

    private static final JsRuleSet CONSUMER =
            new JsRuleSet(new RuleSetId("consumer"), "Consumer", CONSUMER_DISCIPLINE);
    private static final JsRuleSet PRIMITIVE =
            new JsRuleSet(new RuleSetId("primitive"), "Primitive", DOM_DISCIPLINE);
    private static final JsRuleSet SECRETARY =
            new JsRuleSet(new RuleSetId("secretary"), "Secretary", NO_DOM);
    private static final JsRuleSet PURE_LOGIC =
            new JsRuleSet(new RuleSetId("pure-logic"), "Pure logic", NO_DOM);
    private static final JsRuleSet MANAGER_INJECTOR =
            new JsRuleSet(new RuleSetId("manager-injector"), "ManagerInjector", GLOBAL);
    private static final JsRuleSet GENERATED_CSS =
            new JsRuleSet(new RuleSetId("generated-css"), "Generated CSS", GLOBAL);
    private static final JsRuleSet BUNDLED_EXTERNAL =
            JsRuleSet.empty(new RuleSetId("bundled-external"), "Bundled external");

    @Override
    public JsRuleSet rulesFor(JsModuleType type) {
        if (type instanceof StandardJsModuleType std) {
            return switch (std) {
                case CONSUMER         -> CONSUMER;
                case PRIMITIVE        -> PRIMITIVE;
                case SECRETARY        -> SECRETARY;
                case PURE_LOGIC       -> PURE_LOGIC;
                case MANAGER_INJECTOR -> MANAGER_INJECTOR;
                case GENERATED_CSS    -> GENERATED_CSS;
                case BUNDLED_EXTERNAL -> BUNDLED_EXTERNAL;
            };
        }
        throw new IllegalArgumentException(
                "DefaultJsRulePolicy governs only the framework's standard types; got extension type '"
                        + type.slug() + "'. Register it with DefaultJsRulePolicy.INSTANCE.extendedWith(...).");
    }

    /**
     * This framework policy plus a downstream's own types → rule sets. Standard
     * types keep their exhaustive-switch dispatch here; the {@code extensions}
     * are dispatched by lookup. A downstream may not remap a standard type.
     */
    public JsRulePolicy extendedWith(Map<JsModuleType, JsRuleSet> extensions) {
        return new CompositeJsRulePolicy(this, extensions);
    }

    private static List<JsRule> concat(List<JsRule> base, JsRule... extra) {
        var out = new java.util.ArrayList<JsRule>(base);
        java.util.Collections.addAll(out, extra);
        return List.copyOf(out);
    }
}
