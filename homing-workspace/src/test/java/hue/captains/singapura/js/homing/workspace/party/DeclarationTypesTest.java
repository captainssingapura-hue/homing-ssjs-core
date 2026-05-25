package hue.captains.singapura.js.homing.workspace.party;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link JsModulePath}, {@link Secretary}, {@link Party},
 * {@link PartySet} — the workspace-setup-time declaration records.
 */
class DeclarationTypesTest {

    // Two distinct workspace-style sealed message ADTs, used to verify
    // PartySet rejects same-ADT collisions.
    private sealed interface AnimalsMsg {
        record Selected(String animal) implements AnimalsMsg {}
    }
    private sealed interface LayoutMsg {
        record FullscreenChanged(boolean on) implements LayoutMsg {}
    }
    private record AnimalsState(String selected) {}
    private record LayoutState(boolean fullscreen) {}

    private static final JsModulePath ANIMALS_JS = new JsModulePath("workspace/animalsSecretary");
    private static final JsModulePath LAYOUT_JS  = new JsModulePath("workspace/layoutSecretary");

    // ─── JsModulePath ──────────────────────────────────────────────────────

    @Test
    void jsModulePathRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new JsModulePath(""));
        assertThrows(IllegalArgumentException.class, () -> new JsModulePath("  "));
        assertThrows(NullPointerException.class, () -> new JsModulePath(null));
    }

    @Test
    void jsModulePathHoldsValue() {
        assertEquals("a/b/c", new JsModulePath("a/b/c").value());
        assertEquals("a/b/c", new JsModulePath("a/b/c").toString());
    }

    // ─── Secretary ─────────────────────────────────────────────────────────

    @Test
    void secretaryRequiresFields() {
        assertThrows(IllegalArgumentException.class, () -> new Secretary<>(
                "", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS, List.of()));
        assertThrows(NullPointerException.class, () -> new Secretary<>(
                null, AnimalsState.class, AnimalsMsg.class, ANIMALS_JS, List.of()));
        assertThrows(NullPointerException.class, () -> new Secretary<>(
                "n", null, AnimalsMsg.class, ANIMALS_JS, List.of()));
        assertThrows(NullPointerException.class, () -> new Secretary<>(
                "n", AnimalsState.class, null, ANIMALS_JS, List.of()));
        assertThrows(NullPointerException.class, () -> new Secretary<>(
                "n", AnimalsState.class, AnimalsMsg.class, null, List.of()));
    }

    @Test
    void leafSecretary() {
        var sec = Secretary.leaf("root", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS);
        assertEquals("root", sec.name());
        assertTrue(sec.subSecretaries().isEmpty());
    }

    @Test
    void secretaryWithSubSecretaries() {
        var child = Secretary.leaf("child", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS);
        var root  = new Secretary<>("root", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS,
                List.of(child));
        assertEquals(1, root.subSecretaries().size());
        assertEquals("child", root.subSecretaries().get(0).name());
    }

    // ─── Party ─────────────────────────────────────────────────────────────

    @Test
    void partyRequiresFields() {
        var root = Secretary.leaf("r", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS);
        assertThrows(IllegalArgumentException.class, () -> new Party<>("", AnimalsMsg.class, root));
        assertThrows(NullPointerException.class, () -> new Party<>(null, AnimalsMsg.class, root));
        assertThrows(NullPointerException.class, () -> new Party<>("p", null, root));
        assertThrows(NullPointerException.class, () -> new Party<>("p", AnimalsMsg.class, null));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void partyRejectsMessageTypeMismatchAgainstRoot() {
        // The static type system already prevents this at compile time —
        // a Party<LayoutMsg> cannot accept a Secretary<?, AnimalsMsg>.
        // The runtime defensive check in Party's constructor exists to
        // catch erasure-bypassed cases (raw types, reflection). Exercise
        // it via raw-type construction.
        var animalsRoot = Secretary.leaf("r", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS);
        assertThrows(IllegalArgumentException.class,
                () -> new Party("layout", LayoutMsg.class, animalsRoot));
    }

    @Test
    void partyAcceptsMatchingMessageTypes() {
        var root = Secretary.leaf("r", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS);
        var party = new Party<>("animalSelection", AnimalsMsg.class, root);
        assertEquals("animalSelection", party.name());
        assertEquals(AnimalsMsg.class, party.messageType());
    }

    // ─── PartySet ──────────────────────────────────────────────────────────

    @Test
    void emptyPartySet() {
        assertTrue(PartySet.empty().parties().isEmpty());
    }

    @Test
    void partySetRejectsDuplicateNames() {
        var a = new Party<>("p",
                AnimalsMsg.class,
                Secretary.leaf("r", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS));
        var b = new Party<>("p",
                LayoutMsg.class,
                Secretary.leaf("r", LayoutState.class, LayoutMsg.class, LAYOUT_JS));
        assertThrows(IllegalArgumentException.class, () -> new PartySet(List.of(a, b)));
    }

    @Test
    void partySetRejectsDuplicateMessageTypes() {
        var a = new Party<>("a",
                AnimalsMsg.class,
                Secretary.leaf("r", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS));
        var b = new Party<>("b",
                AnimalsMsg.class,    // same ADT
                Secretary.leaf("r", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS));
        assertThrows(IllegalArgumentException.class, () -> new PartySet(List.of(a, b)));
    }

    @Test
    void partySetAcceptsDistinctParties() {
        var animals = new Party<>("animalSelection",
                AnimalsMsg.class,
                Secretary.leaf("r", AnimalsState.class, AnimalsMsg.class, ANIMALS_JS));
        var layout = new Party<>("layout",
                LayoutMsg.class,
                Secretary.leaf("r", LayoutState.class, LayoutMsg.class, LAYOUT_JS));
        var set = new PartySet(List.of(animals, layout));
        assertEquals(2, set.parties().size());
    }
}
