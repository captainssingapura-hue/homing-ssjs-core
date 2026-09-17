package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.Theme;

import java.util.List;
import java.util.Optional;

/**
 * A design: an identity and a set of providers, one per design class it has
 * a word for, optionally over a base design it delegates the rest to. A
 * design never names a component and never learns one exists; its domain is
 * design classes, and every design class is a semantic on a target.
 *
 * <p>A theme is a design with a base. A design with no base is complete for
 * an application only if its providers cover the application's requirement
 * set; a design with a base is complete if the two together do. The check is
 * {@link Deployment}'s, over the set, never over the design's idea of itself.</p>
 *
 * <p>A design is a {@link Theme} — the framework's identity of "what a page
 * wears" — so a registry lists designs where it listed themes, the page's
 * {@code ?theme=} names one, and a picker shows its label. Nothing else of the
 * theme contract survives here: a design has no palette and no override; it
 * has providers.</p>
 */
public interface Design extends Theme {

    List<ImplProvider<?>> providers();

    /** The design this one delegates unfulfilled classes to; empty for a root design. */
    default Optional<Design> base() { return Optional.empty(); }
}
