package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A style as a local style is drawn in it (RFC 0067, appendix Local Styles): the style, which says the shapes, and its
 * scheme's colour tree, which holds every colour a drawing may name. No palette: a local style's sheet is the same in
 * every palette, each palettable colour written as the palette's variable, which the palette's own sheet defines - so a
 * switch of palette repaints it, and only a switch of style fetches it again (appendix Two Classes per Leaf).
 *
 * @param style   the style worn
 * @param colours its scheme's colour tree
 */
public record InStyle(DesignStyle<?> style, ColorTree colours) implements ValueObject {

    public InStyle {
        Objects.requireNonNull(style, "InStyle.style");
        Objects.requireNonNull(colours, "InStyle.colours");
    }

    /** {@code sketch}. */
    @Override public String toString() { return style.token(); }
}
