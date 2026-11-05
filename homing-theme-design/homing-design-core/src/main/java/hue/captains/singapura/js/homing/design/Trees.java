package hue.captains.singapura.js.homing.design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The walks and the guards over the tree of targets, derived from types alone: its leaves in
 * {@code permits} order, a leaf's token and singleton, and the tree's guard - siblings' property sets
 * are disjoint - which a test over the tree holds.
 */
public final class Trees {

    private Trees() {}

    /** Refuses {@code t} unless it is a leaf of the target tree: a record that is a {@link Target}. */
    public static void requireTargetLeaf(Class<?> t) {
        if (!Target.class.isAssignableFrom(t) || !t.isRecord())
            throw new IllegalArgumentException(t.getName() + ": a target coordinate is a leaf of the sealed tree");
    }

    /** The singleton of a target leaf — every leaf declares {@code INSTANCE}. */
    public static Target targetInstance(Class<?> leaf) {
        try { return (Target) leaf.getField("INSTANCE").get(null); }
        catch (ReflectiveOperationException e) { throw new IllegalStateException(leaf.getName() + " has no INSTANCE", e); }
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

    /** The branches: the nodes whose children are leaves - {@code Color}, and each branch under {@code Colorless}. */
    private static List<Class<?>> branches() {
        var out = new ArrayList<Class<?>>();
        var todo = new ArrayList<Class<?>>(List.of(Target.class));
        while (!todo.isEmpty()) {
            Class<?> node = todo.remove(0);
            Class<?>[] permitted = node.getPermittedSubclasses();
            if (permitted == null) continue;
            if (permitted[0].isRecord()) out.add(node);
            else todo.addAll(List.of(permitted));
        }
        return out;
    }

    /** The sealed tree's guard: within a branch, no property is owned twice. */
    public static List<String> propertyCollisions() {
        var out = new ArrayList<String>();
        for (Class<?> branch : branches()) {
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
