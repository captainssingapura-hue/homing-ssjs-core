package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * The schemes things are in, read off their declarations: a node's, a palette's, a style's. A scheme is an abstract
 * class: {@link ColorScheme.Base}, one on top of another, or one from scratch that implements {@link ColorScheme}
 * alone - never the marker itself, nor a variable, which a declaration could otherwise slip in.
 */
record Schemes() implements StatelessFunctionalObject {

    static final Schemes INSTANCE = new Schemes();

    /** The scheme a node is in. */
    Optional<Class<?>> of(ColorNode<?, ?, ?> node) { return scheme(node.getClass(), ColorNode.class); }

    /** The scheme a palette implements. */
    Optional<Class<?>> of(ColorPalette<?> palette) { return scheme(palette.getClass(), ColorPalette.class); }

    /** The scheme a style requires. */
    Optional<Class<?>> of(DesignStyle<?> style) { return scheme(style.getClass(), DesignStyle.class); }

    /** The scheme below one: the scheme it is on top of, or none for the base and a scheme from scratch. */
    Optional<Class<?>> below(Class<?> scheme) {
        Class<?> up = scheme.getSuperclass();
        return up != null && ColorScheme.class.isAssignableFrom(up) ? Optional.of(up) : Optional.empty();
    }

    /** Whether a scheme sees what is in another: its own, and what every scheme below it has. */
    boolean sees(Class<?> scheme, Class<?> of) { return of.isAssignableFrom(scheme); }

    /** {@code the base}, or the scheme's name, as messages say it. */
    String name(Class<?> scheme) { return scheme == ColorScheme.Base.class ? "the base" : scheme.getSimpleName(); }

    private static Optional<Class<?>> scheme(Class<?> declared, Class<?> generic) {
        Class<?> s = TypeArguments.INSTANCE.at(declared, generic, 0);
        if (s == null || s.isInterface() || !ColorScheme.class.isAssignableFrom(s) || !Modifier.isAbstract(s.getModifiers())) return Optional.empty();
        return Optional.of(s);
    }
}
