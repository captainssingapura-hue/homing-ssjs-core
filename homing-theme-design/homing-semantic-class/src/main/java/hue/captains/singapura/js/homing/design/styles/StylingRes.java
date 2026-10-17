package hue.captains.singapura.js.homing.design.styles;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.design.color.ColorLeaf;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * What a style does with a casting it implements (RFC 0067, appendix The Style Jars; D25's name kept): an ADT of two
 * values. Absence is no value of it: a casting with no implementation in a style is the style's base layer's, which only
 * the central manager's lookup answers.
 *
 * <ul>
 *   <li>{@link CssContent} - its own CSS, at its class's canonical name, and the colours that CSS uses;</li>
 *   <li>{@link Delegation} - another implementation's look, of the same component: the type of its target says so.</li>
 * </ul>
 *
 * @param <Sc> the scheme of the style answering
 * @param <C>  the component cast
 */
public sealed interface StylingRes<Sc extends ColorScheme, C extends Component<?>> extends ValueObject
        permits StylingRes.CssContent, StylingRes.Delegation {

    /**
     * Its own CSS - the resource at its class's canonical name - and every colour of the tree that CSS uses, each one the
     * style's scheme sees, by the compiler; the check holds the CSS to them both ways (D32).
     *
     * @param colours each once, in the order it lists them; none for a look that uses no colour of the tree
     */
    record CssContent<Sc extends ColorScheme, C extends Component<?>>(List<ColorLeaf<? super Sc, ?, ?>> colours)
            implements StylingRes<Sc, C> {
        public CssContent {
            colours = List.copyOf(Objects.requireNonNull(colours, "CssContent.colours"));
            var seen = new HashSet<Class<?>>();
            for (ColorLeaf<? super Sc, ?, ?> c : colours)
                if (!seen.add(c.getClass())) throw new IllegalArgumentException("CssContent imports " + c.token() + " twice");
        }

        /** Its colours, listed. */
        @SafeVarargs
        public CssContent(ColorLeaf<? super Sc, ?, ?>... colours) { this(List.of(colours)); }
    }

    /**
     * Another implementation's look, of the same component - its type says so: an implementation of another component
     * does not fit. No CSS and no colours of its own: what it reaches is served under the delegating casting's class.
     *
     * @param to the implementation delegated to, in this style or another
     */
    record Delegation<Sc extends ColorScheme, C extends Component<?>>(CastingStyle<?, ?, C, ?> to) implements StylingRes<Sc, C> {
        public Delegation {
            Objects.requireNonNull(to, "Delegation.to");
        }
    }
}
