package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.Optional;

/**
 * A colour palette (RFC 0067, appendix Semantic Colours and Flavours): a stateless functional object that, given a
 * palettable colour, gives a concrete colour or no answer - a curated set of concrete colours. How it arrives at them,
 * one colour serving several purposes or a colour derived from a few seeds, is its own business. It keeps its identity,
 * its name, what inspired it and its scheme, and nothing else: it knows no component and no style.
 *
 * <p>A palette implements one scheme, its type argument, and is worn only by a style of that scheme. It sits on a
 * palette of the scheme below, which answers what its own scheme does not add, and so down the schemes; a palette of
 * the base, or of a scheme from scratch, sits on nothing. Each palette is complete over the palettable colours its own
 * scheme adds, which the colour tree checks when it is read against the palette ({@link ColorTree#check(ColorPalette)}).</p>
 *
 * @param <S> the scheme it implements
 */
public interface ColorPalette<S extends ColorScheme> {

    /** The palette's token, from its type: {@code harbour}. */
    default String token() { return NodeName.ofType(getClass(), "").value(); }

    /** What a picker calls it: its token's words, capitalised - {@code Neo-Brutalism} - unless it says otherwise. */
    default String label() { return Labels.of(token()); }

    /** One line on what it is trying to be, shown beside its name in a picker; none unless it says. */
    default String inspiration() { return ""; }

    /** The concrete colour it gives a palettable colour; none for a colour it leaves to the palette below or to a sub-palette. */
    Optional<ConcreteColor> color(Palettable<?, ?> color);

    /** The palette it sits on, of the scheme below its own; none for a palette of the base, or of a scheme from scratch. */
    default Optional<ColorPalette<? super S>> below() { return Optional.empty(); }
}
