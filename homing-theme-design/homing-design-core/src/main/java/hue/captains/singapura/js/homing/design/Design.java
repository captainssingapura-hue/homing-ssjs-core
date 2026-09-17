package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

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
 */
public interface Design extends StatelessFunctionalObject {

    String slug();

    default String label() { return slug(); }

    List<ImplProvider<?>> providers();

    /** The design this one delegates unfulfilled classes to; empty for a root design. */
    default Optional<Design> base() { return Optional.empty(); }
}
