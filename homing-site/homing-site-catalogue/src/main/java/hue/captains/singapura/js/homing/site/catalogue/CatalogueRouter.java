package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Router;

import java.util.Objects;
import java.util.Optional;

/**
 * The default router: a {@link CatalogueTree} served at a mount.
 *
 * <p>Path in: the mount is stripped and the rest walked down the tree. A
 * vertex answers with its {@link Listing} page, a leaf with the page it
 * places; either is handed the {@link Trail} the walk wrote down when it is
 * {@link Placed}, and a plain navigable is served as it is. A path not under
 * the mount, or one the tree cannot follow, is empty — the site contract's
 * miss — and {@link #resolution(Path)} is there for a site that wants to say
 * why.</p>
 *
 * <p>Path out: {@link #hrefOf} mints the address of a vertex or a placed
 * page from the mount and the tree, which is the only way an address is
 * made here. The mount is the router's, so the same tree serves at
 * {@code /} for one site and at {@code /cat} for another, and neither tree
 * nor listing knows which.</p>
 */
public final class CatalogueRouter implements Router {

    private final CatalogueTree tree;
    private final Path mount;
    private final Listing listing;

    private CatalogueRouter(CatalogueTree tree, Path mount, Listing listing) {
        this.tree    = Objects.requireNonNull(tree, "CatalogueRouter.tree");
        this.mount   = Objects.requireNonNull(mount, "CatalogueRouter.mount");
        this.listing = Objects.requireNonNull(listing, "CatalogueRouter.listing");
    }

    /** The tree under {@code root}, served at {@code mount}, vertices shown by {@link HtmlListing}. */
    public static CatalogueRouter at(Path mount, L0_Catalogue<?> root) {
        return new CatalogueRouter(CatalogueTree.of(root), mount, HtmlListing.INSTANCE);
    }

    /** An already-read tree, served at {@code mount}. */
    public static CatalogueRouter at(Path mount, CatalogueTree tree) {
        return new CatalogueRouter(tree, mount, HtmlListing.INSTANCE);
    }

    /** This router with its vertices shown by {@code listing}. */
    public CatalogueRouter listing(Listing listing) {
        return new CatalogueRouter(tree, mount, listing);
    }

    public CatalogueTree tree()  { return tree; }
    public Path          mount() { return mount; }

    // ── Path out ──────────────────────────────────────────────────────────────

    /** The address {@code c} is served at. */
    public String hrefOf(Catalogue<?> c) {
        return mount.plus(tree.pathOf(c)).toString();
    }

    /** The address {@code page} is served at, when the tree places it. */
    public Optional<String> hrefOf(Navigable page) {
        return tree.pathOf(page).map(p -> mount.plus(p).toString());
    }

    /** The trail a resolution's walk wrote down; {@link Trail#NONE} for a miss. */
    public Trail trailFor(Resolution r) {
        return switch (r) {
            case Resolution.AtCatalogue(var path, var c) -> lineage(c);
            case Resolution.AtLeaf(var path, var parent, var leaf) ->
                    lineage(parent).then(leaf.name(), mount.plus(path).toString());
            case Resolution.Miss ignored -> Trail.NONE;
        };
    }

    private Trail lineage(Catalogue<?> c) {
        Trail t = Trail.NONE;
        for (Catalogue<?> at : tree.lineageOf(c)) t = t.then(at.name(), hrefOf(at));
        return t;
    }

    // ── Path in ───────────────────────────────────────────────────────────────

    /** The tree's answer for {@code path}; empty when the path is not under the mount at all. */
    public Optional<Resolution> resolution(Path path) {
        return path.under(mount).map(tree::resolve);
    }

    @Override
    public Optional<Navigable> resolve(Path path) {
        return resolution(path).flatMap(r -> switch (r) {
            case Resolution.AtCatalogue(var p, var c) -> Optional.of(placed(listing.pageFor(c, this), trailFor(r)));
            case Resolution.AtLeaf(var p, var parent, var leaf) ->
                    Optional.of(leaf.page() instanceof Placed placed ? placed(placed, trailFor(r)) : leaf.page());
            case Resolution.Miss ignored -> Optional.empty();
        });
    }

    /** The page bound to its trail, as the site contract sees it. */
    private static Navigable placed(Placed page, Trail trail) {
        return query -> page.html(trail, query);
    }
}
