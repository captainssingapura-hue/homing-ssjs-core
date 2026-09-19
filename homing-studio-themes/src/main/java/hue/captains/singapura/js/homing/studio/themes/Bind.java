package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Extent;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.State;

import java.util.Map;

/**
 * The short way to say a design's bindings. Every method returns one entry —
 * a design class and its impl; a design is a map of them, and its function is
 * the lookup.
 * Single-property targets bind by value; the multi-property ones (surface,
 * edge, rule, scale, treatment, decoration) have their own words here so a
 * design reads as what it means rather than as CSS property names.
 */
final class Bind {

    private Bind() {}

    /** A single-property target: ink, fill, corner, shadow, face, weight, ease, transform, cursor, opacity … */
    static Map.Entry<DesignClass<?>, Impl> one(DesignClass<?> cls, String value) {
        return Map.entry(cls, Impl.Bindings.of(value));
    }

    /** A single-property target with a dark-mode value. */
    static Map.Entry<DesignClass<?>, Impl> one(DesignClass<?> cls, String light, String dark) {
        return Map.entry(cls, Impl.Bindings.of(light).in(Mode.DARK, State.REST, dark));
    }

    /** A single-property target with states: rest, hover; optionally active and focus. */
    static Map.Entry<DesignClass<?>, Impl> states(DesignClass<?> cls, String rest, String hover) {
        return Map.entry(cls, Impl.Bindings.of(rest).at(State.HOVER, hover));
    }
    static Map.Entry<DesignClass<?>, Impl> states(DesignClass<?> cls, String rest, String hover, String active) {
        return Map.entry(cls, Impl.Bindings.of(rest).at(State.HOVER, hover).at(State.ACTIVE, active));
    }

