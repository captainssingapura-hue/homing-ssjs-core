package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * The schemes things are in, read off their declarations: a node's, a palette's, a style's, a design's, a tree
 * reader's. A scheme is an abstract class: one from scratch that implements {@link ColorScheme} alone - a base - or one
 * on top of another - never the marker itself, nor a variable, which a declaration could otherwise slip in.
 */
public record Schemes() implements StatelessFunctionalObject {

    public static final Schemes INSTANCE = new Schemes();

    /** The scheme a node is in. */
    public Optional<Class<?>> of(ColorNode<?, ?, ?> node) { return scheme(node.getClass(), ColorNode.class); }

    /** The scheme a palette implements. */
    public Optional<Class<?>> of(ColorPalette<?> palette) { return scheme(palette.getClass(), ColorPalette.class); }

    /** The scheme a style requires. */
    public Optional<Class<?>> of(DesignStyle<?> style) { return scheme(style.getClass(), DesignStyle.class); }

    /** The scheme a design is in (D33). */
    public Optional<Class<?>> of(Design<?, ?> design) { return scheme(design.getClass(), Design.class); }

    /** The scheme below one: the scheme it is on top of, or none for the base and a scheme from scratch. */
    public Optional<Class<?>> below(Class<?> scheme) {
        Class<?> up = scheme.getSuperclass();
        return up != null && ColorScheme.class.isAssignableFrom(up) ? Optional.of(up) : Optional.empty();
    }

    /** Whether a scheme sees what is in another: its own, and what every scheme below it has. */
    public boolean sees(Class<?> scheme, Class<?> of) { return of.isAssignableFrom(scheme); }

    /** The scheme a tree reader reads. */
    public Optional<Class<?>> of(SchemeTreeReader<?> reader) { return scheme(reader.getClass(), SchemeTreeReader.class); }

    /** The scheme's name, as messages say it. */
    public String name(Class<?> scheme) { return scheme.getSimpleName(); }

    private static Optional<Class<?>> scheme(Class<?> declared, Class<?> generic) {
        Class<?> s = TypeArguments.INSTANCE.at(declared, generic, 0);
        if (s == null || s.isInterface() || !ColorScheme.class.isAssignableFrom(s) || !Modifier.isAbstract(s.getModifiers())) return Optional.empty();
        return Optional.of(s);
    }
}
