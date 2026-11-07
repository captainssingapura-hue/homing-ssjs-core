package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.mpa.Mpa;

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
 * <p>Reading walks {@link Catalogue#subCatalogues()} and {@link Catalogue#grafts()}
 * down from the root - a graft's tree taking its positions under the host, as
 * if it had been written there - and refuses the tree on the first of these:</p>
 * <ul>
 *   <li>a sub-catalogue whose {@code parent()} is not the catalogue that
 *       listed it — the two declarations of an edge must agree;</li>
 *   <li>a catalogue listed at a level its type does not allow;</li>
 *   <li>a catalogue reached twice — a vertex has one position, so a tree
 *       grafted twice, or into itself, is refused;</li>
 *   <li>a vertex deeper than {@link #MAX_DEPTH} in the composed tree — the
 *       depth L0 to L8 allows one tree written alone, and composing trees
 *       does not buy more;</li>
 *   <li>two children of one vertex with the same slug, whether catalogues,
 *       grafted roots or leaves — a segment names one thing;</li>
 *   <li>one navigable placed by two leaves — a page has one path, so a link to
 *       it can be minted rather than chosen, whichever tree placed it.</li>
 * </ul>
 *
 * <p>A grafted root is shown at its graft as the graft says ({@link #shownAs});
 * every other vertex as it shows itself.</p>
 *
 * <p>The tree knows nothing of how a vertex or a page is shown; it answers
 * {@link #resolve(Path)} with what is at a path and {@link #pathOf} with where
 * a thing is. {@link CatalogueRouter} is what serves it.</p>
 */
public final class CatalogueTree {

    /** The deepest a vertex may sit in the composed tree: a root is 0. */
    public static final int MAX_DEPTH = 8;

    /** What a listing or a trail shows for a vertex: its own, or what its graft restated. */
    public record Shown(String name, String summary, String badge, String icon) {}

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
    private final Map<Class<?>, Graft<?>> grafts = new LinkedHashMap<>();
    private final Map<Class<?>, List<Leaf<?>>> leaves = new LinkedHashMap<>();
    private final Mpa mpa;

    /**
     * Reads and checks the tree under {@code root}, its pages made with the site's
     * {@code mpa} - every catalogue's {@link Catalogue#leaves(Mpa)} handed it - so every
     * page of the site wears one chrome, whichever tree placed it.
     */
    public CatalogueTree(L0_Catalogue<?> root, Mpa mpa) {
        this.root = Objects.requireNonNull(root, "CatalogueTree.root");
        this.mpa = Objects.requireNonNull(mpa, "CatalogueTree.mpa");
        walk(root, Path.ROOT);
    }

    /**
     * Reads and checks the tree under {@code root} with no MPA: for a tree of pages
     * that need no site. A catalogue that makes its pages with the site's MPA is
     * refused, and told to be read with one.
     */
    public CatalogueTree(L0_Catalogue<?> root) {
        this(root, NoMpa.INSTANCE);
    }

    private void walk(Catalogue<?> vertex, Path at) {
        Class<?> cls = vertex.getClass();
        if (vertexPaths.containsKey(cls)) {
            throw new IllegalArgumentException(
                    "Catalogue " + cls.getName() + " is reached twice: at " + vertexPaths.get(cls)
                    + " and at " + at + ". A vertex has one position");
        }
        if (at.depth() > MAX_DEPTH) {
            throw new IllegalArgumentException(
                    "Catalogue " + cls.getName() + " sits at depth " + at.depth() + " (" + at + ") in the composed tree;"
                    + " the deepest a vertex may sit is " + MAX_DEPTH);
        }
        vertexPaths.put(cls, at);
        preOrder.add(vertex);

        var slots = new LinkedHashMap<String, Slot>();
        List<? extends Catalogue<?>> subs = subCataloguesIn(vertex, slots);
        List<? extends Graft<?>> grafted = graftsIn(vertex, at, slots);
        pagesIn(vertex, at, slots);
        children.put(cls, Collections.unmodifiableMap(slots));

        for (Catalogue<?> sub : subs) walk(sub, at.child(sub.slug().value()));
        for (Graft<?> graft : grafted) walk(graft.root(), at.child(graft.root().slug().value()));
    }

    /** A vertex's sub-catalogues into its slots: each edge declared both ways, one level down. */
    private List<? extends Catalogue<?>> subCataloguesIn(Catalogue<?> vertex, Map<String, Slot> slots) {
        Class<?> cls = vertex.getClass();
        int level = CatalogueLevels.INSTANCE.levelOf(vertex);
        List<? extends Catalogue<?>> subs = vertex.subCatalogues();
        if (subs == null) throw new IllegalArgumentException(cls.getName() + " has null subCatalogues()");
        for (Catalogue<?> sub : subs) {
            if (sub == null) throw new IllegalArgumentException(cls.getName() + " lists a null sub-catalogue");
            var declared = CatalogueLevels.INSTANCE.declaredParentOf(sub).orElse(null);
            if (declared == null || declared.getClass() != cls) {
                throw new IllegalArgumentException(
                        cls.getName() + " lists " + sub.getClass().getName() + " as a sub-catalogue, but its parent() is "
                        + (declared == null ? "none (it is a root)" : declared.getClass().getName())
                        + ". The two declarations of an edge must agree");
            }
            if (CatalogueLevels.INSTANCE.levelOf(sub) != level + 1) {
                throw new IllegalArgumentException(
                        sub.getClass().getName() + " is level " + CatalogueLevels.INSTANCE.levelOf(sub) + " but sits under level " + level);
            }
            claim(slots, sub.slug().value(), new Slot.Vertex(sub), vertex);
            parents.put(sub.getClass(), vertex);
        }
        return subs;
    }

    /** A vertex's grafts into its slots. A graft's root is a root: it names no parent, so there is no second declaration to agree with. */
    private List<? extends Graft<?>> graftsIn(Catalogue<?> vertex, Path at, Map<String, Slot> slots) {
        Class<?> cls = vertex.getClass();
        List<? extends Graft<?>> grafted = vertex.grafts();
        if (grafted == null) throw new IllegalArgumentException(cls.getName() + " has null grafts()");
        for (Graft<?> graft : grafted) {
            if (graft == null) throw new IllegalArgumentException(cls.getName() + " lists a null graft");
            L0_Catalogue<?> r = graft.root();
            claim(slots, r.slug().value(), new Slot.Vertex(r), vertex);
            if (grafts.putIfAbsent(r.getClass(), graft) != null || r.getClass() == root.getClass()) {
                throw new IllegalArgumentException(
                        "The tree under " + r.getClass().getName() + " is grafted twice, or into itself (at " + at
                        + "). A vertex has one position");
            }
            parents.put(r.getClass(), vertex);
        }
        return grafted;
    }

    /** A vertex's pages into its slots, made with the site's MPA - read once, and kept, so a listing shows the very pages the paths name. */
    private void pagesIn(Catalogue<?> vertex, Path at, Map<String, Slot> slots) {
        Class<?> cls = vertex.getClass();
        List<? extends Leaf<?>> leaves = vertex.leaves(mpa);
        if (leaves == null) throw new IllegalArgumentException(cls.getName() + " has null leaves()");
        this.leaves.put(cls, List.copyOf(leaves));
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
    }

    private void claim(Map<String, Slot> slots, String slug, Slot slot, Catalogue<?> under) {
        Slot prior = slots.putIfAbsent(slug, slot);
        if (prior != null) {
            throw new IllegalArgumentException(
                    "Slug '" + slug + "' under " + under.getClass().getName() + " names two things: "
                    + describe(prior) + " and " + describe(slot));
        }
    }

    private String describe(Slot s) {
        return switch (s) {
            case Slot.Vertex v -> "catalogue " + v.catalogue().getClass().getName();
            case Slot.Page p   -> "leaf '" + p.leaf().name() + "'";
        };
    }

    // ── Reading ───────────────────────────────────────────────────────────────

    /** The tree's root. */
    public L0_Catalogue<?> root() { return root; }

    /** Every vertex, root first, each parent before its children. */
    public List<Catalogue<?>> all() { return Collections.unmodifiableList(preOrder); }

    /** How many vertices the tree has. */
    public int size() { return preOrder.size(); }

    /** Whether {@code c} is a vertex of this tree. */
    public boolean contains(Catalogue<?> c) { return vertexPaths.containsKey(c.getClass()); }

    /** The vertex above {@code c} in the composed tree - a grafted root's is its host; empty for the tree's root. */
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

    /** How deep {@code c} sits in the composed tree: the root is 0. Its declared {@link #levelOf level} is within its own tree. */
    public int depthOf(Catalogue<?> c) { return pathOf(c).depth(); }

    /** The graft that placed {@code c} here, when {@code c} is a grafted root; empty for every other vertex. */
    public Optional<Graft<?>> graftOf(Catalogue<?> c) {
        requireIn(c);
        return Optional.ofNullable(grafts.get(c.getClass()));
    }

    /** What {@code c} is shown as here: as its graft restated it, when it is a grafted root; as it shows itself otherwise. */
    public Shown shownAs(Catalogue<?> c) {
        return graftOf(c).<Shown>map(g -> new Shown(g.name(), g.summary(), g.badge(), g.icon()))
                         .orElseGet(() -> new Shown(c.name(), c.summary(), c.badge(), c.icon()));
    }

    /** The pages in {@code c}, in display order, as the tree read them - made with its MPA. */
    public List<Leaf<?>> leavesOf(Catalogue<?> c) {
        requireIn(c);
        return leaves.get(c.getClass());
    }

    /** The MPA the tree's pages were made with; empty when it was read with none. */
    public Optional<Mpa> mpa() { return mpa == NoMpa.INSTANCE ? Optional.empty() : Optional.of(mpa); }

    /** The vertices under {@code c}, in display order: its sub-catalogues, then the roots it grafts. */
    public List<Catalogue<?>> childrenOf(Catalogue<?> c) {
        requireIn(c);
        var out = new ArrayList<Catalogue<?>>(c.subCatalogues());
        for (Graft<?> g : c.grafts()) out.add(g.root());
        return Collections.unmodifiableList(out);
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

    private void requireIn(Catalogue<?> c) {
        Objects.requireNonNull(c, "catalogue");
        if (!vertexPaths.containsKey(c.getClass())) {
            throw new IllegalArgumentException(c.getClass().getName() + " is not in this tree (root "
                    + root.getClass().getName() + ")");
        }
    }
}