    /** A Color.Surface: its colour, light and dark. */
    static Map.Entry<DesignClass<?>, Impl> surface(DesignClass<?> cls, String light, String dark) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "background-color", light)
                .in(Mode.DARK, State.REST, "background-color", dark));
    }
    static Map.Entry<DesignClass<?>, Impl> surface(DesignClass<?> cls, String light) {
        return Map.entry(cls, Impl.Bindings.none().at(State.REST, "background-color", light));
    }
    /** A Color.Surface painted with an image (a gradient, a hatch) over its colour. */
    static Map.Entry<DesignClass<?>, Impl> surfaceImage(DesignClass<?> cls, String color, String image) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "background-color", color)
                .at(State.REST, "background-image", image));
    }
    /** A Color.Surface painted with a pattern: an image tiled at a size — a dot screen, a grid, a texture — over its colour. */
    static Map.Entry<DesignClass<?>, Impl> surfacePattern(DesignClass<?> cls, String color, String image, String size) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "background-color", color)
                .at(State.REST, "background-image", image)
                .at(State.REST, "background-size", size)
                .at(State.REST, "background-repeat", "repeat"));
    }
    static Map.Entry<DesignClass<?>, Impl> surfacePattern(DesignClass<?> cls, String color, String image, String size, String darkColor, String darkImage) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "background-color", color)
                .at(State.REST, "background-image", image)
                .at(State.REST, "background-size", size)
                .at(State.REST, "background-repeat", "repeat")
                .in(Mode.DARK, State.REST, "background-color", darkColor)
                .in(Mode.DARK, State.REST, "background-image", darkImage));
    }

    /** A Color.Edge: its border colour (one value or four), light and dark, with an optional hover. */
    static Map.Entry<DesignClass<?>, Impl> edge(DesignClass<?> cls, String light, String dark) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "border-color", light)
                .in(Mode.DARK, State.REST, "border-color", dark));
    }
    static Map.Entry<DesignClass<?>, Impl> edge(DesignClass<?> cls, String light) {
        return Map.entry(cls, Impl.Bindings.none().at(State.REST, "border-color", light));
    }
    static Map.Entry<DesignClass<?>, Impl> edgeHover(DesignClass<?> cls, String rest, String hover) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "border-color", rest)
                .at(State.HOVER, "border-color", hover));
    }
    static Map.Entry<DesignClass<?>, Impl> edgeFocus(DesignClass<?> cls, String rest, String focus) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "border-color", rest)
                .at(State.FOCUS, "border-color", focus));
    }

    /** A Shape.Rule: border width and style (one value or four). */
    static Map.Entry<DesignClass<?>, Impl> rule(DesignClass<?> cls, String width, String style) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "border-width", width)
                .at(State.REST, "border-style", style));
    }
    /** A Shape.Rule that is also the focus ring: outline width, style and offset on focus. */
    static Map.Entry<DesignClass<?>, Impl> ruleWithFocusRing(DesignClass<?> cls, String width, String style, String ringWidth, String ringOffset) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "border-width", width)
                .at(State.REST, "border-style", style)
                .at(State.FOCUS, "outline-width", ringWidth)
                .at(State.FOCUS, "outline-style", "solid")
                .at(State.FOCUS, "outline-offset", ringOffset));
    }

    /** An edge that is also a ring: one colour on the border and the outline, so a focus ring reads the same drawn either way. */
    static Map.Entry<DesignClass<?>, Impl> ring(DesignClass<?> cls, String light, String dark) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "border-color", light).at(State.REST, "outline-color", light)
                .in(Mode.DARK, State.REST, "border-color", dark).in(Mode.DARK, State.REST, "outline-color", dark));
    }
    static Map.Entry<DesignClass<?>, Impl> ring(DesignClass<?> cls, String light) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "border-color", light).at(State.REST, "outline-color", light));
    }
    /** A Shape.Rule that is a ring at rest: an outline's width, style and offset — the mark on the thing that has the focus, wherever it is drawn. */
    static Map.Entry<DesignClass<?>, Impl> outline(DesignClass<?> cls, String width, String style, String offset) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "outline-width", width)
                .at(State.REST, "outline-style", style)
                .at(State.REST, "outline-offset", offset));
    }

    /**
     * A single-property colour word at every extent — the meaning turned the
     * other way, neutral, and the word itself — light and dark: the three
     * anchors a design owes a word some component scales. Order: −1, 0, 1.
     */
    static Map.Entry<DesignClass<?>, Impl> scaled(DesignClass<?> cls, String negLight, String negDark, String zeroLight, String zeroDark, String light, String dark) {
        return Map.entry(cls, Impl.Bindings.of(light).in(Mode.DARK, State.REST, dark)
                .at(Extent.ZERO, State.REST, zeroLight).in(Mode.DARK, Extent.ZERO, State.REST, zeroDark)
                .at(Extent.NEG, State.REST, negLight).in(Mode.DARK, Extent.NEG, State.REST, negDark));
    }
    static Map.Entry<DesignClass<?>, Impl> scaled(DesignClass<?> cls, String neg, String zero, String full) {
        return Map.entry(cls, Impl.Bindings.of(full).at(Extent.ZERO, State.REST, zero).at(Extent.NEG, State.REST, neg));
    }

    /**
     * A word, anchored at the other two extents for one of its properties — the
     * meaning turned the other way at −1, neutral at 0 — light and dark. The
     * word itself is the anchor at 1, untouched.
     */
    static Map.Entry<DesignClass<?>, Impl> anchored(Map.Entry<DesignClass<?>, Impl> word, String property, String negLight, String negDark, String zeroLight, String zeroDark) {
        var b = (Impl.Bindings) word.getValue();
        return Map.entry(word.getKey(), b
                .at(Extent.ZERO, State.REST, property, zeroLight).in(Mode.DARK, Extent.ZERO, State.REST, property, zeroDark)
                .at(Extent.NEG, State.REST, property, negLight).in(Mode.DARK, Extent.NEG, State.REST, property, negDark));
    }
    static Map.Entry<DesignClass<?>, Impl> anchored(Map.Entry<DesignClass<?>, Impl> word, String property, String neg, String zero) {
        var b = (Impl.Bindings) word.getValue();
        return Map.entry(word.getKey(), b.at(Extent.ZERO, State.REST, property, zero).at(Extent.NEG, State.REST, property, neg));
    }

    /** A Type.Scale: size and line height. */
    static Map.Entry<DesignClass<?>, Impl> scale(DesignClass<?> cls, String size, String lineHeight) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "font-size", size)
                .at(State.REST, "line-height", lineHeight));
    }

    /** A Type.Treatment: any of tracking, case, style. Null skips. */
    static Map.Entry<DesignClass<?>, Impl> treatment(DesignClass<?> cls, String letterSpacing, String textTransform, String fontStyle) {
        var b = Impl.Bindings.none();
        if (letterSpacing != null) b = b.at(State.REST, "letter-spacing", letterSpacing);
        if (textTransform != null) b = b.at(State.REST, "text-transform", textTransform);
        if (fontStyle != null) b = b.at(State.REST, "font-style", fontStyle);
        return Map.entry(cls, b);
    }

    /** A Type.Decoration: the line, with an optional offset and thickness. */
    static Map.Entry<DesignClass<?>, Impl> decoration(DesignClass<?> cls, String line) {
        return Map.entry(cls, Impl.Bindings.none().at(State.REST, "text-decoration-line", line));
    }
    static Map.Entry<DesignClass<?>, Impl> decoration(DesignClass<?> cls, String line, String offset, String thickness) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "text-decoration-line", line)
                .at(State.REST, "text-underline-offset", offset)
                .at(State.REST, "text-decoration-thickness", thickness));
    }

    /** A whole body for a class — the edge case, confined to the target's properties. */
    static Map.Entry<DesignClass<?>, Impl> body(DesignClass<?> cls, String css) {
        return Map.entry(cls, new Impl.Body(css));
    }

    /** An explicit nothing. */
    static Map.Entry<DesignClass<?>, Impl> silence(DesignClass<?> cls) {
        return Map.entry(cls, Impl.Silence.css());
    }
}
