package hue.captains.singapura.js.homing.ui.menu.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * The walk and the guard over a {@link ContextMenuKind}'s tree. The compiler
 * holds the depth and the parent types; what it cannot hold is checked here,
 * in Java, before anything is stamped for any stack: every child names the
 * node that lists it as its parent, every id is unique within the kind
 * across its levels, every label is text, an icon word is a leaf of the
 * vocabulary, and a third-level row lists nothing. Depth is counted from
 * the kind: its rows are at 1, the last level at {@link #MAX_DEPTH}.
 */
public final class MenuTrees {

    private MenuTrees() {}

    /** The deepest level a row may be at: the third. */
    public static final int MAX_DEPTH = 3;

    /** {@code ChangeAnimal} → {@code change-animal}; {@code S10} → {@code s10}; {@code HTMLThing} → {@code html-thing}. */
    public static String kebab(String simpleName) {
        var sb = new StringBuilder();
        for (int i = 0; i < simpleName.length(); i++) {
            char c = simpleName.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                char prev = simpleName.charAt(i - 1);
                boolean nextLower = i + 1 < simpleName.length() && Character.isLowerCase(simpleName.charAt(i + 1));
                if (Character.isLowerCase(prev) || Character.isDigit(prev) || (Character.isUpperCase(prev) && nextLower)) sb.append('-');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /** A kind's name from its class: {@code AnimalMenu} → {@code animal}; a class not so suffixed is its whole name in kebab. */
    public static String kindOf(Class<?> kind) {
        String name = kind.getSimpleName();
        if (name.endsWith("Menu") && name.length() > 4) name = name.substring(0, name.length() - 4);
        return kebab(name);
    }

    /** Every row of the kind, parents before children, in listing order. */
    public static List<MenuRow<?, ?>> rows(ContextMenuKind<?> kind) {
        var out = new ArrayList<MenuRow<?, ?>>();
        walk(kind, (row, depth) -> out.add(row));
        return List.copyOf(out);
    }

    /** The row with an id, or null. */
    public static MenuRow<?, ?> find(ContextMenuKind<?> kind, String id) {
        for (MenuRow<?, ?> r : rows(kind)) if (r.id().equals(id)) return r;
        return null;
    }

    /** Walks the rows, parents before children, each with its depth from the kind (1 for a first-level row). */
    public static void walk(ContextMenuKind<?> kind, BiConsumer<MenuRow<?, ?>, Integer> visit) {
        for (MenuRow<?, ?> r : kind.children()) walk(r, 1, visit);
    }
    private static void walk(MenuRow<?, ?> row, int depth, BiConsumer<MenuRow<?, ?>, Integer> visit) {
        visit.accept(row, depth);
        for (MenuRow<?, ?> c : row.children()) walk(c, depth + 1, visit);
    }

    /** The problems with a kind's tree, in walking order; empty when it is sound. */
    public static List<String> validate(ContextMenuKind<?> kind) {
        var problems = new ArrayList<String>();
        String k = kind.kind();
        if (k.isBlank()) problems.add(kind.getClass().getSimpleName() + ": a kind needs a name");
        if (kind.children() == null || kind.children().isEmpty()) { problems.add(k + ": a kind lists no rows"); return List.copyOf(problems); }
        var ids = new HashSet<String>();
        check(k, kind, kind.children(), 1, ids, problems);
        return List.copyOf(problems);
    }

    private static void check(String kind, MenuNode<?> parent, List<? extends MenuRow<?, ?>> rows, int depth, Set<String> ids, List<String> problems) {
        if (rows == null) { problems.add(kind + ": " + parent.id() + " lists null"); return; }
        if (depth > MAX_DEPTH) { if (!rows.isEmpty()) problems.add(kind + ": " + parent.id() + " is at the last level and lists " + rows.size()); return; }
        for (MenuRow<?, ?> row : rows) {
            String id = row.id();
            if (row.parent() == null || !Objects.equals(row.parent(), parent))
                problems.add(kind + ": " + id + " is listed under " + parent.id() + " but names " + (row.parent() == null ? "no parent" : row.parent().id()));
            if (!ids.add(id)) problems.add(kind + ": row id repeated: " + id);
            if (row.label() == null || row.label().isBlank()) problems.add(kind + ": " + id + " has no label");
            if (row.icon() != null && !row.icon().isRecord()) problems.add(kind + ": " + id + " names " + row.icon().getSimpleName() + " as its icon, which is not a word");
            check(kind, row, row.children(), depth + 1, ids, problems);
        }
    }

    /** {@link #validate}, thrown. */
    public static <K extends ContextMenuKind<?>> K requireValid(K kind) {
        var problems = validate(kind);
        if (!problems.isEmpty()) throw new IllegalArgumentException("ContextMenuKind " + kind.kind() + ": " + String.join("; ", problems));
        return kind;
    }
}
