package hue.captains.singapura.js.homing.design.semantic;

import java.util.List;

/**
 * How a concrete leaf is drawn (RFC 0067, appendix Virtual Leaves and Designs, and Local Styles): in a style, a
 * {@link Drawing} for every target its leaf owns, and for nothing else. The drawing half of a concrete leaf, on the same
 * record as its leaf's half ({@code ConcreteComponent}):
 *
 * <pre>{@code
 * public record ClassicCursorBorder() implements CursorBorder.Concrete<MinesweeperClassic>, Drawn {
 *     @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
 *         return List.of(new Drawing.Own<>(Target.Shape.Outline.INSTANCE, "outline-width: 1px; outline-style: dotted;"),
 *                        new Drawing.Painted<>(Target.Color.Outline.INSTANCE, new Paint.With<>(new BaseColours.Copy())));
 *     }
 * }
 * }</pre>
 *
 * <p>The style is given, never the palette: a local style answers shape once for every style or per style, and colour
 * in semantic colours - a palettable one follows whatever palette is worn, a fixed one pins its value.</p>
 */
public interface Drawn {

    /** Its drawings in the style worn: one for every target its leaf owns. */
    List<Drawing<?>> drawings(DesignStyle<?> style);
}
