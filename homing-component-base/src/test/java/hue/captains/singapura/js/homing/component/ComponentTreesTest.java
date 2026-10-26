package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A vehicle's catalogue declared as records, composed by derivation from a
 * crate closure, and held to its modules: what the guard refuses.
 */
class ComponentTreesTest {

    // ── a module that exports two components and a statics constant ──────
    public record BoxModule() implements DomModule<BoxModule> {
        public static final BoxModule INSTANCE = new BoxModule();
        public record Box() implements BranchComponent<BoxModule> {
            @Override public String summary() { return "A box on a branch."; }
        }
        public record Knob() implements ElementComponent<BoxModule> {
            @Override public String tag() { return "button"; }
        }
        public record BoxEvents() implements Exportable._Constant<BoxModule> {}
        @Override public ImportsFor<BoxModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<BoxModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Box(), new Knob(), new BoxEvents())); }
    }

    // ── the vehicle's catalogue: root → a family → the knob; the box at the root ──
    public record BoxComponents() implements C0_Components<BoxComponents> {
        public static final BoxComponents INSTANCE = new BoxComponents();
        @Override public String name() { return "Boxes"; }
        @Override public List<? extends C1_Components<BoxComponents, ?>> subCatalogues() { return List.of(ControlsComponents.INSTANCE); }
        @Override public List<ComponentEntry<BoxComponents>> leaves() { return List.of(ComponentEntry.of(this, new BoxModule.Box())); }
    }
    public record ControlsComponents() implements C1_Components<BoxComponents, ControlsComponents> {
        public static final ControlsComponents INSTANCE = new ControlsComponents();
        @Override public BoxComponents parent() { return BoxComponents.INSTANCE; }
        @Override public String name() { return "Controls"; }
        @Override public List<ComponentEntry<ControlsComponents>> leaves() { return List.of(ComponentEntry.of(this, new BoxModule.Knob())); }
    }
    static final class BoxCrate implements Crate, ComponentVehicle {
        static final BoxCrate INSTANCE = new BoxCrate();
        @Override public String name() { return "boxes"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(BoxModule.INSTANCE)); }
        @Override public C0_Components<?> components() { return BoxComponents.INSTANCE; }
    }
    /** A site crate that ships nothing of its own and requires the vehicle. */
    static final class SiteCrate implements Crate {
        static final SiteCrate INSTANCE = new SiteCrate();
        @Override public String name() { return "site"; }
        @Override public List<CrateEntry> entries() { return List.of(); }
        @Override public List<Crate> requires() { return List.of(BoxCrate.INSTANCE); }
    }

    @Test
    void aVehiclesCatalogue_isGraftedUnderTheSitesRoot_derivedFromTheClosure() {
        var c = ComponentTrees.compose("gallery", List.of(SiteCrate.INSTANCE));
        NormalizedNode root = c.root();
        assertEquals("gallery", root.segment().value());
        assertEquals(TreeLevel.L0.INSTANCE, root.level());
        assertEquals(1, root.children().size(), "one vehicle in the closure: the site itself ships no catalogue");
        NormalizedNode boxes = root.children().get(0);
        assertEquals("box", boxes.segment().value(), "the vehicle's root segment: BoxComponents without its suffix");
        assertEquals(TreeLevel.L1.INSTANCE, boxes.level(), "grafted one level under the root");
        assertEquals(List.of("controls", "box"), boxes.children().stream().map(n -> n.segment().value()).toList(), "sub-catalogues first, then leaves");
        NormalizedNode controls = boxes.children().get(0), knob = controls.children().get(0);
        assertEquals(TreeLevel.L2.INSTANCE, controls.level());
        assertEquals(TreeLevel.L3.INSTANCE, knob.level());
        assertEquals("knob", knob.segment().value());
        var d = (ComponentDetails.OfComponent) c.detailsOf(knob.identity());
        assertEquals(UiComponent.Shape.ELEMENT, d.shape());
        assertEquals("button", d.tag());
        assertEquals(BoxModule.class.getName(), d.module());
        assertEquals("boxes", d.crate());
        assertEquals(List.of("box", "controls", "knob"), d.path(), "the words a caller reaches it by, from the vehicle");
        assertEquals("Knob", d.row().label());
        assertEquals("element", d.row().badge());
        var box = (ComponentDetails.OfComponent) c.detailsOf(boxes.children().get(1).identity());
        assertEquals("A box on a branch.", box.row().note());
        var vehicle = (ComponentDetails.OfVehicle) c.detailsOf(boxes.identity());
        assertEquals(2, vehicle.componentCount());
        assertEquals("1 vehicle · 2 components", c.detailsOf(root.identity()).row().note());
        assertEquals(List.of(), ComponentTrees.validate(List.of(SiteCrate.INSTANCE)));
        // a vehicle standalone is at L0; the same tree, the same segments, one level up
        var standalone = ComponentTrees.normalize(BoxCrate.INSTANCE, new java.util.HashMap<>());
        assertEquals(TreeLevel.L0.INSTANCE, standalone.level());
        assertEquals("knob", standalone.children().get(0).children().get(0).segment().value());
    }

    // ── what the guard refuses ────────────────────────────────────────────
    public record LoneModule() implements DomModule<LoneModule> {
        public static final LoneModule INSTANCE = new LoneModule();
        public record Lone() implements BranchComponent<LoneModule> {}
        @Override public ImportsFor<LoneModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<LoneModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Lone())); }
    }
    /** Exports a component but ships no catalogue. */
    static final class LoneCrate implements Crate {
        static final LoneCrate INSTANCE = new LoneCrate();
        @Override public String name() { return "lone"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(LoneModule.INSTANCE)); }
    }
    /** Lists a component of a module it does not ship, and lists the box a second time. */
    public record BorrowedComponents() implements C0_Components<BorrowedComponents> {
        public static final BorrowedComponents INSTANCE = new BorrowedComponents();
        @Override public String name() { return "Borrowed"; }
        @Override public List<ComponentEntry<BorrowedComponents>> leaves() { return List.of(ComponentEntry.of(this, new BoxModule.Box()), ComponentEntry.of(this, new BoxModule.Box())); }
    }
    static final class BorrowingCrate implements Crate, ComponentVehicle {
        static final BorrowingCrate INSTANCE = new BorrowingCrate();
        @Override public String name() { return "borrowing"; }
        @Override public List<CrateEntry> entries() { return List.of(); }
        @Override public List<Crate> requires() { return List.of(BoxCrate.INSTANCE); }
        @Override public C0_Components<?> components() { return BorrowedComponents.INSTANCE; }
    }
    /** A family that names the wrong parent. */
    public record StrayComponents() implements C1_Components<BoxComponents, StrayComponents> {
        public static final StrayComponents INSTANCE = new StrayComponents();
        @Override public BoxComponents parent() { return BoxComponents.INSTANCE; }
        @Override public String name() { return "Stray"; }
    }
    public record HostComponents() implements C0_Components<HostComponents> {
        public static final HostComponents INSTANCE = new HostComponents();
        @Override public String name() { return "Host"; }
        @Override public List<? extends C1_Components<HostComponents, ?>> subCatalogues() {
            @SuppressWarnings({"unchecked", "rawtypes"}) List<? extends C1_Components<HostComponents, ?>> stray = (List) List.of(StrayComponents.INSTANCE);
            return stray;
        }
    }
    static final class HostCrate implements Crate, ComponentVehicle {
        static final HostCrate INSTANCE = new HostCrate();
        @Override public String name() { return "host"; }
        @Override public List<CrateEntry> entries() { return List.of(); }
        @Override public C0_Components<?> components() { return HostComponents.INSTANCE; }
    }

    @Test
    void theGuard_holdsTheLogicalSideToThePhysical() {
        assertEquals(List.of("lone: LoneModule exports the component Lone but the crate ships no catalogue"),
                ComponentTrees.validate(List.of(LoneCrate.INSTANCE)));
        var borrowed = ComponentTrees.validate(List.of(BorrowingCrate.INSTANCE));
        assertTrue(borrowed.contains("borrowing/borrowed: segment repeated among siblings: box"), borrowed.toString());
        assertTrue(borrowed.contains("borrowing/borrowed/box is exported by no module borrowing ships"), borrowed.toString());
        assertTrue(borrowed.stream().anyMatch(p -> p.contains("is listed already by")), "the box once by borrowing, then by boxes, or twice by borrowing: " + borrowed);
        assertTrue(borrowed.stream().anyMatch(p -> p.startsWith("identity carried by")), "the composed tree carries the box's identity more than once: " + borrowed);
        assertEquals(List.of("host/stray is listed under host but names box"), ComponentTrees.validate(List.of(HostCrate.INSTANCE)));
    }

    // ── where a component's module is found: among the modules its crate ships ──
    /** Lists the lone component and ships no module: the catalogue finds the lone no module. */
    public record StrandedComponents() implements C0_Components<StrandedComponents> {
        public static final StrandedComponents INSTANCE = new StrandedComponents();
        @Override public String name() { return "Stranded"; }
        @Override public List<ComponentEntry<StrandedComponents>> leaves() { return List.of(ComponentEntry.of(this, new LoneModule.Lone())); }
    }
    static final class StrandedCrate implements Crate, ComponentVehicle {
        static final StrandedCrate INSTANCE = new StrandedCrate();
        @Override public String name() { return "stranded"; }
        @Override public List<CrateEntry> entries() { return List.of(); }
        @Override public C0_Components<?> components() { return StrandedComponents.INSTANCE; }
    }
    /** A second module that exports the box, as the box module's: a crate that ships both gives the box two. */
    public record TwinModule() implements DomModule<BoxModule> {
        public static final TwinModule INSTANCE = new TwinModule();
        @Override public ImportsFor<BoxModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<BoxModule> exports() { return new ExportsOf<>(BoxModule.INSTANCE, List.of(new BoxModule.Box())); }
    }
    public record TwinComponents() implements C0_Components<TwinComponents> {
        public static final TwinComponents INSTANCE = new TwinComponents();
        @Override public String name() { return "Twin"; }
        @Override public List<ComponentEntry<TwinComponents>> leaves() { return List.of(ComponentEntry.of(this, new BoxModule.Box()), ComponentEntry.of(this, new BoxModule.Knob())); }
    }
    static final class TwinCrate implements Crate, ComponentVehicle {
        static final TwinCrate INSTANCE = new TwinCrate();
        @Override public String name() { return "twin"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(BoxModule.INSTANCE), CrateEntry.of(TwinModule.INSTANCE)); }
        @Override public C0_Components<?> components() { return TwinComponents.INSTANCE; }
    }

    @Test
    void aComponentsModule_isTheOneModuleItsCrateShipsThatExportsIt() {
        assertEquals(List.of("stranded/stranded/lone is exported by no module stranded ships"), ComponentTrees.validate(List.of(StrandedCrate.INSTANCE)));
        assertEquals(List.of("?"), modules(ComponentTrees.compose("site", List.of(StrandedCrate.INSTANCE))), "no module: none shown");
        assertEquals(List.of("twin/twin/box is exported by more than one module twin ships: BoxModule, TwinModule"),
                ComponentTrees.validate(List.of(TwinCrate.INSTANCE)));
        assertEquals(List.of("?", BoxModule.class.getName()), modules(ComponentTrees.compose("site", List.of(TwinCrate.INSTANCE))),
                "two modules for the box: none shown; one for the knob");
    }

    /** The module shown for each component at the root of the composition's one vehicle. */
    private List<String> modules(ComponentTrees.Composition c) {
        return c.root().children().get(0).children().stream().map(n -> ((ComponentDetails.OfComponent) c.detailsOf(n.identity())).module()).toList();
    }

    @Test
    void aDeclarationIsItsExport_namedAsItsClass() {
        var knob = new BoxModule.Knob();
        assertEquals("knob", knob.segment().value());
        assertEquals("Knob", knob.label());
        assertEquals(UiComponent.Shape.ELEMENT, knob.shape());
        assertEquals(UiComponent.Shape.BRANCH, new BoxModule.Box().shape());
    }
}
