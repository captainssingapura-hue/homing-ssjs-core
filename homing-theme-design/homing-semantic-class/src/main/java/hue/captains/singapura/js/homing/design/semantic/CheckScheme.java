package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A scheme's jar held, when it is built, to what only its declarations' types can tell (RFC 0067, D49): its own test
 * calls it, and nothing at run time does - the run time reads what each thing says, and this holds what each says to
 * what its type declares, so the two never differ. Each problem is said whole; none when the jar holds.
 *
 * <ul>
 *   <li>a declaration's scheme the one its type names; the scheme a stateless singleton - its {@code INSTANCE}, no
 *       state - saying as the scheme below it the scheme its class extends, and nothing when it extends none;</li>
 *   <li>its leaves, and the branches that came into its layer with them, each a stateless record of its scheme, saying
 *       as its parent the one its type names;</li>
 *   <li>the declaration, its scheme and its every node in one jar: a scheme and its colours live together;</li>
 *   <li>a style's, a palette's, a design's scheme the one its type names.</li>
 * </ul>
 */
public record CheckScheme() implements StatelessFunctionalObject {

    public static final CheckScheme INSTANCE = new CheckScheme();

    /** What refuses a scheme's declaration, over its tree as the instance's registry of schemes reads it; none when it holds. */
    public List<String> check(SchemeDeclaration<?> declaration, SchemeRegistry registry) {
        var problems = new ArrayList<String>();
        String who = declaration.getClass().getSimpleName();
        ColorScheme scheme = declaration.scheme();
        if (scheme == null) return List.of(who + " declares no scheme");
        Class<?> typed = TypeArguments.INSTANCE.at(declaration.getClass(), SchemeDeclaration.class, 0);
        if (typed != scheme.getClass())
            problems.add(who + " declares " + scheme.name() + ", and its type names " + (typed == null ? "no scheme" : typed.getSimpleName()));
        problems.addAll(scheme(scheme));
        String home = home(declaration.getClass());
        if (!Objects.equals(home, home(scheme.getClass())))
            problems.add(who + " declares " + scheme.name() + ", which lives in another jar: a scheme and its colours live in one jar");
        ColorTree tree;
        try { tree = SchemeTrees.INSTANCE.tree(registry, scheme); } catch (IllegalArgumentException e) { problems.add(e.getMessage()); return problems; }
        for (ColorNode<?, ?, ?> n : tree.nodes()) {
            if (!Schemes.INSTANCE.same(tree.scheme(n), scheme)) continue;   // a layer below: its own declaration's
            Class<?> c = n.getClass();
            if (!c.isRecord() || c.getRecordComponents().length > 0)
                problems.add(n.token() + " is " + (c.isRecord() ? "a record with components" : "no record") + ": a node is a stateless record, its type its meaning");
            Class<?> its = TypeArguments.INSTANCE.at(c, ColorNode.class, 0);
            if (its != scheme.getClass())
                problems.add(n.token() + " is " + (its == null ? "of no scheme" : its.getSimpleName() + "'s") + ", and came into " + scheme.name()
                        + "'s layer: a branch comes in with a leaf of its own scheme, and its scheme lists it below");
            Class<?> parent = TypeArguments.INSTANCE.at(c, ColorNode.class, 1);
            Object says = n.parent();
            if (parent == null || says == null || says.getClass() != parent)
                problems.add(n.token() + " says its parent is " + (says == null ? "nothing" : says.getClass().getSimpleName()) + ", and its type names "
                        + (parent == null ? "none" : parent.getSimpleName()));
            if (!Objects.equals(home, home(c)))
                problems.add(n.token() + " lives in another jar than " + who + ": a scheme and its colours live in one jar");
        }
        return problems;
    }

    /** What refuses a scheme itself: a stateless singleton, saying as its scheme below the scheme its class extends. */
    public List<String> scheme(ColorScheme scheme) {
        var problems = new ArrayList<String>();
        Class<?> c = scheme.getClass();
        try {
            Field instance = c.getField("INSTANCE");
            if (!Modifier.isStatic(instance.getModifiers()) || instance.getType() != c || instance.get(null) == null)
                problems.add(scheme.name() + "'s INSTANCE is not its one object");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            problems.add(scheme.name() + " has no INSTANCE: a scheme is a stateless singleton");
        }
        for (Field f : c.getDeclaredFields())
            if (!Modifier.isStatic(f.getModifiers())) problems.add(scheme.name() + " holds " + f.getName() + ": a scheme is stateless");
        Class<?> up = c.getSuperclass();
        Optional<ColorScheme> below = scheme.below();
        boolean extendsScheme = up != null && ColorScheme.class.isAssignableFrom(up);
        if (extendsScheme && (below.isEmpty() || below.get().getClass() != up))
            problems.add(scheme.name() + " extends " + up.getSimpleName() + ", and says it is on top of " + below.map(ColorScheme::name).orElse("nothing"));
        if (!extendsScheme && below.isPresent())
            problems.add(scheme.name() + " extends no scheme, and says it is on top of " + below.get().name());
        return problems;
    }

    /** What refuses a style's say of its scheme: the scheme it says, against the one its type names. */
    public List<String> says(DesignStyle<?> style) { return says(style.token(), style.scheme(), style.getClass(), DesignStyle.class); }

    /** What refuses a palette's say of its scheme: the scheme it says, against the one its type names. */
    public List<String> says(ColorPalette<?> palette) { return says(palette.token(), palette.scheme(), palette.getClass(), ColorPalette.class); }

    /** What refuses a design's say of its scheme: the scheme it says, against the one its type names. */
    public List<String> says(Design<?, ?> design) { return says(design.token(), design.scheme(), design.getClass(), Design.class); }

    private static List<String> says(String token, ColorScheme scheme, Class<?> declared, Class<?> generic) {
        Class<?> typed = TypeArguments.INSTANCE.at(declared, generic, 0);
        if (scheme == null) return List.of(token + " says no scheme");
        if (typed != scheme.getClass())
            return List.of(token + " says " + scheme.name() + ", and its type names " + (typed == null ? "no scheme" : typed.getSimpleName()));
        return List.of();
    }

    /** Where a class was loaded from: its jar, or its classes' directory. */
    private static String home(Class<?> c) {
        CodeSource source = c.getProtectionDomain() == null ? null : c.getProtectionDomain().getCodeSource();
        return source == null || source.getLocation() == null ? null : source.getLocation().toString();
    }
}
