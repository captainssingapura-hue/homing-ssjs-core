package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The instance's colour schemes (RFC 0067, appendix Semantic Colours and Flavours, D49): one declaration for each,
 * registered explicitly at bootstrap as one flat list - as the components are, by module - and every scheme's full tree
 * read from them: its own layer over the layers of the schemes below it, each from its registered declaration
 * ({@link ReadColors}). The style manager takes its styles' trees from it, and the palettes' registry its palettes';
 * a style or palette of a scheme it does not register is refused where it is registered. Nothing is found, and nothing
 * read off a type: a scheme not registered is no scheme of the instance's. A scheme registered and used by nothing is
 * no problem.
 *
 * <p>Refused, with every problem at once, when it registers nothing, a declaration that says no scheme, a scheme twice,
 * a scheme on top of one it does not register, or a tree that cannot be read.</p>
 *
 * @param declarations each scheme's declaration, in the order registered
 * @param trees        each scheme's full tree, by its scheme
 */
public record SchemeRegistry(List<SchemeDeclaration<?>> declarations, Map<Class<?>, ColorTree> trees) implements ValueObject {

    public SchemeRegistry {
        declarations = List.copyOf(declarations);
        trees = Collections.unmodifiableMap(new LinkedHashMap<>(trees));
    }

    /** The instance's schemes, from their declarations: each scheme's tree read, or every problem said at once. */
    public static SchemeRegistry of(List<? extends SchemeDeclaration<?>> declarations) {
        var problems = new ArrayList<String>();
        if (declarations.isEmpty()) problems.add("it registers no scheme");
        var byScheme = new LinkedHashMap<Class<?>, SchemeDeclaration<?>>();
        for (SchemeDeclaration<?> d : declarations) {
            ColorScheme scheme = d.scheme();
            if (scheme == null) { problems.add(d.getClass().getName() + " declares no scheme"); continue; }
            SchemeDeclaration<?> before = byScheme.putIfAbsent(scheme.getClass(), d);
            if (before != null) problems.add(scheme.name() + " is declared twice, by " + before.getClass().getName() + " and "
                    + d.getClass().getName() + ": a scheme has one declaration");
        }
        var trees = new LinkedHashMap<Class<?>, ColorTree>();
        var missing = new HashSet<Class<?>>();
        for (SchemeDeclaration<?> d : byScheme.values()) {
            List<SchemeDeclaration<?>> layers = layers(d.scheme(), byScheme, missing, problems);
            if (layers.isEmpty()) continue;
            try { trees.put(d.scheme().getClass(), ReadColors.INSTANCE.read(layers)); }
            catch (RefusedColors e) { problems.add(d.scheme().name() + "'s tree is refused: " + e.getMessage()); }
        }
        if (!problems.isEmpty()) throw new IllegalArgumentException("the instance's schemes are refused:\n  " + String.join("\n  ", problems));
        return new SchemeRegistry(List.copyOf(byScheme.values()), trees);
    }

    /** A scheme's layers, the base's declaration first: none when one below it is not registered, or the chain comes back. */
    private static List<SchemeDeclaration<?>> layers(ColorScheme scheme, Map<Class<?>, SchemeDeclaration<?>> byScheme,
                                                     java.util.Set<Class<?>> missing, List<String> problems) {
        var out = new ArrayList<SchemeDeclaration<?>>();
        var passed = new HashSet<Class<?>>();
        ColorScheme above = null;
        for (ColorScheme at = scheme; at != null; above = at, at = at.below().orElse(null)) {
            if (!passed.add(at.getClass())) {
                problems.add(scheme.name() + "'s schemes below come back to " + at.name() + ": a scheme is on top of one below it");
                return List.of();
            }
            SchemeDeclaration<?> d = byScheme.get(at.getClass());
            if (d == null) {
                if (missing.add(at.getClass()))
                    problems.add((above == null ? "" : above.name() + " is on top of ") + at.name() + ", which no registered declaration "
                            + "declares: a scheme's schemes below are registered with it");
                return List.of();
            }
            out.add(0, d);
        }
        return out;
    }

    /** Every scheme registered, in the order registered. */
    public List<ColorScheme> schemes() { return declarations.stream().<ColorScheme>map(SchemeDeclaration::scheme).toList(); }

    /** A scheme's full tree; none when the instance does not register it. */
    public Optional<ColorTree> find(ColorScheme scheme) { return Optional.ofNullable(scheme == null ? null : trees.get(scheme.getClass())); }

    /** A scheme's full tree; refused when the instance does not register it. */
    public ColorTree tree(ColorScheme scheme) {
        return find(scheme).orElseThrow(() -> new IllegalArgumentException((scheme == null ? "no scheme" : scheme.name())
                + " is not registered: an instance registers its schemes' declarations"));
    }
}
