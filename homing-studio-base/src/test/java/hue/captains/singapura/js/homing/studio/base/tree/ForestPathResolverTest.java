package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.StudioProxy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestPathResolverTest {

    private static final UUID DOC_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private record TestDoc() implements Doc {
        @Override public UUID uuid()      { return DOC_UUID; }
        @Override public String title()   { return "Alpha Doc"; }
        @Override public String summary() { return "An alpha summary."; }
        @Override public String category(){ return "RFC"; }
        @Override public String kind()    { return "composed"; }
        @Override public String contents(){ return "# Alpha"; }
    }

    // Root(L0) → Child(L1) → doc(L2)
    private record Child() implements L1_Catalogue<Root, Child> {
        static final Child INSTANCE = new Child();
        @Override public Root parent()    { return Root.INSTANCE; }
        @Override public String name()    { return "Child Cat"; }
        @Override public List<Entry<Child>> leaves() {
            return List.of(Entry.of(this, new TestDoc()));
        }
    }
    private record Root() implements L0_Catalogue<Root> {
        static final Root INSTANCE = new Root();
        @Override public String name()    { return "Root Cat"; }
        @Override public List<? extends L1_Catalogue<Root, ?>> subCatalogues() {
            return List.of(Child.INSTANCE);
        }
    }

    // A contributor studio reached via an OfStudio portal: Source(L0) → doc
    private record Source() implements L0_Catalogue<Source> {
        static final Source INSTANCE = new Source();
        @Override public String name() { return "Source Studio"; }
        @Override public List<Entry<Source>> leaves() {
            return List.of(Entry.of(this, new TestDoc()));
        }
    }
    private record ForestHost() implements L0_Catalogue<ForestHost> {
        static final ForestHost INSTANCE = new ForestHost();
        @Override public String name() { return "Forest"; }
        @Override public List<Entry<ForestHost>> leaves() {
            return List.of(Entry.of(this,
                    new StudioProxy<>(Source.INSTANCE, "Proxied", "", "STUDIO", "🌐")));
        }
    }

    @Test
    void resolvesAPlainDocPathWithTrail() {
        // Root → subCatalogue[0]=Child → leaf[0]=doc.  Child is index 0 of
        // Root's nav children (sub-catalogues first); doc is index 0 of Child.
        Optional<ForestPathResolver.Resolved> r =
                ForestPathResolver.INSTANCE.resolve(Root.INSTANCE, List.of(0, 0));
        assertTrue(r.isPresent());
        assertEquals(DOC_UUID, r.get().doc().uuid());
        // trail crumbs carry the catalogue label + a clickable browse URL.
        assertEquals(List.of("Root Cat", "Child Cat"),
                r.get().trail().stream().map(c -> c.text()).toList());
        assertTrue(r.get().trail().get(0).href().startsWith("/app?app=catalogue&id="));
    }

    @Test
    void descendsOfStudioPortal() {
        // ForestHost → portal[0]=Source(L0) → leaf[0]=doc.
        Optional<ForestPathResolver.Resolved> r =
                ForestPathResolver.INSTANCE.resolve(ForestHost.INSTANCE, List.of(0, 0));
        assertTrue(r.isPresent());
        assertEquals(DOC_UUID, r.get().doc().uuid());
        assertEquals(List.of("Forest", "Source Studio"),
                r.get().trail().stream().map(c -> c.text()).toList());
    }

    @Test
    void outOfRangeOrBranchPathIsEmpty() {
        assertTrue(ForestPathResolver.INSTANCE.resolve(Root.INSTANCE, List.of(5)).isEmpty());
        assertTrue(ForestPathResolver.INSTANCE.resolve(Root.INSTANCE, List.of(0)).isEmpty());   // lands on a catalogue
        assertTrue(ForestPathResolver.INSTANCE.resolve(Root.INSTANCE, List.of()).isEmpty());
    }
}
