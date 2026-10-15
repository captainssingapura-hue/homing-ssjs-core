package hue.captains.singapura.js.homing.design.styles;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.RoleNode;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A style's implementation of one casting (RFC 0067, appendix The Style Jars): a stateless functional object, typed by
 * its style, its component and its role, answering what the style does with the casting - its own CSS, or another
 * implementation of the same component ({@link StylingRes}). Its type arguments are what it is an implementation of:
 * they are read, never its name, and two implementations of one (style, component, role) are refused.
 *
 * <pre>{@code
 * package hue.captains.singapura.js.homing.design.styles.editorial;
 *
 * public record PlainButton() implements CastingStyle<ColorScheme.Base, Editorial, HouseControls.PlainButton, RoleRoot> {
 *     @Override public StylingRes<ColorScheme.Base, HouseControls.PlainButton> style() {
 *         return new StylingRes.CssContent<>(new Raised(), new Text(), new Border(), new Ring());
 *     }
 * }
 * }</pre>
 *
 * <p>A {@code CssContent} implementation's CSS is one resource in the same jar, mirroring its canonical name -
 * {@code hue/captains/singapura/js/homing/design/styles/editorial/PlainButton.css} - with no class name in it: its marks,
 * invalid CSS, are filled in when it is aggregated ({@code $casting}, {@code &$word}, {@code $name}). A delegating one
 * has none.</p>
 *
 * @param <Sc> its style's colour scheme: what the colours it imports are held to, by the compiler
 * @param <S>  its style
 * @param <C>  the component cast
 * @param <R>  the role it is cast in: the role catalogue's root, {@code RoleRoot}, on its own
 */
public interface CastingStyle<Sc extends ColorScheme, S extends DesignStyle<Sc>, C extends Component<?>, R extends RoleNode>
        extends StatelessFunctionalObject {

    /** What the style does with the casting: its own CSS, or a delegation to another implementation of the component. */
    StylingRes<Sc, C> style();
}
