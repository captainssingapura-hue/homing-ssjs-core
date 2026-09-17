package hue.captains.singapura.js.homing.core;

import java.util.Set;

/**
 * RFC 0066 — a <b>provided</b> class: a node in the CSS dependency graph whose
 * body the theme supplies. Rendered at {@code :root}, unlayered, as the
 * custom-property declarations the theme binds for every token this class
 * {@link #declares()}.
 *
 * <p>A palette is a class so that it is a node like any other: a class that
 * reads its tokens can name it in {@link CssClass#dependsOn()}, the group that
 * holds it derives its edges the same way, and the client loads it by the
 * same plan. A group that holds a palette is usually a {@link CssGroup#prior()
 * prior} — the global palette is reachable from every class by definition
 * rather than by 179 identical declarations.</p>
 *
 * <p>{@link #body()} is {@code null}: after RFC 0002-ext1 moved every drawn
 * class to an inline body, a null body has exactly one meaning left, and this
 * is it. The theme's body arrives as a {@link PaletteProvision} registered for
 * {@code (this class's group, theme)}. Completeness — every theme provides every
 * palette the deployment reaches — and correctness — every token a body reads
 * is declared by a palette that body can reach — are laws over the graph,
 * checked at build; {@link #declares()} is the set the second law reads.</p>
 *
 * @param <G> the group this palette belongs to
 */
public interface PaletteClass<G extends CssGroup<G>> extends CssClass<G> {

    /** The tokens every provision of this palette must bind. */
    Set<CssVar> declares();

    /** Provided, never declared inline. */
    @Override default String body() { return null; }
}
