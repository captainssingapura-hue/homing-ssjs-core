package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.design.color.ColorLeaf;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * The colours one component's design imports in a style (RFC 0067, appendix Two Classes per Leaf, D32): every colour of
 * the tree its rules use - the component on its own and in every role, in each of its states - and no other. Each is one
 * the style's scheme sees, by the compiler: the scheme is the type argument, and a colour of a scheme the style's is not
 * over does not fit {@code ColorLeaf<? super S, ?, ?>}. The style's check holds its CSS to them: a colour used and not
 * imported is refused, and so is one imported and used nowhere.
 *
 * <pre>{@code
 * new ColorImports<>(HouseMarks.Lamp.INSTANCE, new Accent(), new Quiet())
 * }</pre>
 *
 * @param component the component whose design it is
 * @param colors    what it imports, each once, in the order it lists them
 * @param <S>       the scheme of the style whose design it is
 */
public record ColorImports<S extends ColorScheme>(Component<?> component, List<ColorLeaf<? super S, ?, ?>> colors) implements ValueObject {

    public ColorImports {
        Objects.requireNonNull(component, "ColorImports.component");
        colors = List.copyOf(Objects.requireNonNull(colors, "ColorImports.colors"));
        if (colors.isEmpty())
            throw new IllegalArgumentException(component.token() + " imports no colour: a design that uses none lists no import");
        var seen = new HashSet<Class<?>>();
        for (ColorLeaf<? super S, ?, ?> c : colors)
            if (!seen.add(c.getClass())) throw new IllegalArgumentException(component.token() + " imports " + c.token() + " twice");
    }

    /** Its colours, listed. */
    @SafeVarargs
    public ColorImports(Component<?> component, ColorLeaf<? super S, ?, ?>... colors) { this(component, List.of(colors)); }
}
