package hue.captains.singapura.js.homing.design.semantic;

import java.util.List;

/**
 * How a concrete leaf is drawn (RFC 0067, appendix Virtual Leaves and Designs): in a theme, a
 * {@link Drawing} for every target its leaf owns, and for nothing else. The design's half of a concrete
 * leaf, on the same record as the taxonomy's half ({@code ConcreteComponent}):
 *
 * <pre>{@code
 * public record ClassicCursorBorder() implements CursorBorder.Concrete<MinesweeperClassic>, Drawn {
 *     @Override public List<Drawing<?>> drawings(Dressed theme) {
 *         return List.of(new Drawing.Own<>(Target.Shape.Outline.INSTANCE, "outline-width: 1px; outline-style: dotted;"),
 *                        new Drawing.Own<>(Target.Color.Outline.INSTANCE, "outline-color: #000;"));
 *     }
 * }
 * }</pre>
 *
 * <p>The theme is given whole, so a design answers shape once for every style or per style, and colour from
 * the palette or in values of its own.</p>
 */
public interface Drawn {

    /** Its drawings in the theme worn: one for every target its leaf owns. */
    List<Drawing<?>> drawings(Dressed theme);
}
