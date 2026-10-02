package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Placed;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.tree.NodeName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trees compose by grafting. Two apps' trees - notes and recipes - are each
 * written alone, knowing no site; a site's root grafts both, the recipes
 * restated as its kitchen. Every vertex and page then has one path in the
 * composed tree - the authentic path of this site - and the trail is read
 * off it; the same tree alone has paths of its own. What composing may not
 * do is refused: a tree grafted twice or into itself, a slug that names two
 * things, a page placed by both trees, a vertex deeper than eight.
 */
class GraftTest {

    /** A page that says its trail, so the router's telling can be seen. */
    record Page(String title) implements Placed {
        @Override public HtmlPageContent html(Trail trail, Query q) {
            var crumbs = new StringBuilder();
            for (var c : trail.crumbs()) crumbs.append(c.text()).append('>').append(c.href()).append(' ');
            return new HtmlPageContent(title + "|" + crumbs.toString().trim());
        }
    }

    static final Page WELCOME = new Page("welcome"), MONDAY = new Page("monday"), LAKSA = new Page("laksa"), HOME = new Page("home");

    // ── the notes app's tree, written alone ─────────────────────────────────
    record NotesCatalogue() implements L0_Catalogue<NotesCatalogue> {
        static final NotesCatalogue INSTANCE = new NotesCatalogue();
        @Override public String name() { return "Notes"; }
        @Override public List<? extends L1_Catalogue<NotesCatalogue, ?>> subCatalogues() { return List.of(JournalCatalogue.INSTANCE); }
        @Override public List<Leaf<NotesCatalogue>> leaves() { return List.of(Leaf.of(this, "Welcome", "", WELCOME)); }
    }
    record JournalCatalogue() implements L1_Catalogue<NotesCatalogue, JournalCatalogue> {
        static final JournalCatalogue INSTANCE = new JournalCatalogue();
        @Override public NotesCatalogue parent() { return NotesCatalogue.INSTANCE; }
        @Override public String name() { return "Journal"; }
        @Override public List<Leaf<JournalCatalogue>> leaves() { return List.of(Leaf.of(this, "Monday", "", MONDAY)); }
    }

    // ── the recipes app's tree, written alone ───────────────────────────────
    record RecipesCatalogue() implements L0_Catalogue<RecipesCatalogue> {
        static final RecipesCatalogue INSTANCE = new RecipesCatalogue();
        @Override public String name() { return "Recipes"; }
        @Override public List<? extends L1_Catalogue<RecipesCatalogue, ?>> subCatalogues() { return List.of(SoupsCatalogue.INSTANCE); }
    }
    record SoupsCatalogue() implements L1_Catalogue<RecipesCatalogue, SoupsCatalogue> {
        static final SoupsCatalogue INSTANCE = new SoupsCatalogue();
        @Override public RecipesCatalogue parent() { return RecipesCatalogue.INSTANCE; }
        @Override public String name() { return "Soups"; }
        @Override public List<Leaf<SoupsCatalogue>> leaves() { return List.of(Leaf.of(this, "Laksa", "", LAKSA)); }
    }

    // ── the site's root: its own page, and both trees grafted ───────────────
    record DeskCatalogue() implements L0_Catalogue<DeskCatalogue> {
        static final DeskCatalogue INSTANCE = new DeskCatalogue();
        @Override public String name() { return "Desk"; }
        @Override public List<Graft<DeskCatalogue>> grafts() {
            return List.of(Graft.of(this, NotesCatalogue.INSTANCE),
                           Graft.of(this, RecipesCatalogue.INSTANCE).shownAs("Kitchen", "What we cook").icon("🍜"));
        }
        @Override public List<Leaf<DeskCatalogue>> leaves() { return List.of(Leaf.of(this, "Home", "", HOME)); }
    }

    static final CatalogueTree TREE = CatalogueTree.of(DeskCatalogue.INSTANCE);

    @Test
    void aGraftedTreesVerticesAndPagesTakeTheirPathsUnderTheHost() {
        assertEquals(Path.of("notes"), TREE.pathOf(NotesCatalogue.INSTANCE));
        assertEquals(Path.of("notes", "journal"), TREE.pathOf(JournalCatalogue.INSTANCE));
        assertEquals(Optional.of(Path.of("notes", "journal", "monday")), TREE.pathOf(MONDAY));
        assertEquals(Optional.of(Path.of("recipes", "soups", "laksa")), TREE.pathOf(LAKSA));
        assertEquals(Optional.of(Path.of("home")), TREE.pathOf(HOME));
        var hit = assertInstanceOf(Resolution.AtLeaf.class, TREE.resolve(Path.of("recipes", "soups", "laksa")));
        assertSame(LAKSA, hit.leaf().page());
        assertSame(SoupsCatalogue.INSTANCE, hit.parent());
    }

