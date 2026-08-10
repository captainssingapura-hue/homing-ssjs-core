package hue.captains.singapura.js.homing.conformance.engine;

import hue.captains.singapura.js.homing.conformance.rules.ServedModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.server.EsModuleGetAction;
import hue.captains.singapura.js.homing.server.QueryParamResolver;

import java.util.Objects;

/**
 * RFC 0044 Phase 6 — turns an {@link EsModule} into the {@link ServedModule}
 * conformance validates, by rendering it through the <b>same code path the
 * server serves from</b> ({@link EsModuleGetAction#render(EsModule)}). So the
 * validated bytes equal the served bytes — conformance can never disagree with
 * what a browser actually receives.
 *
 * <p>Uses the server's own {@link QueryParamResolver} and default (null)
 * theme/locale — i.e. exactly a plain {@code /module?class=} fetch.</p>
 */
public final class ServedModuleRenderer {

    private final EsModuleGetAction serving;

    public ServedModuleRenderer() {
        this(new EsModuleGetAction(new QueryParamResolver()));
    }

    public ServedModuleRenderer(EsModuleGetAction serving) {
        this.serving = Objects.requireNonNull(serving, "serving");
    }

    /** Render + structurally classify — the standalone path (no crate context). */
    public ServedModule render(EsModule<?> module) {
        return render(module, ModuleClassifier.classify(module));
    }

    /** Render with an already-decided type — the crate path, where the entry's declared role wins. */
    public ServedModule render(EsModule<?> module, JsModuleType type) {
        String served = serving.render(module);
        return ServedModule.of(module.getClass().getName(), type, served);
    }
}
