package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A catalogue tree, read once from its root and checked: every vertex and
 * every page has one path, and the path is the slugs from the root.
 *
 * <p>Reading walks {@link Catalogue#subCatalogues()} down from the root and
 * refuses the tree on the first of these:</p>
 * <ul>
 *   <li>a sub-catalogue whose {@code parent()} is not the catalogue that
 *       listed it — the two declarations of an edge must agree;</li>
 *   <li>a catalogue listed at a level its type does not allow;</li>
 *   <li>a catalogue reached twice — a vertex has one position;</li>
 *   <li>two children of one vertex with the same slug, whether both are
 *       catalogues, both leaves, or one of each — a segment names one thing;</li>
 *   <li>one navigable placed by two leaves — a page has one path, so a link to
 *       it can be minted rather than chosen.</li>
 * </ul>
 *
 * <p>The tree knows nothing of how a vertex or a page is shown; it answers
 * {@link #resolve(Path)} with what is at a path and {@link #pathOf} with where
 * a thing is. {@link CatalogueRouter} is what serves it.</p>
 */
public final class CatalogueTree {

    private sealed interface Slot {
        record Vertex(Catalogue<?> catalogue) implements Slot {}
        record Page(Leaf<?> leaf) implements Slot {}
    }

    private final L0_Catalogue<?> root;
    private final List<Catalogue<?>> preOrder = new ArrayList<>();
    private final Map<Class<?>, Catalogue<?>> parents = new LinkedHashMap<>();
    private final Map<Class<?>, Path> vertexPaths = new LinkedHashMap<>();
    private final Map<Class<?>, Map<String, Slot>> children = new LinkedHashMap<>();
    private final Map<Navigable, Path> pagePaths = new LinkedHashMap<>();

    private CatalogueTree(L0_Catalogue<?> root) {
        this.root = Objects.requireNonNull(root, "CatalogueTree.root");
        walk(root, Path.ROOT);
    }

    /** Reads and checks the tree under {@code root}. */
    public static CatalogueTree of(L0_Catalogue<?> root) {
        return new CatalogueTree(root);
    }

    private void walk(Catalogue<?> vertex, Path at) {
        Class<?> cls = vertex.getClass();
        if (vertexPaths.containsKey(cls)) {
            throw new IllegalArgumentException(
                    "Catalogue " + cls.getName() + " is reached twice: at " + vertexPaths.get(cls)
                    + " and at " + at + ". A vertex has one position");
        }
        vertexPaths.put(cls, at);
        preOrder.add(vertex);

        var slots = new LinkedHashMap<String, Slot>();
        int level = levelOf(vertex);

        List<? extends Catalogue<?>> subs = vertex.subCatalogues();
        if (subs == null) throw new IllegalArgumentException(cls.getName() + " has null subCatalogues()");
        for (Catalogue<?> sub : subs) {
            if (sub == null) throw new IllegalArgumentException(cls.getName() + " lists a null sub-catalogue");
            var declared = declaredParentOf(sub).orElse(null);
            if (declared == null || declared.getClass() != cls) {
                throw new IllegalArgumentException(
                        cls.getName() + " lists " + sub.getClass().getName() + " as a sub-catalogue, but its parent() is "
                        + (declared == null ? "none (it is a root)" : declared.getClass().getName())
                        + ". The two declarations of an edge must agree");
            }
            if (levelOf(sub) != level + 1) {
                throw new IllegalArgumentException(
                        sub.getClass().getName() + " is level " + levelOf(sub) + " but sits under level " + level);
            }
            claim(slots, sub.slug().value(), new Slot.Vertex(sub), vertex);
            parents.put(sub.getClass(), vertex);
        }

        List<? extends Leaf<?>> leaves = vertex.leaves();
        if (leaves == null) throw new IllegalArgumentException(cls.getName() + " has null leaves()");
        for (Leaf<?> leaf : leaves) {
            if (leaf == null) throw new IllegalArgumentException(cls.getName() + " lists a null leaf");
            claim(slots, leaf.slug().value(), new Slot.Page(leaf), vertex);
            Path here = at.child(leaf.slug().value());
            Path already = pagePaths.putIfAbsent(leaf.page(), here);
            if (already != null) {
                throw new IllegalArgumentException(
                        "Navigable " + leaf.page() + " is placed twice: at " + already + " and at " + here
                        + ". A page has one path");
            }
        }
        children.put(cls, Collections.unmodifiableMap(slots));

        for (Catalogue<?> sub : subs) walk(sub, at.child(sub.slug().value()));
    }

    private static void claim(Map<String, Slot> slots, String slug, Slot slot, Catalogue<?> under) {
        Slot prior = slots.putIfAbsent(slug, slot);
        if (prior != null) {
            throw new IllegalArgumentException(
                    "Slug '" + slug + "' under " + under.getClass().getName() + " names two things: "
                    + describe(prior) + " and " + describe(slot));
        }
    }

    private static String describe(Slot s) {
        return switch (s) {
            case Slot.Vertex v -> "catalogue " + v.catalogue().getClass().getName();
            case Slot.Page p   -> "leaf '" + p.leaf().name() + "'";
        };
    }

    // ── Reading ───────────────────────────────────────────────────────────────

    public L0_Catalogue<?> root() { return root; }

    /** Every vertex, root first, each parent before its children. */
    public List<Catalogue<?>> all() { return Collections.unmodifiableList(preOrder); }

    public int size() { return preOrder.size(); }

    public boolean contains(Catalogue<?> c) { return vertexPaths.containsKey(c.getClass()); }

    /** The vertex above {@code c}; empty for the root. */
    public Optional<Catalogue<?>> parentOf(Catalogue<?> c) {
        requireIn(c);
        return Optional.ofNullable(parents.get(c.getClass()));
    }

    /** Root first, down to and including {@code c}. */
    public List<Catalogue<?>> lineageOf(Catalogue<?> c) {
        requireIn(c);
        var out = new ArrayList<Catalogue<?>>();
        for (Catalogue<?> at = c; at != null; at = parents.get(at.getClass())) out.add(at);
        Collections.reverse(out);
        return out;
    }

    /** The slugs from the root to {@code c}; the root's is {@link Path#ROOT}. */
    public Path pathOf(Catalogue<?> c) {
        requireIn(c);
        return vertexPaths.get(c.getClass());
    }

    /** The one path a placed page has; empty when {@code page} is not in this tree. */
    public Optional<Path> pathOf(Navigable page) {
        return Optional.ofNullable(pagePaths.get(page));
    }

    /** What is at {@code path}, or where the walk stopped. */
    public Resolution resolve(Path path) {
        Objects.requireNonNull(path, "path");
        Catalogue<?> at = root;
        Catalogue<?> parent = null;
        Leaf<?> leaf = null;
        for (int i = 0; i < path.depth(); i++) {
            if (leaf != null) return new Resolution.Miss(path, i, Resolution.Reason.PAST_A_LEAF);
            Slot next = children.get(at.getClass()).get(path.segments().get(i));
            switch (next) {
                case Slot.Vertex v -> { parent = at; at = v.catalogue(); }
                case Slot.Page p   -> { parent = at; leaf = p.leaf(); }
                case null          -> { return new Resolution.Miss(path, i, Resolution.Reason.NO_SUCH_CHILD); }
            }
        }
        return leaf != null ? new Resolution.AtLeaf(path, parent, leaf)
                            : new Resolution.AtCatalogue(path, at);
    }

    // ── Levels ────────────────────────────────────────────────────────────────

    /** 0 for a root, up to 8. */
    public static int levelOf(Catalogue<?> c) {
        return switch (c) {
            case L0_Catalogue<?> ignored    -> 0;
            case L1_Catalogue<?, ?> ignored -> 1;
            case L2_Catalogue<?, ?> ignored -> 2;
            case L3_Catalogue<?, ?> ignored -> 3;
            case L4_Catalogue<?, ?> ignored -> 4;
            case L5_Catalogue<?, ?> ignored -> 5;
            case L6_Catalogue<?, ?> ignored -> 6;
            case L7_Catalogue<?, ?> ignored -> 7;
            case L8_Catalogue<?, ?> ignored -> 8;
        };
    }

    /** The parent a catalogue declares for itself; empty for a root. */
    public static Optional<Catalogue<?>> declaredParentOf(Catalogue<?> c) {
        return switch (c) {
            case L0_Catalogue<?> ignored -> Optional.empty();
            case L1_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L2_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L3_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L4_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L5_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L6_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L7_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L8_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
        };
    }

    private void requireIn(Catalogue<?> c) {
        Objects.requireNonNull(c, "catalogue");
        if (!vertexPaths.containsKey(c.getClass())) {
            throw new IllegalArgumentException(c.getClass().getName() + " is not in this tree (root "
                    + root.getClass().getName() + ")");
        }
    }
}
