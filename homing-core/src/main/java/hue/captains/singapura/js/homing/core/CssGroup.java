package hue.captains.singapura.js.homing.core;

import java.util.List;

/**
 * A CSS resource declaration that also acts as an {@link EsModule}.
 * <p>The generated JS module exports frozen {@link CssClass} objects.
 * Mirrors the {@link SvgGroup} pattern: each CssGroup is a module
 * whose exports are its declared CSS classes.</p>
 *
 * @param <C> self-type
 */
public interface CssGroup<C extends CssGroup<C>> extends EsModule<C> {

    // RFC 0064 — a group's dependencies are not declared here. They are
    // derived from its classes' dependsOn() by CssImportsFor.of(group), and
    // there is deliberately no method to override: see CssImportsFor.

    List<CssClass<C>> cssClasses();

    @Override
    default ImportsFor<C> imports() {
        return ImportsFor.noImports();
    }

    @SuppressWarnings("unchecked")
    @Override
    default ExportsOf<C> exports() {
        return new ExportsOf<>((C) this, List.copyOf(cssClasses()));
    }
}
