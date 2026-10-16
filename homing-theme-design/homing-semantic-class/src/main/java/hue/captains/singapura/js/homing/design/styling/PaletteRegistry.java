package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.semantic.ColorPalette;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.SchemeReaders;
import hue.captains.singapura.js.homing.design.semantic.Schemes;
import hue.captains.singapura.js.homing.design.styles.PaletteDeclaration;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The instance's palettes (RFC 0067, D47): one registry for the whole instance, each jar a sub-registry of its own - its
 * declaration ({@link PaletteDeclaration}) - registered at bootstrap, the default palette the first jar's first. It
 * stands apart from the styles: neither a style nor a scheme knows a palette, and a palette of a scheme no style
 * requires may be registered - it is read all the same, and nothing wears it. Each palette's scheme is read by the
 * reader the instance's finder finds for it ({@link SchemeReaders}).
 *
 * <p>Refused, with every problem at once, when it registers nothing, a jar twice, or two palettes of one token; a
 * palette of no scheme, or of one whose reader is not found; one not whole over its scheme's colours; or one sitting on a
 * palette not registered.</p>
 *
 * @param jars    the jars of palettes, each by its declaration, the default first
 * @param readers how it finds a scheme's reader: the instance's one finder
 * @param trees   each palette's scheme, read, by its scheme
 */
public record PaletteRegistry(List<PaletteDeclaration> jars, SchemeReaders readers, Map<Class<?>, ColorTree> trees) implements ValueObject {

    public PaletteRegistry {
        jars = List.copyOf(jars);
        trees = Collections.unmodifiableMap(new LinkedHashMap<>(trees));
    }

    /** The instance's palettes from its jars, each scheme's reader found by name. */
    public static PaletteRegistry of(List<PaletteDeclaration> jars) { return of(jars, SchemeReaders.BY_NAME); }

    /** The instance's palettes from its jars, each scheme's reader found by the finder it is given. */
    public static PaletteRegistry of(List<PaletteDeclaration> jars, SchemeReaders readers) {
        var problems = new ArrayList<String>();
        var seen = new HashSet<Class<?>>();
        for (PaletteDeclaration d : jars) if (!seen.add(d.getClass())) problems.add(d.getClass().getName() + " is registered twice");
        List<ColorPalette<?>> palettes = flat(jars);
        if (palettes.isEmpty()) problems.add("it registers no palette");
        var tokens = new HashSet<String>();
        for (ColorPalette<?> p : palettes) if (!tokens.add(p.token())) problems.add("two palettes are " + p.token());
        var schemes = new ArrayList<Class<?>>();
        for (ColorPalette<?> p : palettes) {
            Optional<Class<?>> scheme = Schemes.INSTANCE.of(p);
            if (scheme.isEmpty()) problems.add(p.token() + " implements no scheme");
            else if (!schemes.contains(scheme.get())) schemes.add(scheme.get());
        }
        SchemeReaders.Read read = readers.read(schemes);
        problems.addAll(read.problems());
        for (ColorPalette<?> p : palettes) {
            Optional<ColorTree> tree = Schemes.INSTANCE.of(p).map(read.trees()::get);
            if (tree.isEmpty()) continue;   // its scheme is refused, and said so
            problems.addAll(tree.get().check(p));   // whole over its scheme's colours, and a chain down the schemes
            for (Optional<? extends ColorPalette<?>> b = p.below(); b.isPresent(); b = b.get().below())
                if (!palettes.contains(b.get())) { problems.add(p.token() + " sits on " + b.get().token() + ", which is not registered"); break; }
        }
        if (!problems.isEmpty()) throw new IllegalArgumentException("the instance's palettes are refused:\n  " + String.join("\n  ", problems));
        return new PaletteRegistry(jars, readers, read.trees());
    }

    private static List<ColorPalette<?>> flat(List<PaletteDeclaration> jars) {
        return jars.stream().<ColorPalette<?>>flatMap(d -> d.palettes().stream()).toList();
    }

    /** Every palette registered: each jar's, in the order the jars are registered, the default first. */
    public List<ColorPalette<?>> palettes() { return flat(jars); }

    /** A palette registered, by its token. */
    public Optional<ColorPalette<?>> palette(String token) { return palettes().stream().filter(p -> p.token().equals(token)).findFirst(); }

    /** The colour tree a palette is written over: its scheme's. */
    public ColorTree tree(ColorPalette<?> palette) {
        Class<?> scheme = Schemes.INSTANCE.of(palette).orElseThrow(() -> new IllegalArgumentException(palette.token() + " implements no scheme"));
        ColorTree tree = trees.get(scheme);
        if (tree == null) throw new IllegalArgumentException(palette.token() + " is not registered");
        return tree;
    }
}
