package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * What a module imports for its designs in one style it does not own (RFC 0067, appendix Two Classes per Leaf, D32): the
 * components drawn in the files it gives that style - {@code <family>.<module>.css} - the ones it adds and the ones it
 * casts in roles of its own, each design's colours typed by the style's scheme, as the style's own are.
 *
 * @param style   the style, by type
 * @param imports each design's imports, one per component
 * @param <S>     the style's scheme
 */
public record StyleImports<S extends ColorScheme>(Class<? extends DesignStyle<S>> style, List<ColorImports<S>> imports) implements ValueObject {

    public StyleImports {
        Objects.requireNonNull(style, "StyleImports.style");
        imports = List.copyOf(Objects.requireNonNull(imports, "StyleImports.imports"));
    }

    /** Its designs' imports, listed. */
    @SafeVarargs
    public StyleImports(Class<? extends DesignStyle<S>> style, ColorImports<S>... imports) { this(style, List.of(imports)); }
}
