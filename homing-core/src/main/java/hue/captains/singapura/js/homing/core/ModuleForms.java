package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.js.homing.base.lang.StatelessSingleton;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/** Works out a module's {@link ModuleForm} from its type: a form is inferred, never declared. */
public record ModuleForms() implements StatelessFunctionalObject, StatelessSingleton {

    public static final ModuleForms INSTANCE = new ModuleForms();

    /**
     * Infer the form from the module's type, in the same priority order the
     * serving path resolves its content provider.
     */
    public ModuleForm of(EsModule<?> module) {
        if (module instanceof BundledExternalModule<?>) return ModuleForm.BUNDLED_EXTERNAL;
        if (module instanceof SvgGroup<?>)              return ModuleForm.SVG_GROUP;
        if (module instanceof CssGroup<?>)              return ModuleForm.CSS_GROUP;
        if (module instanceof DerivedCss<?>)            return ModuleForm.DERIVED_CSS;
        if (module instanceof SelfContent)              return ModuleForm.SELF_CONTENT;
        return ModuleForm.RESOURCE_BACKED;
    }
}
