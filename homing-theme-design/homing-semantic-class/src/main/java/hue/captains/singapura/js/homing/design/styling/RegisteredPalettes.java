package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.ColorPalette;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.SchemeTrees;
import hue.captains.singapura.js.homing.design.styles.PaletteDeclaration;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What can be worked out of an instance's palettes (RFC 0067, D47, D49): every palette of its jars, one by its token,
 * and the tree a palette is written over - its scheme's, as the instance's schemes read it ({@link SchemeTrees}). Used by
 * the registry's rules as it is made, and by whoever reads the registry after: the registry itself is its jars and the
 * schemes alone ({@link PaletteRegistry}).
 */
public record RegisteredPalettes() implements StatelessFunctionalObject {

    public static final RegisteredPalettes INSTANCE = new RegisteredPalettes();

    /** Every palette of these jars: each jar's, in the order the jars are listed, the default first. */
    public List<ColorPalette<?>> palettes(List<PaletteDeclaration> jars) {
        return jars.stream().<ColorPalette<?>>flatMap(d -> d.palettes().stream()).toList();
    }

    /** Every palette registered: each jar's, in the order registered, the default first. */
    public List<ColorPalette<?>> palettes(PaletteRegistry registry) { return palettes(registry.jars()); }

    /** A palette registered, by its token. */
    public Optional<ColorPalette<?>> palette(PaletteRegistry registry, String token) {
        return palettes(registry).stream().filter(p -> p.token().equals(token)).findFirst();
    }

    /** The colour tree a palette registered is written over: its scheme's. */
    public ColorTree tree(PaletteRegistry registry, ColorPalette<?> palette) {
        if (!palettes(registry).contains(palette)) throw new IllegalArgumentException(palette.token() + " is not registered");
        ColorScheme scheme = palette.scheme();
        if (scheme == null) throw new IllegalArgumentException(palette.token() + " says no scheme");
        return SchemeTrees.INSTANCE.tree(registry.schemes(), scheme);
    }

    /** The trees the registered palettes are written over, by their schemes' classes, in the order of the palettes. */
    public Map<Class<?>, ColorTree> trees(PaletteRegistry registry) {
        var out = new LinkedHashMap<Class<?>, ColorTree>();
        for (ColorPalette<?> p : palettes(registry))
            if (!out.containsKey(p.scheme().getClass())) out.put(p.scheme().getClass(), SchemeTrees.INSTANCE.tree(registry.schemes(), p.scheme()));
        return Collections.unmodifiableMap(out);
    }
}
