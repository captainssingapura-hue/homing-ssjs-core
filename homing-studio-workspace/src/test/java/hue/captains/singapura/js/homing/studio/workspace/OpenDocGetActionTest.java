package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.StudioProxy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** RFC 0040 — leveled-path resolution (the /open path walk), including OfStudio descent. */
class OpenDocGetActionTest {

    private static final UUID DOC_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final String DOC_URL = "/app?app=doc-reader&doc=" + DOC_UUID;

    private record TestDoc() implements Doc {
        @Override public UUID uuid()      { return DOC_UUID; }
        @Override public String title()   { return "Alpha Doc"; }
        @Override public String contents(){ return "# Alpha"; }
    }

    // Root(L0) → Child(L1) → doc
    private record Child() implements L1_Catalogue<Root, Child> {
        static final Child INSTANCE = new Child();
        @Override public Root parent() { return Root.INSTANCE; }
        @Override public String name() { return "Child"; }
        @Override public List<Entry<Child>> leaves() { return List.of(Entry.of(this, new TestDoc())); }
    }
    private record Root() implements L0_Catalogue<Root> {
        static final Root INSTANCE = new Root();
        @Override public String name() { return "Root"; }
        @Override public List<? extends L1_Catalogue<Root, ?>> subCatalogues() { return List.of(Child.INSTANCE); }
    }

    // Source studio: Source(L0) → SourceChild(L1) → doc
    private record SourceChild() implements L1_Catalogue<Source, SourceChild> {
        static final SourceChild INSTANCE = new SourceChild();
        @Override public Source parent() { return Source.INSTANCE; }
        @Override public String name()   { return "Source Child"; }
        @Override public List<Entry<SourceChild>> leaves() { return List.of(Entry.of(this, new TestDoc())); }
    }
    private record Source() implements L0_Catalogue<Source> {
        static final Source INSTANCE = new Source();
        @Override public String name() { return "Source"; }
        @Override public List<? extends L1_Catalogue<Source, ?>> subCatalogues() { return List.of(SourceChild.INSTANCE); }
    }
    // Forest(L0) whose only leaf is an OfStudio portal to Source
    private record Forest() implements L0_Catalogue<Forest> {
        static final Forest INSTANCE = new Forest();
        @Override public String name() { return "Forest"; }
        @Override public List<Entry<Forest>> leaves() {
            return List.of(Entry.of(this, new StudioProxy<>(Source.INSTANCE, "Proxied", "", "STUDIO", "")));
        }
    }

    @Test
    void resolvesPlainPath() {
        // Root[0]=Child, Child[0]=doc  → [0,0]
        assertEquals(DOC_URL, OpenDocGetAction.resolve(Root.INSTANCE, List.of(0, 0)));
    }

    @Test
    void resolvesThroughOfStudioPortal() {
        // Forest[0]=portal→Source, Source[0]=SourceChild, SourceChild[0]=doc → [0,0,0]
        assertEquals(DOC_URL, OpenDocGetAction.resolve(Forest.INSTANCE, List.of(0, 0, 0)));
    }

    @Test
    void branchPathAndOutOfRangeYieldNull() {
        assertNull(OpenDocGetAction.resolve(Root.INSTANCE, List.of(0)),    "path ends on a branch");
        assertNull(OpenDocGetAction.resolve(Root.INSTANCE, List.of(5)),    "index out of range");
        assertNull(OpenDocGetAction.resolve(Root.INSTANCE, List.of()),     "empty path");
    }
}
