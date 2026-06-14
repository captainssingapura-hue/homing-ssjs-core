package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.StudioProxy;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogueNormalizerTest {

    private static final UUID DOC_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private record TestDoc() implements Doc {
        @Override public UUID uuid()      { return DOC_UUID; }
        @Override public String title()   { return "Alpha Doc"; }
        @Override public String summary() { return "An alpha summary."; }
        @Override public String category(){ return "RFC"; }
        @Override public String kind()    { return "composed"; }
        @Override public String contents(){ return "# Alpha"; }
    }

    // ── A plain catalogue tree: Root(L0) → Child(L1) → doc leaf(L2) ──────────

    private record Child() implements L1_Catalogue<Root, Child> {
        static final Child INSTANCE = new Child();
        @Override public Root parent()    { return Root.INSTANCE; }
        @Override public String name()    { return "Child Cat"; }
        @Override public String summary() { return "Child summary."; }
        @Override public List<Entry<Child>> leaves() {
            return List.of(Entry.of(this, new TestDoc()));
        }
    }
    private record Root() implements L0_Catalogue<Root> {
        static final Root INSTANCE = new Root();
        @Override public String name()    { return "Root Cat"; }
        @Override public String summary() { return "Root summary."; }
        @Override public List<? extends L1_Catalogue<Root, ?>> subCatalogues() {
            return List.of(Child.INSTANCE);
        }
    }

    // ── A contributor studio (its own L0 tree) to be grafted via OfStudio ───
    //   Source(L0) → SourceChild(L1) → doc(L2)

    private record SourceChild() implements L1_Catalogue<Source, SourceChild> {
        static final SourceChild INSTANCE = new SourceChild();
        @Override public Source parent()  { return Source.INSTANCE; }
        @Override public String name()    { return "Source Child"; }
        @Override public List<Entry<SourceChild>> leaves() {
            return List.of(Entry.of(this, new TestDoc()));
        }
    }
    private record Source() implements L0_Catalogue<Source> {
        static final Source INSTANCE = new Source();
        @Override public String name()    { return "Source Studio"; }
        @Override public List<? extends L1_Catalogue<Source, ?>> subCatalogues() {
            return List.of(SourceChild.INSTANCE);
        }
    }

    // ── A host whose ONLY leaf is an OfStudio portal to Source ──────────────
    private record ForestHost() implements L0_Catalogue<ForestHost> {
        static final ForestHost INSTANCE = new ForestHost();
        @Override public String name() { return "Forest"; }
        @Override public List<Entry<ForestHost>> leaves() {
            return List.of(Entry.of(this,
                    new StudioProxy<>(Source.INSTANCE, "Proxied Studio", "", "STUDIO", "🌐")));
        }
    }

    @Test
    void normalizesLevelsAndDimensions() {
        NormalizedNode root = CatalogueNormalizer.INSTANCE.normalize(Root.INSTANCE);
        assertEquals(TreeLevel.L0.INSTANCE, root.level());
        assertEquals(1, root.children().size());

        NormalizedNode child = root.children().get(0);
        assertEquals(TreeLevel.L1.INSTANCE, child.level());

        NormalizedNode leaf = child.children().get(0);
        assertEquals(TreeLevel.L2.INSTANCE, leaf.level());
        assertTrue(leaf.children().isEmpty());
    }

    @Test
    void graftsOfStudioPortalWithShiftedLevels() {
        // ForestHost(L0) → [ grafted Source(L1) → SourceChild(L2) → doc(L3) ]
        NormalizedNode host = CatalogueNormalizer.INSTANCE.normalize(ForestHost.INSTANCE);
        assertEquals(TreeLevel.L0.INSTANCE, host.level());
        assertEquals(1, host.children().size(), "the OfStudio portal grafts in one node");

        NormalizedNode graftedSource = host.children().get(0);
        assertEquals(TreeLevel.L1.INSTANCE, graftedSource.level(), "source root lands one below host");

        NormalizedNode sourceChild = graftedSource.children().get(0);
        assertEquals(TreeLevel.L2.INSTANCE, sourceChild.level(), "descendants shift by the same delta");

        NormalizedNode doc = sourceChild.children().get(0);
        assertEquals(TreeLevel.L3.INSTANCE, doc.level());
    }

    @Test
    void serialisesViaTheUnchangedWriter() {
        NormalizedNode host = CatalogueNormalizer.INSTANCE.normalize(ForestHost.INSTANCE);
        String json = new TreeNodeJsonWriter().write(host);
        assertTrue(json.contains("\"level\":\"L0\""), json);
        assertTrue(json.contains("\"level\":\"L1\""), json);   // the grafted source root
        assertTrue(json.contains("\"level\":\"L3\""), json);   // the grafted doc, shifted down
    }
}
