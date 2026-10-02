package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.Path;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static hue.captains.singapura.js.homing.site.catalogue.Fixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogueTreeTest {

    private static final CatalogueTree TREE = CatalogueTree.of(RootCatalogue.INSTANCE);

    @Test
    void readsPreOrderWithParents() {
        assertEquals(List.of(RootCatalogue.INSTANCE, AlphaCatalogue.INSTANCE, DeepCatalogue.INSTANCE, BetaCatalogue.INSTANCE),
                     TREE.all());
        assertEquals(Optional.empty(), TREE.parentOf(RootCatalogue.INSTANCE));
        assertEquals(Optional.of(AlphaCatalogue.INSTANCE), TREE.parentOf(DeepCatalogue.INSTANCE));
        assertEquals(List.of(RootCatalogue.INSTANCE, AlphaCatalogue.INSTANCE, DeepCatalogue.INSTANCE),
                     TREE.lineageOf(DeepCatalogue.INSTANCE));
    }

    @Test
    void everyVertexAndPageHasOnePath() {
        assertEquals(Path.ROOT,             TREE.pathOf(RootCatalogue.INSTANCE));
        assertEquals(Path.of("alpha"),      TREE.pathOf(AlphaCatalogue.INSTANCE));
        assertEquals(Path.of("alpha", "deep"), TREE.pathOf(DeepCatalogue.INSTANCE));
        assertEquals(Path.of("b"),          TREE.pathOf(BetaCatalogue.INSTANCE));   // slug overridden
        assertEquals(Optional.of(Path.of("about")),             TREE.pathOf(P_ABOUT));
        assertEquals(Optional.of(Path.of("alpha", "one")),      TREE.pathOf(P_ONE));
        assertEquals(Optional.of(Path.of("alpha", "plain-page")), TREE.pathOf(P_PLAIN));
        assertEquals(Optional.of(Path.of("alpha", "deep", "two")), TREE.pathOf(P_TWO));
        assertEquals(Optional.empty(), TREE.pathOf(q -> null));
    }

    @Test
    void resolvesWhatItMinted() {
        for (Catalogue<?> c : TREE.all()) {
            var r = assertInstanceOf(Resolution.AtCatalogue.class, TREE.resolve(TREE.pathOf(c)));
            assertSame(c, r.catalogue());
        }
        var leaf = assertInstanceOf(Resolution.AtLeaf.class, TREE.resolve(Path.of("alpha", "deep", "two")));
        assertSame(DeepCatalogue.INSTANCE, leaf.parent());
        assertSame(P_TWO, leaf.leaf().page());
    }

    @Test
    void missesSayWhere() {
        var m1 = assertInstanceOf(Resolution.Miss.class, TREE.resolve(Path.of("alpha", "nope")));
        assertEquals(1, m1.failedAt());
        assertEquals("nope", m1.at());
        assertEquals(Resolution.Reason.NO_SUCH_CHILD, m1.reason());

        var m2 = assertInstanceOf(Resolution.Miss.class, TREE.resolve(Path.of("about", "more")));
        assertEquals(1, m2.failedAt());
        assertEquals(Resolution.Reason.PAST_A_LEAF, m2.reason());
        assertTrue(!m2.isHit());
    }

    @Test
    void levels() {
        assertEquals(0, CatalogueTree.levelOf(RootCatalogue.INSTANCE));
        assertEquals(1, CatalogueTree.levelOf(BetaCatalogue.INSTANCE));
        assertEquals(2, CatalogueTree.levelOf(DeepCatalogue.INSTANCE));
    }

    @Test
    void refusesADisagreeingEdge() {
        var ex = assertThrows(IllegalArgumentException.class, () -> CatalogueTree.of(new DisagreeRoot()));
        assertTrue(ex.getMessage().contains("must agree"), ex.getMessage());
    }

    @Test
    void refusesTwoLeavesWithOneSlug() {
        var ex = assertThrows(IllegalArgumentException.class, () -> CatalogueTree.of(new SlugClashRoot()));
        assertTrue(ex.getMessage().contains("Slug 'x'"), ex.getMessage());
    }

    @Test
    void refusesACatalogueAndALeafWithOneSlug() {
        var ex = assertThrows(IllegalArgumentException.class, () -> CatalogueTree.of(new MixedClashRoot()));
        assertTrue(ex.getMessage().contains("Slug 'shadowed'"), ex.getMessage());
    }

    @Test
    void refusesOnePagePlacedTwice() {
        var ex = assertThrows(IllegalArgumentException.class, () -> CatalogueTree.of(new TwicePlacedRoot()));
        assertTrue(ex.getMessage().contains("placed twice"), ex.getMessage());
    }

    @Test
    void refusesAStranger() {
        assertThrows(IllegalArgumentException.class, () -> TREE.pathOf(new SlugClashRoot()));
    }
}
