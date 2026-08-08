package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;

import java.util.List;

/**
 * <b>The</b> framework policy — one fixed, opinionated mapping from {@link
 * JsModuleType} to its {@link JsRuleSet} (RFC 0044). Not a configuration
 * surface: a downstream with different needs authors its own policy from the
 * same constructs and runs it alongside, never a patch to this one.
 *
 * <p>The polymorphism is now real, not cosmetic. Every non-bundled module still
 * carries the CDN-free {@link #BASE} rule, but the DOM rules split by type:</p>
 * <ul>
 *   <li><b>DOM owners</b> ({@link JsModuleType#CONSUMER}, {@link
 *       JsModuleType#PRIMITIVE}) may touch the DOM but must never <em>wipe</em>
 *       branch-owned DOM — {@link NoDomDestructionRule}.</li>
 *   <li><b>No-DOM modules</b> ({@link JsModuleType#SECRETARY}, {@link
 *       JsModuleType#PURE_LOGIC}) must touch <em>no</em> DOM at all — the
 *       stricter {@link NoDomAccessRule}.</li>
 *   <li>Managers and generated CSS carry the base only; a bundled external is
 *       exempt.</li>
 * </ul>
 * <p>Functional Object: one {@code INSTANCE}.</p>
 */
public record DefaultJsRulePolicy() implements JsRulePolicy {

    public static final DefaultJsRulePolicy INSTANCE = new DefaultJsRulePolicy();

    /** Shared base — rules every non-exempt module is held to, whatever its type. */
    private static final List<JsRule> BASE = List.of(
            NoCdnImportRule.INSTANCE);

    /** DOM owners: base + may-not-wipe branch-owned DOM. */
    private static final List<JsRule> DOM_DISCIPLINE = concat(BASE,
            NoDomDestructionRule.INSTANCE);

    /**
     * The full consumer discipline: DOM ownership plus the ported view/manager
     * scanners — typed css, the href manager, no import redeclaration, and the
     * pure-view doctrines.
     */
    private static final List<JsRule> CONSUMER_DISCIPLINE = concat(DOM_DISCIPLINE,
            NoRawCssRule.INSTANCE,
            NoRawHrefRule.INSTANCE,
            NoManagerRedeclarationRule.INSTANCE,
            ViewDoctrineRule.INSTANCE);

    /** No-DOM modules: base + may-not-touch-the-DOM-at-all. */
    private static final List<JsRule> NO_DOM = concat(BASE,
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
            new JsRuleSet(new RuleSetId("manager-injector"), "ManagerInjector", BASE);
    private static final JsRuleSet GENERATED_CSS =
            new JsRuleSet(new RuleSetId("generated-css"), "Generated CSS", BASE);
    private static final JsRuleSet BUNDLED_EXTERNAL =
            JsRuleSet.empty(new RuleSetId("bundled-external"), "Bundled external");

    @Override
    public JsRuleSet rulesFor(JsModuleType type) {
        return switch (type) {
            case CONSUMER         -> CONSUMER;
            case PRIMITIVE        -> PRIMITIVE;
            case SECRETARY        -> SECRETARY;
            case PURE_LOGIC       -> PURE_LOGIC;
            case MANAGER_INJECTOR -> MANAGER_INJECTOR;
            case GENERATED_CSS    -> GENERATED_CSS;
            case BUNDLED_EXTERNAL -> BUNDLED_EXTERNAL;
        };
    }

    private static List<JsRule> concat(List<JsRule> base, JsRule... extra) {
        var out = new java.util.ArrayList<JsRule>(base);
        java.util.Collections.addAll(out, extra);
        return List.copyOf(out);
    }
}
