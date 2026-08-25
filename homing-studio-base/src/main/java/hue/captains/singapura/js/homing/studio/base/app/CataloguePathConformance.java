package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.Doc;

import java.util.ArrayList;

/**
 * RFC 0051 Phase 2 — a reusable check that path and node are inverses across
 * an entire catalogue tree.
 *
 * <p>Lives in main rather than test so every studio can run it against its own
 * tree; the framework's own tests, the self-studio's and the demo's all call
 * the same code. A law worth stating for one tree is worth stating for every
 * downstream tree, and duplicating the walk per repo is how they would come to
 * disagree about what the law says.</p>
 */
public final class CataloguePathConformance {

    private CataloguePathConformance() {}

    /**
     * RFC 0051 Phase 2 — the bijection, over a whole real tree.
     *
     * <p>Walks every catalogue and every positioned leaf, derives its path,
     * resolves that path, and requires the same node back. Unit tests pin the
     * shapes; this is what makes the claim about the corpus rather than about
     * a fixture.</p>
     */
    public static void assertPathBijection(CatalogueRegistry registry) {
        var failures = new ArrayList<String>();
        for (Catalogue<?> c : registry.all()) {
            CataloguePath path = registry.pathOf(c);
            PathResolution back = registry.resolve(path);
            if (!(back instanceof PathResolution.ToCatalogue(var p, var got)) || got != c) {
                failures.add("catalogue " + c.getClass().getName() + " -> " + path.toUrl()
                           + " -> " + describe(back));
            }
            for (Entry<?> e : c.leaves()) {
                if (!(e instanceof Entry.OfDoc<?, ?>(Doc d))) continue;
                CataloguePath leafPath = registry.pathOf(d);
                if (leafPath == null) {
                    failures.add("leaf " + d.title() + " under " + c.getClass().getName()
                               + " has no path (not in docHome)");
                    continue;
                }
                PathResolution leafBack = registry.resolve(leafPath);
                if (!(leafBack instanceof PathResolution.ToLeaf(var lp, var parent, var gotDoc))
                        || !gotDoc.uuid().equals(d.uuid())) {
                    failures.add("leaf " + d.title() + " -> " + leafPath.toUrl()
                               + " -> " + describe(leafBack));
                }
            }
        }
        if (!failures.isEmpty()) {
            throw new AssertionError("RFC 0051 path bijection broken for "
                    + failures.size() + " node(s):\n  " + String.join("\n  ", failures));
        }
    }

    private static String describe(PathResolution r) {
        return switch (r) {
            case PathResolution.ToCatalogue(var p, var c) -> "catalogue " + c.getClass().getName();
            case PathResolution.ToLeaf(var p, var parent, var d) -> "leaf " + d.title();
            case PathResolution.Miss m -> "MISS(" + m.reason() + " at '" + m.at() + "')";
        };
    }
}
