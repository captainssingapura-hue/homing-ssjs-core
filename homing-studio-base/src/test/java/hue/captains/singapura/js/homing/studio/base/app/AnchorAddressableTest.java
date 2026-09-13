package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.studio.base.DocRegistry;
import hue.captains.singapura.js.homing.studio.base.Doc;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * RFC 0058 — a sub-node: params that name a path INSIDE a positioned node. The
 * path index never holds it (a fragment is no position); the app maps its args
 * to the node it lives at plus the anchor, and the registry answers with the
 * node's path and the fragment appended.
 */
class AnchorAddressableTest {

    /** A shelf of books: the shelf is the node, a book is a path inside it. */
    public static final class Shelf implements AppModule<Shelf.Params, Shelf>, AnchorAddressable<Shelf.Params> {
        public static final Shelf INSTANCE = new Shelf();
        private Shelf() {}
        public record Params(String shelf, String book) implements AppModule._Param {}
        record appMain() implements AppModule._AppMain<Params, Shelf> {}
        static final ParamCodec<Params> CODEC = new ParamCodec<>() {
            @Override public Decoded<Params> from(Map<String, List<String>> q) {
                String s = QueryString.first(q, "shelf");
                if (s == null) return Decoded.missing("shelf");
                return Decoded.ok(new Params(s, QueryString.first(q, "book")));
            }
            @Override public Map<String, List<String>> to(Params p) {
                return p.book() == null ? QueryString.of("shelf", p.shelf())
                                        : Map.of("shelf", List.of(p.shelf()), "book", List.of(p.book()));
            }
        };
        @Override public String simpleName() { return "shelf"; }
        @Override public String title()      { return "Shelf"; }
        @Override public Class<Params> paramsType() { return Params.class; }
        @Override public ParamCodec<Params> paramCodec() { return CODEC; }
        @Override public ImportsFor<Shelf> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Shelf> exports() {
            return new ExportsOf<>(this, List.<Exportable<Shelf>>of(new appMain()));
        }
        @Override public Optional<Anchored<Params>> anchorOf(Params p) {
            if (p.book() == null) return Optional.empty();
            return Optional.of(new Anchored<>(new Params(p.shelf(), null), "book/" + p.book()));
        }
    }

    record Root() implements L0_Catalogue<Root> {
        static final Root INSTANCE = new Root();
        @Override public String name() { return "Library"; }
        @Override public List<Entry<Root>> leaves() {
            return List.of(Entry.of(this,
                    new Navigable<>(Shelf.INSTANCE, new Shelf.Params("fiction", null), "Fiction", ""),
                    new hue.captains.singapura.js.homing.tree.NodeName("fiction")));
        }
    }

    private static CatalogueRegistry registry() {
        return new CatalogueRegistry(new StudioBrand("Test", Root.class),
                new DocRegistry(List.<Doc>of()), List.of(Root.INSTANCE));
    }

    @Test
    void theNodeResolvesAsAnyLeaf_andASubNodeAsItsPathPlusAnchor() {
        var r = registry();
        assertEquals("/cat/fiction", r.pathForFlat("shelf", Map.of("shelf", List.of("fiction"))).toUrl());
        // The sub-node is not in the index…
        assertNull(r.pathForFlat("shelf", Map.of("shelf", List.of("fiction"), "book", List.of("dune"))));
        // …but the anchored address answers for it.
        assertEquals("/cat/fiction#book/dune",
                r.anchoredUrlForFlat("shelf", Map.of("shelf", List.of("fiction"), "book", List.of("dune"))));
    }

    @Test
    void noAnswerWhenTheNodeIsUnpositionedOrTheParamsNameTheNodeItself() {
        var r = registry();
        // A shelf nobody placed: the book has no node to be inside.
        assertNull(r.anchoredUrlForFlat("shelf", Map.of("shelf", List.of("history"), "book", List.of("x"))));
        // The node itself is not a sub-node; pathForFlat is the route for it.
        assertNull(r.anchoredUrlForFlat("shelf", Map.of("shelf", List.of("fiction"))));
        // An app that is not anchor-addressable, or unknown, answers nothing.
        assertNull(r.anchoredUrlForFlat("no-such-app", Map.of("shelf", List.of("fiction"))));
        assertNull(r.anchoredUrlForFlat("shelf", Map.of()));
    }
}
