package hue.captains.singapura.js.homing.conformance.rules;

import java.util.List;

/**
 * <b>The</b> framework policy — one fixed, opinionated mapping from {@link
 * JsModuleType} to its {@link JsRuleSet} (RFC 0044). Not a configuration
 * surface: a downstream with different needs authors its own policy from the
 * same constructs and runs it alongside, never a patch to this one.
 *
 * <p>Seed state (this is filled out in Phase 5 as the scanners are ported). For
 * now every non-bundled type carries the CDN-free rule; a bundled external is
 * exempt — enough to make the polymorphism real and visible in the studio.
 * Functional Object: one {@code INSTANCE}.</p>
 */
public record DefaultJsRulePolicy() implements JsRulePolicy {

    public static final DefaultJsRulePolicy INSTANCE = new DefaultJsRulePolicy();

    // Shared base — rules every non-exempt module is held to (grows in Phase 5).
    private static final List<JsRule> BASE = List.of(NoCdnImportRule.INSTANCE);

    private static final JsRuleSet CONSUMER =
            new JsRuleSet(new RuleSetId("consumer"), "Consumer", BASE);
    private static final JsRuleSet PRIMITIVE =
            new JsRuleSet(new RuleSetId("primitive"), "Primitive", BASE);
    private static final JsRuleSet SECRETARY =
            new JsRuleSet(new RuleSetId("secretary"), "Secretary", BASE);
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
            case MANAGER_INJECTOR -> MANAGER_INJECTOR;
            case GENERATED_CSS    -> GENERATED_CSS;
            case BUNDLED_EXTERNAL -> BUNDLED_EXTERNAL;
        };
    }
}
