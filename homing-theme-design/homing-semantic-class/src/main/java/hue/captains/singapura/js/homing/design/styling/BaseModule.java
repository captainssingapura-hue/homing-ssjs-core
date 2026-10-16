package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;

/**
 * A component module with its looks in the base layer (RFC 0067, D50): every component needs a look in the base, so the
 * two come together - the module's components ({@link ComponentModule}, their definition), and a bundle in the base for
 * each of them, its whole design. The base modules registered are the instance's components and its base layer at once.
 *
 * @param <Sc> the base's scheme
 * @param <B>  the base layer
 */
public interface BaseModule<Sc extends ColorScheme, B extends DesignStyle<Sc>> extends StyleModule<Sc, B> {

    /** The components it brings: their definition. */
    ComponentModule components();
}
