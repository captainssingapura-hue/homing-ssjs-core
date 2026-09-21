package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.C1_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;
import hue.captains.singapura.js.homing.component.ComponentVehicle;
import hue.captains.singapura.js.homing.component.ElementComponent;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The components feed: the composed tree as canonical TreeNode JSON with a
 * display block per row, and one node's details by name-path. The
 * framework's own crates ship no catalogue yet — the feed over them is a
 * root alone — so the vehicle is a fixture of this test.
 */
class ComponentTreeFeedTest {

    // ── the fixture: a vehicle with a box at its root and a knob in a family, required by a site ──
    public record BoxModule() implements DomModule<BoxModule> {
        public static final BoxModule INSTANCE = new BoxModule();
        public record Box() implements BranchComponent<BoxModule> { @Override public String summary() { return "A box on a branch."; } }
        public record Knob() implements ElementComponent<BoxModule> { @Override public String tag() { return "button"; } }
        @Override public ImportsFor<BoxModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<BoxModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Box(), new Knob())); }
    }
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
    static final class SiteCrate implements Crate {
        static final SiteCrate INSTANCE = new SiteCrate();
        @Override public String name() { return "site"; }
        @Override public List<CrateEntry> entries() { return List.of(); }
        @Override public List<Crate> requires() { return List.of(BoxCrate.INSTANCE); }
    }

    @Test
    void theTreeIsRowsWithDisplay_andANodeAnswersByNamePath() throws Exception {
        List<Crate> top = List.of(SiteCrate.INSTANCE);
        var action = new ComponentTreeGetAction(top);
        DocContent tree = action.execute(new ComponentTreeGetAction.Query(null), new EmptyParam.NoHeaders()).get();
        String json = tree.body();
        assertTrue(json.startsWith("{\"level\":\"L0\",\"segment\":\"components\",\"display\":{\"label\":\"components\""), json.substring(0, Math.min(160, json.length())));
        assertTrue(json.contains("\"segment\":\"box\",\"display\":{\"label\":\"Boxes\",\"badge\":\"vehicle\""), json);
        assertTrue(json.contains("\"segment\":\"knob\",\"display\":{\"label\":\"Knob\",\"badge\":\"element\",\"note\":\"" + BoxModule.class.getName().replace("/", "\\/") + "\",\"kind\":\"element\"}"), json);

        DocContent knob = action.execute(new ComponentTreeGetAction.Query("box/controls/knob"), new EmptyParam.NoHeaders()).get();
        assertEquals("{\"segment\":\"knob\",\"level\":\"L3\",\"kind\":\"component\",\"label\":\"Knob\",\"shape\":\"element\",\"tag\":\"button\",\"summary\":\"\","
                   + "\"module\":\"" + BoxModule.class.getName() + "\",\"crate\":\"boxes\",\"path\":\"box\\/controls\\/knob\"}", knob.body());
        DocContent vehicle = action.execute(new ComponentTreeGetAction.Query("box"), new EmptyParam.NoHeaders()).get();
        assertTrue(vehicle.body().contains("\"kind\":\"vehicle\",\"label\":\"Boxes\"") && vehicle.body().endsWith("\"components\":2}"), vehicle.body());
        assertTrue(action.execute(new ComponentTreeGetAction.Query("box/nothing"), new EmptyParam.NoHeaders()).isCompletedExceptionally(), "an unknown path is not found");
    }

    @Test
    void theFrameworksOwnCratesCatalogueOneComponent_theKeyboardSteward() throws Exception {
        DocContent tree = new ComponentTreeGetAction(TopLevelCrates.ALL).execute(new ComponentTreeGetAction.Query(null), new EmptyParam.NoHeaders()).get();
        assertTrue(tree.body().contains("\"note\":\"1 vehicle 00b7 1 component\"") || tree.body().contains("1 vehicle"), tree.body());
        assertTrue(tree.body().contains("\"segment\":\"server\",\"display\":{\"label\":\"Base\",\"badge\":\"vehicle\""), tree.body());
        assertTrue(tree.body().contains("\"segment\":\"keyboard-steward\",\"display\":{\"label\":\"KeyboardSteward\",\"badge\":\"branch\""), tree.body());
    }
}
