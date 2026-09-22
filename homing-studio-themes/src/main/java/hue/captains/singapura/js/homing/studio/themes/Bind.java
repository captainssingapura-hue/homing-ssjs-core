package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Extent;
import hue.captains.singapura.js.homing.design.Growth;
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

    /** A Shape.Rule that is also the focus ring, in the ring's own style: for Sketchy's dashed one. */
    static Map.Entry<DesignClass<?>, Impl> ruleWithFocusRing(DesignClass<?> cls, String width, String style, String ringWidth, String ringStyle, String ringOffset) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "border-width", width)
                .at(State.REST, "border-style", style)
                .at(State.FOCUS, "outline-width", ringWidth)
                .at(State.FOCUS, "outline-style", ringStyle)
                .at(State.FOCUS, "outline-offset", ringOffset));
    }

    /**
     * The ring alone, on focus: the outline colour and nothing at rest, so a
     * control's edge stays its word's and the ring appears only when the
     * control has the focus. What a focusable control wears; {@code ring} is
     * for the element that IS the focus and draws it now.
     */
    static Map.Entry<DesignClass<?>, Impl> focusRing(DesignClass<?> cls, String light, String dark) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.FOCUS, "outline-color", light)
                .in(Mode.DARK, State.FOCUS, "outline-color", dark));
    }
    static Map.Entry<DesignClass<?>, Impl> focusRing(DesignClass<?> cls, String light) {
        return Map.entry(cls, Impl.Bindings.none().at(State.FOCUS, "outline-color", light));
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
    /**
     * The keys' mark on a word's Shape.Rule: the outline a thing wears while
     * the keys are on it — in force (HELD), proposed (CANDIDATE) and lent to
     * something inside it (LENT) — added to whatever the pair already says.
     * The outline channel, so nothing moves and no border is fought over.
     */
    static Map.Entry<DesignClass<?>, Impl> withKeysMark(Map.Entry<DesignClass<?>, Impl> base,
                                                        String heldWidth, String heldStyle, String heldOffset,
                                                        String candidateWidth, String candidateStyle, String candidateOffset,
                                                        String lentWidth, String lentStyle, String lentOffset) {
        return Map.entry(base.getKey(), ((Impl.Bindings) base.getValue())
                .at(State.HELD, "outline-width", heldWidth).at(State.HELD, "outline-style", heldStyle).at(State.HELD, "outline-offset", heldOffset)
                .at(State.CANDIDATE, "outline-width", candidateWidth).at(State.CANDIDATE, "outline-style", candidateStyle).at(State.CANDIDATE, "outline-offset", candidateOffset)
                .at(State.LENT, "outline-width", lentWidth).at(State.LENT, "outline-style", lentStyle).at(State.LENT, "outline-offset", lentOffset));
    }

    /** The keys' mark colour on a word that says nothing else about its edge: only the outline, only while the keys are on it. */
    static Map.Entry<DesignClass<?>, Impl> keysInk(DesignClass<?> cls, String held, String candidate, String lent) {
        return withKeysInk(Map.entry(cls, Impl.Bindings.none()), held, candidate, lent);
    }
    /** The keys' mark colour, light and dark, on a word that says nothing else about its edge. */
    static Map.Entry<DesignClass<?>, Impl> keysInk(DesignClass<?> cls, String held, String candidate, String lent, String heldDark, String candidateDark, String lentDark) {
        return withKeysInk(Map.entry(cls, Impl.Bindings.none()), held, candidate, lent, heldDark, candidateDark, lentDark);
    }

    /** The keys' mark colour on a word's Color.Edge: the outline's colour in force, proposed and lent. */
    static Map.Entry<DesignClass<?>, Impl> withKeysInk(Map.Entry<DesignClass<?>, Impl> base, String held, String candidate, String lent) {
        return Map.entry(base.getKey(), ((Impl.Bindings) base.getValue())
                .at(State.HELD, "outline-color", held)
                .at(State.CANDIDATE, "outline-color", candidate)
                .at(State.LENT, "outline-color", lent));
    }
    /** The keys' mark colour, light and dark. */
    static Map.Entry<DesignClass<?>, Impl> withKeysInk(Map.Entry<DesignClass<?>, Impl> base, String held, String candidate, String lent, String heldDark, String candidateDark, String lentDark) {
        return Map.entry(base.getKey(), ((Impl.Bindings) withKeysInk(base, held, candidate, lent).getValue())
                .in(Mode.DARK, State.HELD, "outline-color", heldDark)
                .in(Mode.DARK, State.CANDIDATE, "outline-color", candidateDark)
                .in(Mode.DARK, State.LENT, "outline-color", lentDark));
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

    /**
     * A word seen at rest as it is when hovered: REST takes HOVER's value for
     * {@code property} in every mode that has one, washed to {@code pct}
     * percent over transparent — 100 takes it whole. A tab on its strip, one
     * of many but each a thing before it is touched.
     */
    static Impl restingAsHovered(Impl impl, String property, int pct) {
        var b = (Impl.Bindings) impl;
        for (Mode mode : Mode.values()) {
            String v = b.values().getOrDefault(mode, Map.of()).getOrDefault(State.HOVER, Map.of()).get(property);
            if (v == null) continue;
            String rest = pct >= 100 ? v : "color-mix(in srgb, " + v + " " + pct + "%, transparent)";
            b = b.in(mode, State.REST, property, rest);
        }
        return b;
    }

    /** A Type.Scale: size and line height. */
    static Map.Entry<DesignClass<?>, Impl> scale(DesignClass<?> cls, String size, String lineHeight) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "font-size", size)
                .at(State.REST, "line-height", lineHeight));
    }
    /** A Type.Scale that grows with the element's size: the font by {@code ratio} per unit, the line height holding its proportion. */
    static Map.Entry<DesignClass<?>, Impl> scale(DesignClass<?> cls, String size, String lineHeight, double ratio) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "font-size", size)
                .at(State.REST, "line-height", lineHeight)
                .grows("font-size", ratio).grows("line-height", 1));
    }

    // ── density: the space a design gives a box, each length growing with the element's size ──

    /** A Size.Inset: the air inside, block and inline, both growing by {@code ratio}. */
    static Map.Entry<DesignClass<?>, Impl> inset(DesignClass<?> cls, String block, String inline, double ratio) {
        return Map.entry(cls, Impl.Bindings.none()
                .at(State.REST, "padding-block", block)
                .at(State.REST, "padding-inline", inline)
                .grows("padding-block", ratio).grows("padding-inline", ratio));
    }
    /** A Size.Gap, growing by {@code ratio}. */
    static Map.Entry<DesignClass<?>, Impl> gap(DesignClass<?> cls, String gap, double ratio) {
        return Map.entry(cls, Impl.Bindings.of(gap).grows(ratio));
    }
    /** A Size.Proportion: square at aspect 0, {@code widest} to one at +1, one to {@code widest} at −1 — the box's aspect is the element's number. */
    static Map.Entry<DesignClass<?>, Impl> proportion(DesignClass<?> cls, double widest) {
        return proportion(cls, "1", widest);
    }
    /** A Size.Proportion whose rest is the design's own — a tab's, wide — {@code room} times wider at aspect +1, {@code room} times narrower at −1. */
    static Map.Entry<DesignClass<?>, Impl> proportion(DesignClass<?> cls, String rest, double room) {
        return Map.entry(cls, Impl.Bindings.of(rest).grows(room, Growth.ASPECT));
    }
    /** A Size.Extent that is the box's own measure inline, growing by {@code ratio}; the block follows its proportion. */
    static Map.Entry<DesignClass<?>, Impl> measure(DesignClass<?> cls, String inlineSize, double ratio) {
        return Map.entry(cls, Impl.Bindings.none().at(State.REST, "inline-size", inlineSize).grows("inline-size", ratio));
    }
    /** A Size.Extent in both directions: a box's inline size and its block size, growing by {@code ratio} — a slider's length and the room across it, a cap's along and across; logical, so a rail stood up turns them with it. */
    static Map.Entry<DesignClass<?>, Impl> extent(DesignClass<?> cls, String inlineSize, String blockSize, double ratio) {
        return Map.entry(cls, Impl.Bindings.none().at(State.REST, "inline-size", inlineSize).at(State.REST, "block-size", blockSize).grows("inline-size", ratio).grows("block-size", ratio));
    }
    /** A Size.Extent that is a square: a knob, a mark. */
    static Map.Entry<DesignClass<?>, Impl> square(DesignClass<?> cls, String size, double ratio) {
        return extent(cls, size, size, ratio);
    }
    /** A Size.Extent that is a thickness only — a track's, a bar's — the length being the box's. */
    static Map.Entry<DesignClass<?>, Impl> thickness(DesignClass<?> cls, String blockSize, double ratio) {
        return Map.entry(cls, Impl.Bindings.none().at(State.REST, "block-size", blockSize).grows("block-size", ratio));
    }
    /** A Size.Extent: the least a box may be inline, growing by {@code ratio}. */
    static Map.Entry<DesignClass<?>, Impl> minWidth(DesignClass<?> cls, String minWidth, double ratio) {
        return Map.entry(cls, Impl.Bindings.none().at(State.REST, "min-width", minWidth).grows("min-width", ratio));
    }

    /** A Type.Glyph: the symbol drawn for an {@code Icon} word, as {@code content} — a character, an emoji; quoted here, so the design writes only the glyph. */
    static Map.Entry<DesignClass<?>, Impl> glyph(DesignClass<?> cls, String symbol) {
        return Map.entry(cls, Impl.Bindings.of("\"" + symbol + "\""));
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
