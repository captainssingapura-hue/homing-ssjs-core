package hue.captains.singapura.js.homing.conformance.engine;

import hue.captains.singapura.js.homing.component.UiComponent;
import hue.captains.singapura.js.homing.core.BundledExternalModule;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExternalModule;
import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.ManagerInjector;
import hue.captains.singapura.js.homing.core.ModuleForm;
import hue.captains.singapura.js.homing.core.ModuleForms;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * RFC 0044 Phase 6/7 — classifies a module into the {@link JsModuleType} the
 * policy dispatches on. The <b>domain</b> role (primitive / secretary / pure
 * logic) is <em>declared</em> on the {@link CrateEntry}; when present it wins - a downstream's own
 * type too. A widget is never declared out of its discipline all the same: whatever its type, the
 * engine holds a module that exports a widget to the widget rule as well ({@link #exportsAWidget}).
 * Absent a declaration, the type is derived structurally from the mechanical
 * {@link ModuleForm} plus the cross-cutting markers:
 *
 * <ul>
 *   <li>bundled or CDN-proxy external → {@code BUNDLED_EXTERNAL} (exempt third-party);</li>
 *   <li>{@link ManagerInjector} → {@code MANAGER_INJECTOR};</li>
 *   <li>a module that exports a widget ({@link UiComponent.Shape#WIDGET}) → {@code WIDGET};</li>
 *   <li>CssGroup → {@code GENERATED_CSS};</li>
 *   <li>everything else (SvgGroup, SelfContent, resource-backed) → {@code CONSUMER},
 *       the full-discipline baseline.</li>
 * </ul>
 */
public record ModuleClassifier() implements StatelessFunctionalObject {

    public static final ModuleClassifier INSTANCE = new ModuleClassifier();

    /** The entry's declared domain type if any, else the structural classification of its module. */
    public JsModuleType classify(CrateEntry entry) {
        JsModuleType declared = entry.declaredType();
        return declared != null ? declared : classify(entry.module());
    }

    /**
     * The type of a module with none declared, derived from what it is: an external module, a manager injector or a
     * widget first, then by its {@link ModuleForm}.
     */
    public JsModuleType classify(EsModule<?> module) {
        if (module instanceof BundledExternalModule<?> || module instanceof ExternalModule<?>) {
            return StandardJsModuleType.BUNDLED_EXTERNAL;
        }
        if (module instanceof ManagerInjector) {
            return StandardJsModuleType.MANAGER_INJECTOR;
        }
        if (exportsAWidget(module)) {
            return StandardJsModuleType.WIDGET;
        }
        return switch (ModuleForms.INSTANCE.of(module)) {
            case CSS_GROUP, DERIVED_CSS -> StandardJsModuleType.GENERATED_CSS;
            case SVG_GROUP, SELF_CONTENT, RESOURCE_BACKED -> StandardJsModuleType.CONSUMER;
            case BUNDLED_EXTERNAL -> StandardJsModuleType.BUNDLED_EXTERNAL; // (already handled above)
        };
    }

    /** Whether a module exports a widget: a component whose shape is a widget's. */
    boolean exportsAWidget(EsModule<?> module) {
        return module.exports().exports().stream()
                .anyMatch(x -> x instanceof UiComponent<?> c && c.shape() == UiComponent.Shape.WIDGET);
    }
}