    @Test
    void theSameTreeAlone_hasPathsOfItsOwn() {
        var alone = CatalogueTree.of(NotesCatalogue.INSTANCE);
        assertEquals(Path.ROOT, alone.pathOf(NotesCatalogue.INSTANCE));
        assertEquals(Optional.of(Path.of("journal", "monday")), alone.pathOf(MONDAY));
    }

    @Test
    void aGraftedRootsParentIsItsHost_itsLevelItsOwn_itsDepthTheSites() {
        assertEquals(Optional.of(DeskCatalogue.INSTANCE), TREE.parentOf(NotesCatalogue.INSTANCE));
        assertEquals(List.of(DeskCatalogue.INSTANCE, NotesCatalogue.INSTANCE, JournalCatalogue.INSTANCE), TREE.lineageOf(JournalCatalogue.INSTANCE));
        assertEquals(0, CatalogueTree.levelOf(NotesCatalogue.INSTANCE));
        assertEquals(1, TREE.depthOf(NotesCatalogue.INSTANCE));
        assertEquals(2, TREE.depthOf(JournalCatalogue.INSTANCE));
        assertTrue(TREE.graftOf(RecipesCatalogue.INSTANCE).isPresent());
        assertTrue(TREE.graftOf(SoupsCatalogue.INSTANCE).isEmpty(), "only a grafted root has a graft");
        assertEquals(List.of(NotesCatalogue.INSTANCE, RecipesCatalogue.INSTANCE), TREE.childrenOf(DeskCatalogue.INSTANCE));
    }

    @Test
    void aGraftedRootIsShownAsItsGraftSays_everyOtherVertexAsItself() {
        assertEquals(new CatalogueTree.Shown("Kitchen", "What we cook", "CATALOGUE", "🍜"), TREE.shownAs(RecipesCatalogue.INSTANCE));
        assertEquals("Notes", TREE.shownAs(NotesCatalogue.INSTANCE).name());
        assertEquals("Soups", TREE.shownAs(SoupsCatalogue.INSTANCE).name());
    }

    @Test
    void theRouterServesTheComposedTree_theTrailReadOffThePath() {
        var router = CatalogueRouter.at(Path.ROOT, TREE);
        assertEquals("laksa|Desk>/ Kitchen>/recipes Soups>/recipes/soups Laksa>/recipes/soups/laksa",
                router.resolve(Path.of("recipes", "soups", "laksa")).orElseThrow().html(Query.NONE).body());
        assertEquals(Optional.of("/notes/journal/monday"), router.hrefOf(MONDAY));
        String listing = router.resolve(Path.ROOT).orElseThrow().html(Query.NONE).body();
        assertTrue(listing.contains("href=\"/notes\"") && listing.contains("href=\"/recipes\""), listing);
        assertTrue(listing.contains("Kitchen") && listing.contains("What we cook"), "the graft's restatement: " + listing);
        String kitchen = router.resolve(Path.of("recipes")).orElseThrow().html(Query.NONE).body();
        assertTrue(kitchen.contains("<title>Kitchen</title>"), kitchen);
    }

    // ── what composing may not do ───────────────────────────────────────────

    record TwiceGraftedRoot() implements L0_Catalogue<TwiceGraftedRoot> {
        @Override public String name() { return "Twice"; }
        @Override public List<? extends L1_Catalogue<TwiceGraftedRoot, ?>> subCatalogues() { return List.of(new Shelf()); }
        @Override public List<Graft<TwiceGraftedRoot>> grafts() { return List.of(Graft.of(this, NotesCatalogue.INSTANCE)); }
    }
    record Shelf() implements L1_Catalogue<TwiceGraftedRoot, Shelf> {
        @Override public TwiceGraftedRoot parent() { return new TwiceGraftedRoot(); }
        @Override public String name() { return "Shelf"; }
        @Override public List<Graft<Shelf>> grafts() { return List.of(Graft.of(this, NotesCatalogue.INSTANCE)); }
    }

    @Test
    void aTreeGraftedTwiceIsRefused() {
        var e = assertThrows(IllegalArgumentException.class, () -> CatalogueTree.of(new TwiceGraftedRoot()));
        assertTrue(e.getMessage().contains("grafted twice"), e.getMessage());
    }

