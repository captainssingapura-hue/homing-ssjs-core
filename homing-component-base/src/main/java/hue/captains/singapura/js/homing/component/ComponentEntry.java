package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.DomModule;

import java.util.Objects;

/**
 * A leaf of a {@link ComponentCatalogue}, typed by its host so an entry can
 * only be listed by the catalogue it was made for. One variant so far — a
 * component; a portal to another vehicle's root is the variant a site adds
 * when it arranges rather than derives.
 *
 * @param <C> the host catalogue's type
 */
public sealed interface ComponentEntry<C extends ComponentCatalogue<C>> {

    /** A component declared by its module, listed here. */
    record OfComponent<C extends ComponentCatalogue<C>, M extends DomModule<M>>(UiComponent<M> component) implements ComponentEntry<C> {
        public OfComponent { Objects.requireNonNull(component, "ComponentEntry.OfComponent.component"); }
    }

    /** {@code host} is a type witness for inference, as the catalogue's {@code Entry.of} takes it; discarded. */
    static <C extends ComponentCatalogue<C>, M extends DomModule<M>> ComponentEntry<C> of(C host, UiComponent<M> component) {
        return new OfComponent<>(component);
    }
}
