package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The CSS manager's affiliate: the class handles (RFC 0002-ext1). A handle is
 * identity, not behaviour — {@code st_btn} is the string {@code "st-btn"} and
 * the rules behind it are bound late by the cascade, which is what lets the
 * manager swap every sheet on the page while every holder keeps its handle
 * (RFC 0064). Split out of {@link CssClassManager} when the manager took on
 * the dependency graph and the theme switch.
 *
 * <p>The two exported names are the JS classes; they shadow the core
 * interfaces of the same names only inside this declaration.</p>
 */
public record CssHandles() implements EsModule<CssHandles> {

    public static final CssHandles INSTANCE = new CssHandles();

    /** The uniform handle type — every class handle in the system is one. */
    public record CssClass() implements Exportable._Constant<CssHandles> {}

    /** A base with precomputed variant handles as properties. */
    public record CssUtility() implements Exportable._Constant<CssHandles> {}

    @Override
    public ImportsFor<CssHandles> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<CssHandles> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new CssClass(), new CssUtility()));
    }
}
