package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * Registration's first stage (RFC 0067, appendix The Style Jars): one style, and the jars that implement it, each by its
 * declaration - Base's from every component jar, a further style's from its own jar. An instance lists the registrations
 * it includes, the second stage ({@link StyleManager}).
 *
 * @param style the style
 * @param jars  the declarations of the jars implementing it
 */
public record StyleRegistration(DesignStyle<?> style, List<StyleDeclaration> jars) implements ValueObject {
    public StyleRegistration {
        Objects.requireNonNull(style, "StyleRegistration.style");
        jars = List.copyOf(Objects.requireNonNull(jars, "StyleRegistration.jars"));
    }

    /** A style and its jars, listed. */
    public static StyleRegistration of(DesignStyle<?> style, StyleDeclaration... jars) { return new StyleRegistration(style, List.of(jars)); }
}
