package hue.captains.singapura.js.homing.design.styles;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * One jar's declaration of what it provides for styles (RFC 0067, appendix The Style Jars): explicitly, in Java, each
 * style it implements castings of and the full list of its implementations for each - one declaration per jar, and no
 * {@code META-INF/services}. A component's jar provides its components' Base; a style's jar, that style's refinements.
 * Registered at bootstrap with the central manager ({@code StyleManager}); held alone, per implementation, by the jar's
 * own check ({@code CheckDeclaration}).
 */
public interface StyleDeclaration extends StatelessFunctionalObject {

    /** Each style the jar provides implementations for, with every one of them. */
    List<Provided<?, ?>> provided();

    /**
     * A style's implementations in one jar, typed: a list for Editorial holds Editorial's alone.
     *
     * @param style     the style
     * @param castings  every implementation of it the jar holds
     */
    record Provided<Sc extends ColorScheme, S extends DesignStyle<Sc>>(S style, List<? extends CastingStyle<Sc, S, ?, ?>> castings)
            implements ValueObject {
        public Provided {
            Objects.requireNonNull(style, "Provided.style");
            castings = List.copyOf(Objects.requireNonNull(castings, "Provided.castings"));
        }
    }
}
