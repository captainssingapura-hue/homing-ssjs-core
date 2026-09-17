package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.ImplProvider;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.State;

/**
 * The short way to say a design's bindings. Every method returns an
 * {@link ImplProvider} for one design class; the design is the list.
 * Single-property targets bind by value; the multi-property ones (surface,
 * edge, rule, scale, treatment, decoration) have their own words here so a
 * design reads as what it means rather than as CSS property names.
 */
final class Bind {

    private Bind() {}

    /** A single-property target: ink, fill, corner, shadow, face, weight, ease, transform, cursor, opacity … */
    static <D extends DesignClass<?, ?>> ImplProvider<D> one(Class<D> cls, String value) {
        return ImplProvider.of(cls, Impl.Bindings.of(value));
    }

    /** A single-property target with a dark-mode value. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> one(Class<D> cls, String light, String dark) {
        return ImplProvider.of(cls, Impl.Bindings.of(light).in(Mode.DARK, State.REST, dark));
    }

    /** A single-property target with states: rest, hover; optionally active and focus. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> states(Class<D> cls, String rest, String hover) {
        return ImplProvider.of(cls, Impl.Bindings.of(rest).at(State.HOVER, hover));
    }
    static <D extends DesignClass<?, ?>> ImplProvider<D> states(Class<D> cls, String rest, String hover, String active) {
        return ImplProvider.of(cls, Impl.Bindings.of(rest).at(State.HOVER, hover).at(State.ACTIVE, active));
    }

    /** A Color.Surface: its colour, light and dark. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> surface(Class<D> cls, String light, String dark) {
        return ImplProvider.of(cls, Impl.Bindings.none()
                .at(State.REST, "background-color", light)
                .in(Mode.DARK, State.REST, "background-color", dark));
    }
    static <D extends DesignClass<?, ?>> ImplProvider<D> surface(Class<D> cls, String light) {
        return ImplProvider.of(cls, Impl.Bindings.none().at(State.REST, "background-color", light));
    }
    /** A Color.Surface painted with an image (a gradient, a hatch) over its colour. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> surfaceImage(Class<D> cls, String color, String image) {
        return ImplProvider.of(cls, Impl.Bindings.none()
                .at(State.REST, "background-color", color)
                .at(State.REST, "background-image", image));
    }

    /** A Color.Edge: its border colour (one value or four), light and dark, with an optional hover. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> edge(Class<D> cls, String light, String dark) {
        return ImplProvider.of(cls, Impl.Bindings.none()
                .at(State.REST, "border-color", light)
                .in(Mode.DARK, State.REST, "border-color", dark));
    }
    static <D extends DesignClass<?, ?>> ImplProvider<D> edge(Class<D> cls, String light) {
        return ImplProvider.of(cls, Impl.Bindings.none().at(State.REST, "border-color", light));
    }
    static <D extends DesignClass<?, ?>> ImplProvider<D> edgeHover(Class<D> cls, String rest, String hover) {
        return ImplProvider.of(cls, Impl.Bindings.none()
                .at(State.REST, "border-color", rest)
                .at(State.HOVER, "border-color", hover));
    }
    static <D extends DesignClass<?, ?>> ImplProvider<D> edgeFocus(Class<D> cls, String rest, String focus) {
        return ImplProvider.of(cls, Impl.Bindings.none()
                .at(State.REST, "border-color", rest)
                .at(State.FOCUS, "border-color", focus));
    }

    /** A Shape.Rule: border width and style (one value or four). */
    static <D extends DesignClass<?, ?>> ImplProvider<D> rule(Class<D> cls, String width, String style) {
        return ImplProvider.of(cls, Impl.Bindings.none()
                .at(State.REST, "border-width", width)
                .at(State.REST, "border-style", style));
    }
    /** A Shape.Rule that is also the focus ring: outline width, style and offset on focus. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> ruleWithFocusRing(Class<D> cls, String width, String style, String ringWidth, String ringOffset) {
        return ImplProvider.of(cls, Impl.Bindings.none()
                .at(State.REST, "border-width", width)
                .at(State.REST, "border-style", style)
                .at(State.FOCUS, "outline-width", ringWidth)
                .at(State.FOCUS, "outline-style", "solid")
                .at(State.FOCUS, "outline-offset", ringOffset));
    }

    /** A Type.Scale: size and line height. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> scale(Class<D> cls, String size, String lineHeight) {
        return ImplProvider.of(cls, Impl.Bindings.none()
                .at(State.REST, "font-size", size)
                .at(State.REST, "line-height", lineHeight));
    }

    /** A Type.Treatment: any of tracking, case, style. Null skips. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> treatment(Class<D> cls, String letterSpacing, String textTransform, String fontStyle) {
        var b = Impl.Bindings.none();
        if (letterSpacing != null) b = b.at(State.REST, "letter-spacing", letterSpacing);
        if (textTransform != null) b = b.at(State.REST, "text-transform", textTransform);
        if (fontStyle != null) b = b.at(State.REST, "font-style", fontStyle);
        return ImplProvider.of(cls, b);
    }

    /** A Type.Decoration: the line, with an optional offset and thickness. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> decoration(Class<D> cls, String line) {
        return ImplProvider.of(cls, Impl.Bindings.none().at(State.REST, "text-decoration-line", line));
    }
    static <D extends DesignClass<?, ?>> ImplProvider<D> decoration(Class<D> cls, String line, String offset, String thickness) {
        return ImplProvider.of(cls, Impl.Bindings.none()
                .at(State.REST, "text-decoration-line", line)
                .at(State.REST, "text-underline-offset", offset)
                .at(State.REST, "text-decoration-thickness", thickness));
    }

    /** A whole body for a class — the edge case, confined to the target's properties. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> body(Class<D> cls, String css) {
        return ImplProvider.of(cls, new Impl.Body(css));
    }

    /** An explicit nothing. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> silence(Class<D> cls) {
        return ImplProvider.of(cls, Impl.Silence.css());
    }
}
