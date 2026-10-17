package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;

import java.util.List;

/**
 * A component module with its looks in the base layer (RFC 0067, D50): every component needs a look in the base, so the
 * two come together - the module's components ({@link ComponentModule}, their definition), and a bundle in the base for
 * each of them, its whole design. The base modules registered are the instance's components and its base layer at once.
 * A module whose components include a local-style branch registers that branch's local styles with them (appendix Local
 * Styles): each is drawn in every style the instance serves, a sheet of its own beside the style's.
 *
 * @param <Sc> the base's scheme
 * @param <B>  the base layer
 */
public interface BaseModule<Sc extends ColorScheme, B extends DesignStyle<Sc>> extends StyleModule<Sc, B> {

    /** The components it brings: their definition. */
    ComponentModule components();

    /** The local styles it registers, each of a local-style branch its components bring; none, unless it says. */
    default List<? extends LocalStyle<?, ?>> localStyles() { return List.of(); }
}
