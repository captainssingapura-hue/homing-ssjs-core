package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;

import java.util.List;

/**
 * A module's looks in one style (RFC 0067, D50): a bundle for each top-level component it styles. The house's styles
 * each come in one module - every look the style overrides of the base - and an extension brings its components with
 * their looks in the base ({@link BaseModule}) and in any style it serves.
 *
 * @param <Sc> the style's scheme
 * @param <S>  the style
 */
public interface StyleModule<Sc extends ColorScheme, S extends DesignStyle<Sc>> {

    /** The style its looks are in. */
    S style();

    /** A bundle for each top-level component it styles. */
    List<StyleBundle<Sc, S>> bundles();
}
