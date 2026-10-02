package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.Placed;
import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.List;

/**
 * A three-level tree — root, two level-1s, one level-2 — with pages at
 * every level, and the broken trees each check refuses.
 */
final class Fixtures {

    private Fixtures() {}

    /** A page that shows its trail when it has one. */
    record Page(String title) implements Placed {
        @Override public HtmlPageContent html(Trail trail, hue.captains.singapura.js.homing.site.Query q) {
            return new HtmlPageContent(title + "|" + trail.depth() + "|" + q.first("v").orElse(""));
        }
    }

    static final Page      P_ABOUT = new Page("about");
    static final Page      P_ONE   = new Page("one");
    static final Page      P_TWO   = new Page("two");
    static final Navigable P_PLAIN = q -> new HtmlPageContent("plain");

    // ── The good tree ─────────────────────────────────────────────────────────

    record RootCatalogue() implements L0_Catalogue<RootCatalogue> {
        static final RootCatalogue INSTANCE = new RootCatalogue();
        @Override public String name() { return "Root"; }
        @Override public List<? extends L1_Catalogue<RootCatalogue, ?>> subCatalogues() {
            return List.of(AlphaCatalogue.INSTANCE, BetaCatalogue.INSTANCE);
        }
        @Override public List<Leaf<RootCatalogue>> leaves() {
            return List.of(Leaf.of(this, "About", "What this is", P_ABOUT));
        }
    }

    record AlphaCatalogue() implements L1_Catalogue<RootCatalogue, AlphaCatalogue> {
        static final AlphaCatalogue INSTANCE = new AlphaCatalogue();
        @Override public RootCatalogue parent() { return RootCatalogue.INSTANCE; }
        @Override public String name() { return "Alpha"; }
        @Override public List<? extends L2_Catalogue<AlphaCatalogue, ?>> subCatalogues() {
            return List.of(DeepCatalogue.INSTANCE);
        }
        @Override public List<Leaf<AlphaCatalogue>> leaves() {
            return List.of(Leaf.of(this, NodeName.slug("one"), "One", "", P_ONE),
                           Leaf.of(this, "Plain page", "", P_PLAIN));
        }
    }

    record BetaCatalogue() implements L1_Catalogue<RootCatalogue, BetaCatalogue> {
        static final BetaCatalogue INSTANCE = new BetaCatalogue();
        @Override public RootCatalogue parent() { return RootCatalogue.INSTANCE; }
        @Override public String name() { return "Beta"; }
        @Override public NodeName slug() { return new NodeName("b"); }
    }

    record DeepCatalogue() implements L2_Catalogue<AlphaCatalogue, DeepCatalogue> {
        static final DeepCatalogue INSTANCE = new DeepCatalogue();
        @Override public AlphaCatalogue parent() { return AlphaCatalogue.INSTANCE; }
        @Override public String name() { return "Deep"; }
        @Override public List<Leaf<DeepCatalogue>> leaves() {
            return List.of(Leaf.of(this, "Two", "", P_TWO));
        }
    }

    // ── The broken ones ───────────────────────────────────────────────────────

    /** Lists a child whose parent() names someone else. */
    record DisagreeRoot() implements L0_Catalogue<DisagreeRoot> {
        @Override public String name() { return "Disagree"; }
        @Override public List<? extends L1_Catalogue<DisagreeRoot, ?>> subCatalogues() {
            return List.of(new Stray());
        }
    }
    record Stray() implements L1_Catalogue<DisagreeRoot, Stray> {
        @Override public DisagreeRoot parent() { return null; }
        @Override public String name() { return "Stray"; }
    }

    /** Two leaves with one slug. */
    record SlugClashRoot() implements L0_Catalogue<SlugClashRoot> {
        @Override public String name() { return "Clash"; }
        @Override public List<Leaf<SlugClashRoot>> leaves() {
            return List.of(Leaf.of(this, NodeName.slug("x"), "X one", "", P_ONE),
                           Leaf.of(this, NodeName.slug("x"), "X two", "", P_TWO));
        }
    }

    /** A sub-catalogue and a leaf with one slug. */
    record MixedClashRoot() implements L0_Catalogue<MixedClashRoot> {
        @Override public String name() { return "Mixed"; }
        @Override public List<? extends L1_Catalogue<MixedClashRoot, ?>> subCatalogues() {
            return List.of(new Shadowed());
        }
        @Override public List<Leaf<MixedClashRoot>> leaves() {
            return List.of(Leaf.of(this, NodeName.slug("shadowed"), "Shadowed page", "", P_ONE));
        }
    }
    record Shadowed() implements L1_Catalogue<MixedClashRoot, Shadowed> {
        @Override public MixedClashRoot parent() { return new MixedClashRoot(); }
        @Override public String name() { return "Shadowed"; }
    }

    /** One navigable placed twice. */
    record TwicePlacedRoot() implements L0_Catalogue<TwicePlacedRoot> {
        @Override public String name() { return "Twice"; }
        @Override public List<Leaf<TwicePlacedRoot>> leaves() {
            return List.of(Leaf.of(this, "Here", "", P_ONE), Leaf.of(this, "And here", "", P_ONE));
        }
    }
}
