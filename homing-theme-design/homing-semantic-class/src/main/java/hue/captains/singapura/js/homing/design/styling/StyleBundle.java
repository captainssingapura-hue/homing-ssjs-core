package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * A top-level component's looks in one style (RFC 0067, D50): its own, and those of its whole design - each part it casts
 * in a role, and each part no module lists, down to the next component a module lists. The castings are part of the
 * component's design, so they come with it, as one bundle. A look several components share stays with the shared
 * component and comes into each bundle by the same implementation, reached as a delegation would reach it - it is never
 * registered on its own.
 *
 * @param <Sc>      the style's scheme
 * @param <S>       the style
 * @param component the top-level component - the owner
 * @param looks     its looks in the style: the style's implementations of castings of its design
 */
public record StyleBundle<Sc extends ColorScheme, S extends DesignStyle<Sc>>(Component<?> component, List<? extends CastingStyle<Sc, S, ?, ?>> looks)
        implements ValueObject {

    public StyleBundle {
        Objects.requireNonNull(component, "StyleBundle.component");
        looks = List.copyOf(Objects.requireNonNull(looks, "StyleBundle.looks"));
    }
}
