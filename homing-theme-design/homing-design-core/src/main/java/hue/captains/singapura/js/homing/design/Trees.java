package hue.captains.singapura.js.homing.design;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The walks and the guards over the two trees. Everything here is derived
 * from types alone — no instance, no registry — and cached per class, so a
 * projection record stays a one-liner and the tokens cannot drift from the
 * names.
 *
 * <p>The guards the open tree needs: a coordinate must be a leaf (a record);
 * a semantic leaf must have exactly one branch; a projection must be declared
 * inside its semantic leaf. The sealed tree's guard: siblings' property sets
 * are disjoint. Each is a refusal at the point the type is first touched,
 * and a rule over a closure repeats it.</p>
 */
public final class Trees {

    private Trees() {}

    /** The two coordinates of a design class. */
    public record Coordinates(Class<? extends Semantic> semantic, Class<? extends Target> target) {}

    private static final Map<Class<?>, Coordinates> COORDINATES = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Target> TARGETS = new ConcurrentHashMap<>();

    // ── design classes ────────────────────────────────────────────────────

    /** Reads {@code DesignClass<S, T>} off the record's declared interfaces; refuses a non-leaf coordinate. */
    public static Coordinates coordinates(Class<?> designClass) {
        return COORDINATES.computeIfAbsent(designClass, Trees::readCoordinates);
    }

    @SuppressWarnings("unchecked")
    private static Coordinates readCoordinates(Class<?> designClass) {
        if (!designClass.isRecord())
            throw new IllegalArgumentException(designClass.getName() + ": a design class is a record");
        for (Type t : designClass.getGenericInterfaces()) {
            if (t instanceof ParameterizedType p && p.getRawType() == DesignClass.class) {
                Class<?> s = (Class<?>) p.getActualTypeArguments()[0];
                Class<?> target = (Class<?>) p.getActualTypeArguments()[1];
                requireSemanticLeaf(s);
                requireTargetLeaf(target);
                if (designClass.getEnclosingClass() != s)
                    throw new IllegalArgumentException(designClass.getName() + ": a projection is declared inside its semantic leaf, "
                            + s.getSimpleName() + " — not in " + (designClass.getEnclosingClass() == null ? "a top-level file" : designClass.getEnclosingClass().getSimpleName()));
                return new Coordinates((Class<? extends Semantic>) s, (Class<? extends Target>) target);
            }
        }
        throw new IllegalArgumentException(designClass.getName() + ": does not implement DesignClass<S, T> directly");
    }

    public static String classToken(Class<?> designClass) {
        Coordinates c = coordinates(designClass);
        return semanticToken(c.semantic()) + "-" + targetToken(c.target());
    }

    // ── semantic leaves ───────────────────────────────────────────────────

    /** A leaf is a record with exactly one branch among its direct interfaces. */
    public static void requireSemanticLeaf(Class<?> s) {
        if (!Semantic.class.isAssignableFrom(s))
            throw new IllegalArgumentException(s.getName() + ": not a Semantic");
        if (!s.isRecord())
            throw new IllegalArgumentException(s.getName() + ": a semantic coordinate is a leaf (a record), not a branch");
        if (branchesOf(s).size() != 1)
            throw new IllegalArgumentException(s.getName() + ": a semantic leaf has exactly one branch; found " + branchesOf(s));
    }

    /** The branches a leaf directly implements — {@code Semantic} itself excluded. */
    public static List<Class<?>> branchesOf(Class<?> leaf) {
        var out = new ArrayList<Class<?>>();
        for (Class<?> i : leaf.getInterfaces())
            if (i != Semantic.class && Semantic.class.isAssignableFrom(i)) out.add(i);
        return out;
    }

    /** The one branch of a leaf. */
    public static Class<?> branchOf(Class<?> leaf) {
        requireSemanticLeaf(leaf);
        return branchesOf(leaf).get(0);
    }

    public static String semanticToken(Class<?> leaf) { return kebab(leaf.getSimpleName()); }

    // ── target leaves ─────────────────────────────────────────────────────

    public static void requireTargetLeaf(Class<?> t) {
        if (!Target.class.isAssignableFrom(t) || !t.isRecord())
            throw new IllegalArgumentException(t.getName() + ": a target coordinate is a leaf of the sealed tree");
    }

    /** The singleton of a target leaf — every leaf declares {@code INSTANCE}. */
    public static Target targetInstance(Class<?> leaf) {
        return TARGETS.computeIfAbsent(leaf, l -> {
            try { return (Target) l.getField("INSTANCE").get(null); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException(l.getName() + " has no INSTANCE", e); }
        });
    }

    /** {@code branch-leaf}. */
    public static String targetToken(Class<?> leaf) {
        Class<?> branch = leaf.getEnclosingClass();
        return kebab(branch.getSimpleName()) + "-" + kebab(leaf.getSimpleName());
    }

    /** Every leaf of the sealed tree, in {@code permits} order. */
    public static List<Target> targetLeaves() {
        var out = new ArrayList<Target>();
        walk(Target.class, out);
        return List.copyOf(out);
    }

    private static void walk(Class<?> node, List<Target> out) {
        Class<?>[] permitted = node.getPermittedSubclasses();
        if (permitted == null) { out.add(targetInstance(node)); return; }
        for (Class<?> p : permitted) walk(p, out);
    }

    /** The sealed tree's guard: within a branch, no property is owned twice. */
    public static List<String> propertyCollisions() {
        var out = new ArrayList<String>();
        for (Class<?> branch : Target.class.getPermittedSubclasses()) {
            var seen = new java.util.HashMap<String, String>();
            for (Class<?> leaf : branch.getPermittedSubclasses()) {
                for (String p : targetInstance(leaf).properties()) {
                    String prior = seen.put(p, leaf.getSimpleName());
                    if (prior != null) out.add(branch.getSimpleName() + ": " + p + " owned by " + prior + " and " + leaf.getSimpleName());
                }
            }
        }
        return out;
    }

    // ── naming ────────────────────────────────────────────────────────────

    /** {@code OnDanger} → {@code on-danger}; {@code Color_Surface} → {@code color-surface}. */
    public static String kebab(String name) {
        var sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') { sb.append('-'); continue; }
            if (Character.isUpperCase(c) && i > 0 && name.charAt(i - 1) != '_') sb.append('-');
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /** The variable stem for a (class, property): the class token, plus the property when the target owns more than one. */
    public static String variable(DesignClass<?, ?> dc, String property) {
        Set<String> owned = dc.targetLeaf().properties();
        return "--" + dc.token() + (owned.size() == 1 ? "" : "-" + property);
    }
}
