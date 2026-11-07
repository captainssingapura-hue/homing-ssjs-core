package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentVehicle;
import hue.captains.singapura.js.homing.component.WidgetSlotModule;
import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardEventsModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardSecretaryModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardStewardModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardChordsModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardShortcutsModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardWalkModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardHomeModule;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardMarkModule;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.party.PartyModule;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-server}: the two runtime
 * manager modules it ships, the component base's primitives, and the keyboard
 * party — whose steward is the one component it catalogues. A leaf crate: it
 * requires no other crate.
 */
public final class ServerCrate implements Crate, ComponentVehicle {

    public static final ServerCrate INSTANCE = new ServerCrate();

    private ServerCrate() {}

    @Override public C0_Components<?> components() { return ServerComponents.INSTANCE; }

    @Override
    public String name() {
        return "homing-server";
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                new CrateEntry(CssClassManager.INSTANCE),
                new CrateEntry(HrefManager.INSTANCE),
                new CrateEntry(PreferenceSteward.INSTANCE),
                new CrateEntry(CssDependencyGraph.INSTANCE),
                new CrateEntry(CssHandles.INSTANCE),
                new CrateEntry(CssLoadProcedure.INSTANCE),
                new CrateEntry(CssLinks.INSTANCE),
                new CrateEntry(ThemeSheets.INSTANCE),
                // RFC 0067, Two Classes per Leaf - an element wears its leaf's style-aware classes.
                new CrateEntry(StyledLeaves.INSTANCE),
                // RFC 0066 E3 - the component base's own: the widget slot, a
                // primitive that places and removes a root and mints nothing.
                // Declared here because a Maven module has one crate, and this
                // crate is the base's until the rename sweep.
                new CrateEntry(WidgetSlotModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                // RFC 0066 E3 - the Party primitive (RFC 0028), lifted from the workspace: headless.
                new CrateEntry(PartyModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // RFC 0066 E3 - the keyboard party: its secretary, its steward (the DOM face,
                // one per page), the walk over the focus tree, the claiming convention, and
                // its events as data.
                new CrateEntry(FocusPartyModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(KeyboardSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                new CrateEntry(KeyboardStewardModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                new CrateEntry(KeysModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                new CrateEntry(KeyboardEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(KeyboardWalkModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(KeyboardHomeModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(KeyboardMarkModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                new CrateEntry(KeyboardShortcutsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(KeyboardChordsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
