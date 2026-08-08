package hue.captains.singapura.js.homing.conformance.engine;

import hue.captains.singapura.js.homing.conformance.rules.JsModuleType;
import hue.captains.singapura.js.homing.core.BundledExternalModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExternalModule;
import hue.captains.singapura.js.homing.core.ManagerInjector;
import hue.captains.singapura.js.homing.core.ModuleForm;

/**
 * RFC 0044 Phase 6 — classifies a module into the {@link JsModuleType} the
 * policy dispatches on. Derived from the mechanical {@link ModuleForm} plus the
 * cross-cutting markers, with the domain roles (primitive / secretary) deferred:
 *
 * <ul>
 *   <li>bundled or CDN-proxy external → {@code BUNDLED_EXTERNAL} (exempt third-party);</li>
 *   <li>{@link ManagerInjector} → {@code MANAGER_INJECTOR};</li>
 *   <li>CssGroup → {@code GENERATED_CSS};</li>
 *   <li>everything else (SvgGroup, SelfContent, resource-backed) → {@code CONSUMER}
 *       until domain classification lands.</li>
 * </ul>
 */
public final class ModuleClassifier {

    private ModuleClassifier() {}

    public static JsModuleType classify(EsModule<?> module) {
        if (module instanceof BundledExternalModule<?> || module instanceof ExternalModule<?>) {
            return JsModuleType.BUNDLED_EXTERNAL;
        }
        if (module instanceof ManagerInjector) {
            return JsModuleType.MANAGER_INJECTOR;
        }
        return switch (ModuleForm.of(module)) {
            case CSS_GROUP -> JsModuleType.GENERATED_CSS;
            case SVG_GROUP, SELF_CONTENT, RESOURCE_BACKED -> JsModuleType.CONSUMER;
            case BUNDLED_EXTERNAL -> JsModuleType.BUNDLED_EXTERNAL; // (already handled above)
        };
    }
}
