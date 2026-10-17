package hue.captains.singapura.js.homing.core;

import java.util.List;

/**
 * An EsModule whose generated JavaScript interacts with the DOM.
 * <p>Only DomModules may declare CSS dependencies, since CSS
 * is only meaningful for modules that produce or manipulate
 * visible elements.</p>
 *
 * @param <M> self-type
 */
public interface DomModule<M extends DomModule<M>> extends EsModule<M> {

    /**
     * Derives CSS dependencies from this module's imports.
     * Any import source that is a {@link CssGroup} is treated as a CSS dependency.
     */
    @SuppressWarnings("unchecked")
    default List<CssGroup<?>> cssGroups() {
        return (List<CssGroup<?>>) (List<?>) imports().getAllImports().keySet().stream()
                .filter(m -> m instanceof CssGroup<?>)
                .toList();
    }

    /** The derived sheets it imports: a look's, which it wears by its own hand (RFC 0064, D25). */
    @SuppressWarnings("unchecked")
    default List<DerivedCss<?>> derivedSheets() {
        return (List<DerivedCss<?>>) (List<?>) imports().getAllImports().keySet().stream()
                .filter(m -> m instanceof DerivedCss<?>)
                .toList();
    }
}
