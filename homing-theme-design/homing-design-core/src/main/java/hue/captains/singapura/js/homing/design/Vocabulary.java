package hue.captains.singapura.js.homing.design;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The pre-registration of semantic leaves — the one place the open tree is
 * enumerated. A deployment registers the leaves it ships (the framework's
 * nine branches through {@link DesignCrate}, a product's own through its
 * crate) before anything is served; from then on every {@link Target} knows
 * which projections it groups, and a name that was never registered is a name
 * that cannot be served.
 *
 * <p>Registration is by type, at boot, in the crate that owns the leaf: no
 * lookup by string, no scan, no {@code Class.forName}. A leaf registered
 * twice is the same leaf; a leaf that fails the tree's guards is refused at
 * registration, not at first request.</p>
 */
public final class Vocabulary {

    private Vocabulary() {}

    private static final List<Class<? extends Semantic>> LEAVES = new CopyOnWriteArrayList<>();

    /** Register a leaf and, through it, every projection declared inside it. Idempotent. */
    @SafeVarargs
    public static void register(Class<? extends Semantic>... leaves) {
        for (var leaf : leaves) {
            Trees.requireSemanticLeaf(leaf);
            for (Class<?> nested : leaf.getDeclaredClasses())
                if (DesignClass.class.isAssignableFrom(nested)) Trees.coordinates(nested);   // the guards, now
            if (!LEAVES.contains(leaf)) LEAVES.add(leaf);
        }
    }

    /** Every registered leaf, in registration order. */
    public static List<Class<? extends Semantic>> leaves() { return List.copyOf(LEAVES); }

    /** Every registered projection, in registration order. */
    public static List<DesignClass<?, ?>> projections() {
        var out = new ArrayList<DesignClass<?, ?>>();
        for (var leaf : LEAVES) out.addAll(projectionsOf(leaf));
        return List.copyOf(out);
    }

    /** The projections of one leaf, instantiated, in declaration order. */
    public static List<DesignClass<?, ?>> projectionsOf(Class<? extends Semantic> leaf) {
        var out = new ArrayList<DesignClass<?, ?>>();
        for (Class<?> nested : leaf.getDeclaredClasses()) {
            if (!nested.isRecord() || !DesignClass.class.isAssignableFrom(nested)) continue;
            try { out.add((DesignClass<?, ?>) nested.getDeclaredConstructor().newInstance()); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException(nested.getName(), e); }
        }
        return List.copyOf(out);
    }

    /** The registered projections onto one target — what that target's sheet and module carry. */
    public static List<DesignClass<?, ?>> onto(Class<? extends Target> target) {
        var out = new ArrayList<DesignClass<?, ?>>();
        for (var p : projections()) if (p.target() == target) out.add(p);
        return List.copyOf(out);
    }

    /** The registered projections' classes onto each target, keyed by target token — for a deployment's requirement set. */
    public static Map<String, Set<Class<? extends DesignClass<?, ?>>>> byTarget() {
        var out = new LinkedHashMap<String, Set<Class<? extends DesignClass<?, ?>>>>();
        for (var p : projections()) {
            @SuppressWarnings("unchecked") var c = (Class<? extends DesignClass<?, ?>>) p.getClass();
            out.computeIfAbsent(p.targetLeaf().token(), k -> new java.util.LinkedHashSet<>()).add(c);
        }
        return out;
    }

    /** For tests only: forget every registration. */
    static void reset() { LEAVES.clear(); }
}
