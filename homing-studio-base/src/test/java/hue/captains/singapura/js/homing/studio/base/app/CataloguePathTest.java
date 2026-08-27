package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.app.DocReader;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocRegistry;
import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0051 Phase 2 — path and node are inverses.
 *
 * <p>These are unit-scale; the same laws are exercised against the real
 * studio and demo trees by each repo's boot test.</p>
 */
class CataloguePathTest {

    static final UUID DOC_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    static final Doc LEAF_DOC = new Doc() {
        @Override public UUID   uuid()     { return DOC_ID; }
        @Override public String title()    { return "A Leaf"; }
        @Override public String contents() { return ""; }
        @Override public NodeName slug()   { return new NodeName("a-leaf"); }
    };

    record Root() implements L0_Catalogue<Root> {
        static final Root INSTANCE = new Root();
        @Override public String name() { return "Root"; }
        @Override public List<? extends L1_Catalogue<Root, ?>> subCatalogues() {
            return List.of(Branch.INSTANCE);
        }
    }

    record Branch() implements L1_Catalogue<Root, Branch> {
        static final Branch INSTANCE = new Branch();
        @Override public Root parent()  { return Root.INSTANCE; }
        @Override public String name()  { return "Branch"; }
        @Override public List<Entry<Branch>> leaves() {
            return List.of(Entry.of(this, DocReader.INSTANCE, new DocReader.Params(LEAF_DOC.uuid().toString()), LEAF_DOC));
        }
    }

    private CatalogueRegistry registry() {
        return new CatalogueRegistry(new StudioBrand("Test", Root.class),
                new DocRegistry(List.of(LEAF_DOC)),
                List.of(Root.INSTANCE, Branch.INSTANCE));
    }

    @Test
    void emptyPathIsTheRoot() {
        var r = registry().resolve(new CataloguePath(List.of()));
        assertInstanceOf(PathResolution.ToCatalogue.class, r);
        assertSame(Root.INSTANCE, ((PathResolution.ToCatalogue) r).catalogue());
    }

    @Test
    void pathOfCatalogue_roundTrips() {
        var reg = registry();
        var path = reg.pathOf(Branch.INSTANCE);
        assertEquals("/cat/branch", path.toUrl());
        var back = reg.resolve(path);
        assertInstanceOf(PathResolution.ToCatalogue.class, back);
        assertSame(Branch.INSTANCE, ((PathResolution.ToCatalogue) back).catalogue());
    }

    @Test
    void pathOfDoc_roundTrips() {
        var reg = registry();
        var path = reg.pathOf(LEAF_DOC);
        assertEquals("/cat/branch/a-leaf", path.toUrl());
        var back = reg.resolve(path);
        assertInstanceOf(PathResolution.ToLeaf.class, back);
        assertSame(LEAF_DOC, ((PathResolution.ToLeaf) back).doc());
        assertSame(Branch.INSTANCE, ((PathResolution.ToLeaf) back).parent());
    }

    @Test
    void unknownSegment_missesAtTheSegmentThatFailed() {
        var r = registry().resolveUrl("/cat/branch/nope");
        var miss = assertInstanceOf(PathResolution.Miss.class, r);
        assertEquals(PathResolution.Reason.NO_SUCH_CHILD, miss.reason());
        assertEquals(1, miss.failedAt());
        assertEquals("nope", miss.at());
    }

    @Test
    void pathPastALeaf_isItsOwnReason() {
        // Distinguished from a plain miss because the caller held a real
        // address and appended to it — a stale deep link, not a typo.
        var r = registry().resolveUrl("/cat/branch/a-leaf/extra");
        var miss = assertInstanceOf(PathResolution.Miss.class, r);
        assertEquals(PathResolution.Reason.PAST_A_LEAF, miss.reason());
    }

    @Test
    void nonCataloguePathsAreMissesNotExceptions() {
        // Untrusted input: every one of these is an answer to produce, not a
        // failure to raise.
        assertNull(CataloguePath.parse("/app?app=doc-reader"));
        assertNull(CataloguePath.parse("/cat/../etc/passwd"));
        assertNull(CataloguePath.parse("/cat//double"));
        assertFalse(registry().resolveUrl("/somewhere/else").isHit());
    }

    @Test
    void parse_isTheInverseOfToUrl() {
        var path = new CataloguePath(List.of(new NodeName("branch"), new NodeName("a-leaf")));
        assertEquals(path, CataloguePath.parse(path.toUrl()));
        assertEquals(new CataloguePath(List.of()), CataloguePath.parse("/cat"));
        assertEquals(new CataloguePath(List.of()), CataloguePath.parse("/cat/"));
    }
}
