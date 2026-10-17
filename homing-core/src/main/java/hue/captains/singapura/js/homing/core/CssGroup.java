package hue.captains.singapura.js.homing.core;

import java.util.List;

/**
 * A CSS resource declaration that also acts as an {@link EsModule}.
 * <p>The generated JS module exports frozen {@link CssClass} objects.
 * Mirrors the {@link SvgGroup} pattern: each CssGroup is a module
 * whose exports are its declared CSS classes.</p>
 *
 * <p>A group is PROVIDED - declared by hand, each class with its body - and so
 * the same in every theme: its sheet is fetched once, with no theme, and a
 * theme switch leaves it alone (RFC 0064, D25). What varies with the theme is
 * DERIVED, and is a {@link DerivedCss}, never a group: the CSS manager loads
 * groups and nothing else.</p>
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
