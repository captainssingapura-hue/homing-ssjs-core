package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;
import hue.captains.singapura.js.homing.component.ComponentVehicle;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.ui.menu.tree.ContextMenuKind;
import hue.captains.singapura.js.homing.ui.menu.tree.M1_Node;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Discovery and registration in one walk: a component declares the kinds
 * it needs; a site's registry is derived from the components its crate
 * closure catalogues — each kind once, in catalogue order — and a need
 * nobody can find is refused.
 */
class NeedContextMenuTest {

    // ── two kinds, owned by the crate whose components open them ─────────
    public record TabMenu() implements ContextMenuKind<TabMenu> {
        public static final TabMenu INSTANCE = new TabMenu();
        @Override public List<? extends M1_Node<TabMenu, ?>> children() { return List.of(Close.INSTANCE); }
        public record Close() implements M1_Node<TabMenu, Close> {
            public static final Close INSTANCE = new Close();
            @Override public TabMenu parent() { return TabMenu.INSTANCE; }
            @Override public String label() { return "Close"; }
        }
    }
    public record PaneMenu() implements ContextMenuKind<PaneMenu> {
        public static final PaneMenu INSTANCE = new PaneMenu();
        @Override public List<? extends M1_Node<PaneMenu, ?>> children() { return List.of(Float.INSTANCE); }
        public record Float() implements M1_Node<PaneMenu, Float> {
            public static final Float INSTANCE = new Float();
            @Override public PaneMenu parent() { return PaneMenu.INSTANCE; }
            @Override public String label() { return "Float"; }
        }
    }

    // ── a module with two components: both need the tab menu, one the pane menu too ──
    public record PanesModule() implements DomModule<PanesModule> {
        public static final PanesModule INSTANCE = new PanesModule();
        public record Pane() implements BranchComponent<PanesModule>, NeedContextMenu {
            @Override public Set<ContextMenuKind<?>> required() { return Set.of(TabMenu.INSTANCE, PaneMenu.INSTANCE); }
        }
        public record Strip() implements BranchComponent<PanesModule>, NeedContextMenu {
            @Override public Set<ContextMenuKind<?>> required() { return Set.of(TabMenu.INSTANCE); }
        }
        public record Hand() implements Exportable._Constant<PanesModule> {}
        @Override public ImportsFor<PanesModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<PanesModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Pane(), new Strip(), new Hand())); }
    }
    public record PanesComponents() implements C0_Components<PanesComponents> {
        public static final PanesComponents INSTANCE = new PanesComponents();
        @Override public String name() { return "Panes"; }
        @Override public List<ComponentEntry<PanesComponents>> leaves() { return List.of(ComponentEntry.of(this, new PanesModule.Strip()), ComponentEntry.of(this, new PanesModule.Pane())); }
    }
    static final class PanesCrate implements Crate, ComponentVehicle {
        static final PanesCrate INSTANCE = new PanesCrate();
        @Override public String name() { return "panes"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(PanesModule.INSTANCE)); }
        @Override public C0_Components<?> components() { return PanesComponents.INSTANCE; }
    }
    static final class SiteCrate implements Crate {
        static final SiteCrate INSTANCE = new SiteCrate();
        @Override public String name() { return "site"; }
        @Override public List<CrateEntry> entries() { return List.of(); }
        @Override public List<Crate> requires() { return List.of(PanesCrate.INSTANCE); }
    }

    @Test
    void theRegistryIsDerivedFromWhatTheCataloguedComponentsNeed_eachKindOnce_inCatalogueOrder() {
        var r = ContextMenuRegistry.requiredBy(List.of(SiteCrate.INSTANCE));
        assertEquals(List.of("tab", "pane"), r.kinds().stream().map(ContextMenuKind::kind).toList(), "the strip's tab menu first, then the pane's own; the tab menu once though two components need it");
        assertEquals(List.of(), ContextMenuRegistry.validate(List.of(SiteCrate.INSTANCE)));
        assertTrue(r.js().get(2).contains("\"tab\":{\"kind\":\"tab\"") && r.js().get(2).contains("\"pane\":{\"kind\":\"pane\""), r.js().toString());
    }

    // ── what is refused ───────────────────────────────────────────────────
    public record LooseModule() implements DomModule<LooseModule> {
        public static final LooseModule INSTANCE = new LooseModule();
        /** A need on a constant that is no component. */
        public record Statics() implements Exportable._Constant<LooseModule>, NeedContextMenu {
            @Override public Set<ContextMenuKind<?>> required() { return Set.of(TabMenu.INSTANCE); }
        }
        /** A component with a need, in no catalogue. */
        public record Uncatalogued() implements BranchComponent<LooseModule>, NeedContextMenu {
            @Override public Set<ContextMenuKind<?>> required() { return Set.of(); }
        }
        @Override public ImportsFor<LooseModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<LooseModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Statics(), new Uncatalogued())); }
    }
    static final class LooseCrate implements Crate {
        static final LooseCrate INSTANCE = new LooseCrate();
        @Override public String name() { return "loose"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(LooseModule.INSTANCE)); }
    }

    @Test
    void aNeedNobodyCanFindIsRefused() {
        assertEquals(List.of(
                "loose: LooseModule.Statics needs context menus but is not a declared component",
                "loose: LooseModule.Uncatalogued needs context menus but no catalogue lists it",
                "loose: LooseModule.Uncatalogued needs context menus but names no kind"),
                ContextMenuRegistry.validate(List.of(LooseCrate.INSTANCE)));
        assertEquals(List.of(), ContextMenuRegistry.requiredBy(List.of(LooseCrate.INSTANCE)).kinds(), "nothing derived: the needs are invisible, which is what validate says");
    }
}
