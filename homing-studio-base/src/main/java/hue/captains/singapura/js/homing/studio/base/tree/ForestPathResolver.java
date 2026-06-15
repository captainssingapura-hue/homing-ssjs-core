package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RFC 0040 — resolves a node's <b>leveled tree path</b> (a sequence of
 * child indices from the forest root) to the actual {@link Doc} sitting at
 * that position, plus the ancestor catalogue labels walked to reach it.
 *
 * <p>This is the server-side half of "positional identity": the Navigator
 * carries no stamped id, only the structural path ({@code l0,l1,…}); this
 * resolver walks the same forest the renderer drew and hands back the node's
 * direct {@code Doc} reference. Because the node already holds the {@code Doc},
 * neither a uuid index nor a leveled content-fetch is needed — the consumer
 * ({@code /open-content}, {@code /open-refs}) reads straight off the result.</p>
 *
 * <p>Child ordering matches every tree builder ({@code CatalogueTreeAdapter}
 * and {@code CatalogueNormalizer}): sub-catalogues first, then {@code OfDoc} /
 * {@code OfStudio} leaves in declaration order; {@code OfIllustration} is
 * skipped, and {@code OfStudio} portals are descended (their proxied source
 * L0 becomes the next catalogue). Identical to the legacy
 * {@code OpenDocGetAction} walk, lifted into studio-base so the default
 * Open endpoints can share it without a studio-workspace dependency.</p>
 *
 * <p>Functional Object: stateless, one {@code INSTANCE}.</p>
 *
 * @since homing-studio-base — RFC 0040 leveled Open
 */
public final class ForestPathResolver {

    public static final ForestPathResolver INSTANCE = new ForestPathResolver();

    private ForestPathResolver() {}

    /**
     * The result of a successful walk: the {@link Doc} at the path, and the
     * ordered display labels of every catalogue descended to reach it (the
     * breadcrumb prelude — the leaf's own title is the consumer's to append).
     */
    public record Resolved(Doc doc, List<String> trail) {}

    /**
     * Walk {@code root}'s forest by the child-index {@code path} to the
     * {@link Doc} it addresses. Empty when the path is out of range or lands
     * on a branch (catalogue/portal) rather than a doc.
     */
    public Optional<Resolved> resolve(Catalogue<?> root, List<Integer> path) {
        if (root == null || path == null || path.isEmpty()) return Optional.empty();
        Catalogue<?> cat = root;
        var trail = new ArrayList<String>();
        for (int i = 0; i < path.size(); i++) {
            trail.add(cat.name());                       // every catalogue descended is a crumb
            List<NavChild> children = orderedNavChildren(cat);
            int idx = path.get(i);
            if (idx < 0 || idx >= children.size()) return Optional.empty();
            NavChild child = children.get(idx);
            boolean last = (i == path.size() - 1);
            switch (child) {
                case NavSub ns    -> cat = ns.catalogue();
                case NavPortal np -> cat = np.source();
                case NavDoc nd    -> {
                    if (last) return Optional.of(new Resolved(nd.doc(), List.copyOf(trail)));
                    return Optional.empty();             // doc mid-path — nothing below it
                }
            }
        }
        return Optional.empty();                          // path ended on a branch
    }

    /** The nav children of a catalogue, in canonical tree-builder order. */
    private static List<NavChild> orderedNavChildren(Catalogue<?> cat) {
        var out = new ArrayList<NavChild>();
        for (Catalogue<?> sub : cat.subCatalogues()) out.add(new NavSub(sub));
        for (Entry<?> entry : cat.leaves()) {
            if (entry instanceof Entry.OfDoc<?, ?> od) {
                out.add(new NavDoc(od.doc()));
            } else if (entry instanceof Entry.OfStudio<?, ?> os) {
                out.add(new NavPortal(os.proxy().source()));
            }
            // OfIllustration — skipped, matching the tree builders.
        }
        return out;
    }

    private sealed interface NavChild permits NavSub, NavDoc, NavPortal {}
    private record NavSub(Catalogue<?> catalogue) implements NavChild {}
    private record NavDoc(Doc doc) implements NavChild {}
    private record NavPortal(Catalogue<?> source) implements NavChild {}
}
