package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Optional;

/**
 * A style worn in a palette chosen at run time - from a picker, a listing, a command line - where the compiler cannot
 * see the two together: the theme when both are of one scheme, exactly, and none otherwise. A palette of a scheme above
 * the style's is refused as one below it is: the colours it adds are ones the style cannot see.
 */
public record Wear() implements StatelessFunctionalObject {

    public static final Wear INSTANCE = new Wear();

    /** The style worn in the palette, when the two are of one scheme. */
    public Optional<Dressed> of(ColorTree colors, DesignStyle<?> style, ColorPalette<?> palette) {
        Optional<ColorScheme> mine = Schemes.INSTANCE.of(style), its = Schemes.INSTANCE.of(palette);
        if (mine.isEmpty() || its.isEmpty() || !Schemes.INSTANCE.same(mine.get(), its.get())) return Optional.empty();
        return Optional.of(new Dressed(colors, style, palette, true));
    }

    /** Whether the style may be worn in the palette: both of one scheme. */
    public boolean fits(DesignStyle<?> style, ColorPalette<?> palette) {
        Optional<ColorScheme> mine = Schemes.INSTANCE.of(style), its = Schemes.INSTANCE.of(palette);
        return mine.isPresent() && its.isPresent() && Schemes.INSTANCE.same(mine.get(), its.get());
    }
}