    record Ouroboros() implements L0_Catalogue<Ouroboros> {
        @Override public String name() { return "Ouroboros"; }
        @Override public List<Graft<Ouroboros>> grafts() { return List.of(Graft.of(this, new Tail())); }
    }
    record Tail() implements L0_Catalogue<Tail> {
        @Override public String name() { return "Tail"; }
        @Override public List<Graft<Tail>> grafts() { return List.of(Graft.of(this, new Ouroboros())); }
    }

    @Test
    void aTreeGraftedIntoItselfIsRefused() {
        var e = assertThrows(IllegalArgumentException.class, () -> CatalogueTree.of(new Ouroboros()));
        assertTrue(e.getMessage().contains("into itself"), e.getMessage());
    }

    record ClashRoot() implements L0_Catalogue<ClashRoot> {
        @Override public String name() { return "Clash"; }
        @Override public List<Graft<ClashRoot>> grafts() { return List.of(Graft.of(this, NotesCatalogue.INSTANCE)); }
        @Override public List<Leaf<ClashRoot>> leaves() { return List.of(Leaf.of(this, NodeName.slug("notes"), "My notes", "", HOME)); }
    }

    @Test
    void aGraftWhoseSlugNamesASiblingIsRefused() {
        var e = assertThrows(IllegalArgumentException.class, () -> CatalogueTree.of(new ClashRoot()));
        assertTrue(e.getMessage().contains("Slug 'notes'"), e.getMessage());
    }

    record Borrower() implements L0_Catalogue<Borrower> {
        @Override public String name() { return "Borrower"; }
        @Override public List<Leaf<Borrower>> leaves() { return List.of(Leaf.of(this, "Laksa again", "", LAKSA)); }
    }
    record SharingRoot() implements L0_Catalogue<SharingRoot> {
        @Override public String name() { return "Sharing"; }
        @Override public List<Graft<SharingRoot>> grafts() {
            return List.of(Graft.of(this, RecipesCatalogue.INSTANCE), Graft.of(this, new Borrower()));
        }
    }

    @Test
    void aPagePlacedByTwoTreesIsRefused_wherever_it_is_placed() {
        var e = assertThrows(IllegalArgumentException.class, () -> CatalogueTree.of(new SharingRoot()));
        assertTrue(e.getMessage().contains("placed twice"), e.getMessage());
    }

    // a chain of roots, each grafting the next: the tenth sits at depth 9
    record G0() implements L0_Catalogue<G0> { public String name() { return "g0"; } public List<Graft<G0>> grafts() { return List.of(Graft.of(this, new G1())); } }
    record G1() implements L0_Catalogue<G1> { public String name() { return "g1"; } public List<Graft<G1>> grafts() { return List.of(Graft.of(this, new G2())); } }
    record G2() implements L0_Catalogue<G2> { public String name() { return "g2"; } public List<Graft<G2>> grafts() { return List.of(Graft.of(this, new G3())); } }
    record G3() implements L0_Catalogue<G3> { public String name() { return "g3"; } public List<Graft<G3>> grafts() { return List.of(Graft.of(this, new G4())); } }
    record G4() implements L0_Catalogue<G4> { public String name() { return "g4"; } public List<Graft<G4>> grafts() { return List.of(Graft.of(this, new G5())); } }
    record G5() implements L0_Catalogue<G5> { public String name() { return "g5"; } public List<Graft<G5>> grafts() { return List.of(Graft.of(this, new G6())); } }
    record G6() implements L0_Catalogue<G6> { public String name() { return "g6"; } public List<Graft<G6>> grafts() { return List.of(Graft.of(this, new G7())); } }
    record G7() implements L0_Catalogue<G7> { public String name() { return "g7"; } public List<Graft<G7>> grafts() { return List.of(Graft.of(this, new G8())); } }
    record G8() implements L0_Catalogue<G8> { public String name() { return "g8"; } public List<Graft<G8>> grafts() { return List.of(Graft.of(this, new G9())); } }
    record G9() implements L0_Catalogue<G9> { public String name() { return "g9"; } }

    @Test
    void composingDeeperThanEightIsRefused() {
        assertEquals(8, CatalogueTree.of(new G1()).depthOf(new G9()), "nine roots deep is the limit, and allowed");
        var e = assertThrows(IllegalArgumentException.class, () -> CatalogueTree.of(new G0()));
        assertTrue(e.getMessage().contains("depth 9"), e.getMessage());
    }
}
