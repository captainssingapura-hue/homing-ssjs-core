package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.color.SemanticColor;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * The CSS variables hand-written CSS names its colours by (RFC 0067, appendix Two Classes per Leaf), each named by its
 * leaf of the colour tree, whose names are unique across the tree:
 *
 * <ul>
 *   <li>{@code --fp-<leaf>} - from palette - a palettable colour, defined by each palette's sheet;</li>
 *   <li>{@code --fixed-<leaf>} - a fixed colour, assigned in the leaf's own CSS;</li>
 *   <li>{@code --it-<leaf>} - an interpolated colour, assigned in the leaf's own CSS;</li>
 *   <li>{@code --along-<scale>} - a colour along a scale by the element's degree, assigned in the leaf's own CSS;</li>
 *   <li>{@code --current-<leaf>} - CSS's currentColor at a share, assigned in the leaf's own CSS (D31, D32).</li>
 * </ul>
 */
public record ColorVariables() implements StatelessFunctionalObject {

    public static final ColorVariables INSTANCE = new ColorVariables();

    /** {@code --fp-accent}. */
    public String of(Palettable<?, ?> color) { return "--fp-" + color.token(); }

    /** {@code --fixed-mine-red}. */
    public String of(Fixed<?, ?> color) { return "--fixed-" + color.token(); }

    /** {@code --it-warning}. */
    public String of(Interpolated<?, ?> color) { return "--it-" + color.token(); }

    /** {@code --current-rule}. */
    public String of(CurrentColor<?, ?> color) { return "--current-" + color.token(); }

    /** The variable of a colour of any kind. */
    public String of(SemanticColor<?> color) {
        return switch (color) {
            case Palettable<?, ?> p -> of(p);
            case Fixed<?, ?> f -> of(f);
            case Interpolated<?, ?> i -> of(i);
            case CurrentColor<?, ?> k -> of(k);
        };
    }

    /** {@code --along-risk}. */
    public String along(ColorScale<?, ?> scale) { return "--along-" + scale.token(); }
}
