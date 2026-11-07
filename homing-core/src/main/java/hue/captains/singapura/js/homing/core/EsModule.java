package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A "Declaration" only of an ES/JS module.
 *
 * <p>Implements {@link Importable} as of RFC 0001 Step 04: this lets
 * {@link ModuleImports} key on the {@link Importable} supertype, sharing
 * the same import-graph machinery between EsModule sources and
 * {@link Linkable} sources (specifically {@link ProxyApp}).</p>
 */
public non-sealed interface EsModule<M extends EsModule<M>> extends Importable, StatelessFunctionalObject {
    /** What the module imports, and from where. */
    ImportsFor<M> imports();
    /** What the module exports. */
    ExportsOf<M> exports();
}
