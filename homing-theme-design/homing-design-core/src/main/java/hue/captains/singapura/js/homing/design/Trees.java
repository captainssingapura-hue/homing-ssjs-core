package hue.captains.singapura.js.homing.design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The walks and the guards over the tree of targets, derived from types alone: its leaves in
 * {@code permits} order, a leaf's token and singleton, and the tree's guard - siblings' property sets
 * are disjoint - which a test over the tree holds.
 */
public final class Trees {

    private Trees() {}

    private static final Map<Class<?>, Target> TARGETS = new ConcurrentHashMap<>();

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

    /** {@code branch-leaf}: {@code color-background}. */
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
            var seen = new HashMap<String, Target>();
            for (Class<?> leaf : branch.getPermittedSubclasses()) {
                Target t = targetInstance(leaf);
                for (String p : t.properties()) {
                    Target prior = seen.putIfAbsent(p, t);
                    if (prior != null)
                        out.add(branch.getSimpleName() + ": " + p + " owned by " + prior.getClass().getSimpleName() + " and " + leaf.getSimpleName());
                }
            }
        }
        return out;
    }

    /** {@code BoxShadow} → {@code box-shadow}; {@code Color_Surface} → {@code color-surface}. */
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
}
