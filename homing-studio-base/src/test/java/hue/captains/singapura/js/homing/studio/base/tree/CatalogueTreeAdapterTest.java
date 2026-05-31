package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogueTreeAdapterTest {

    private static final UUID DOC_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    /** Minimal ComposedDoc-kind leaf. */
    private record TestDoc() implements Doc {
        @Override public UUID uuid()      { return DOC_UUID; }
        @Override public String title()   { return "Alpha Doc"; }
        @Override public String summary() { return "An alpha summary."; }
        @Override public String category(){ return "RFC"; }
        @Override public String kind()    { return "composed"; }
        @Override public String contents(){ return "# Alpha"; }
    }

    /** L1 child catalogue with one doc leaf. */
    private record Child() implements L1_Catalogue<Root, Child> {
        static final Child INSTANCE = new Child();
        @Override public Root parent()    { return Root.INSTANCE; }
        @Override public String name()    { return "Child Cat"; }
        @Override public String summary() { return "Child summary."; }
        @Override public List<Entry<Child>> leaves() {
            return List.of(Entry.of(this, new TestDoc()));
        }
    }

    /** L0 root with one sub-catalogue. */
    private record Root() implements L0_Catalogue<Root> {
        static final Root INSTANCE = new Root();
        @Override public String name()    { return "Root Cat"; }
        @Override public String summary() { return "Root summary."; }
        @Override public List<? extends L1_Catalogue<Root, ?>> subCatalogues() {
            return List.of(Child.INSTANCE);
        }
    }

    @Test
    void adaptsLevelsAndDimensions() {
        CatalogueTreeNode root = CatalogueTreeAdapter.INSTANCE.adapt(Root.INSTANCE);

        assertEquals(TreeLevel.L0.INSTANCE, root.level());
        assertEquals(1, root.kids().size());

        CatalogueTreeNode child = root.kids().get(0);
        assertEquals(TreeLevel.L1.INSTANCE, child.level());
        assertEquals(1, child.kids().size());

        CatalogueTreeNode leaf = child.kids().get(0);
        assertEquals(TreeLevel.L2.INSTANCE, leaf.level());
        assertEquals(DOC_UUID.toString(), leaf.id());
        assertTrue(leaf.kids().isEmpty());
    }

    @Test
    void serialisesToSelfDescribingJson() {
        CatalogueTreeNode root = CatalogueTreeAdapter.INSTANCE.adapt(Root.INSTANCE);
        String json = new TreeNodeJsonWriter().write(root);

        // Branch carries Kind=catalogue + its Summary inline.
        assertTrue(json.contains("\"level\":\"L0\""), json);
        assertTrue(json.contains("\"key\":\"kind\",\"valueTag\":\"kind\",\"text\":\"catalogue\""), json);
        assertTrue(json.contains("\"text\":\"Root summary.\""), json);
        // Leaf carries doc kind + title.
        assertTrue(json.contains("\"text\":\"composed\""), json);
        assertTrue(json.contains("\"text\":\"Alpha Doc\""), json);
        assertTrue(json.contains("\"id\":\"" + DOC_UUID + "\""), json);
    }
}
