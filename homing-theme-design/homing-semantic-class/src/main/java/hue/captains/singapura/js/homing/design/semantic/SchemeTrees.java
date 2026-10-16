package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What can be worked out of schemes' declarations (RFC 0067, D49): each scheme's declaration, a scheme's layers - the
 * declarations from the base's up to its own - and its full tree, read from them ({@link ReadColors}). Used by the
 * registry's rules as it is made, and by whoever reads the registry after: the registry itself is its declarations
 * alone ({@link SchemeRegistry}).
 */
public record SchemeTrees() implements StatelessFunctionalObject {

    public static final SchemeTrees INSTANCE = new SchemeTrees();

    /** A scheme's layers as its declarations give them. */
    public sealed interface Layers permits Layers.Found, Layers.Missing, Layers.ComesBack {
        /** Every layer's declaration, the base's first and the scheme's own last. */
        record Found(List<SchemeDeclaration<?>> declarations) implements Layers {
            public Found { declarations = List.copyOf(declarations); }
        }
        /** A scheme below it - or the scheme itself - that no declaration declares; {@code above} the one on top of it, if any. */
        record Missing(Optional<ColorScheme> above, ColorScheme missing) implements Layers {}
        /** The schemes below it come back to one already passed. */
        record ComesBack(ColorScheme at) implements Layers {}
    }

    /** Each scheme's declaration, by its scheme, in the order declared: the first of two for one scheme; none that says none. */
    public Map<Class<?>, SchemeDeclaration<?>> byScheme(List<SchemeDeclaration<?>> declarations) {
        var out = new LinkedHashMap<Class<?>, SchemeDeclaration<?>>();
        for (SchemeDeclaration<?> d : declarations) if (d.scheme() != null) out.putIfAbsent(d.scheme().getClass(), d);
        return out;
    }

    /** A scheme's layers, down the schemes it says it is on top of, each from the declaration of it. */
    public Layers layers(ColorScheme scheme, Map<Class<?>, SchemeDeclaration<?>> byScheme) {
        var out = new ArrayList<SchemeDeclaration<?>>();
        var passed = new HashSet<Class<?>>();
        ColorScheme above = null;
        for (ColorScheme at = scheme; at != null; above = at, at = at.below().orElse(null)) {
            if (!passed.add(at.getClass())) return new Layers.ComesBack(at);
            SchemeDeclaration<?> d = byScheme.get(at.getClass());
            if (d == null) return new Layers.Missing(Optional.ofNullable(above), at);
            out.add(0, d);
        }
        return new Layers.Found(out);
    }

    /** Every scheme a registry registers, in the order registered. */
    public List<ColorScheme> schemes(SchemeRegistry registry) {
        return registry.declarations().stream().<ColorScheme>map(SchemeDeclaration::scheme).toList();
    }

    /** A scheme's full tree, read from the registry's declarations; none when the registry does not register it. */
    public Optional<ColorTree> find(SchemeRegistry registry, ColorScheme scheme) {
        if (scheme == null) return Optional.empty();
        return layers(scheme, byScheme(registry.declarations())) instanceof Layers.Found found
                ? Optional.of(ReadColors.INSTANCE.read(found.declarations()))
                : Optional.empty();
    }

    /** A scheme's full tree, read from the registry's declarations; refused when the registry does not register it. */
    public ColorTree tree(SchemeRegistry registry, ColorScheme scheme) {
        return find(registry, scheme).orElseThrow(() -> new IllegalArgumentException((scheme == null ? "no scheme" : scheme.name())
                + " is not registered: an instance registers its schemes' declarations"));
    }
}
