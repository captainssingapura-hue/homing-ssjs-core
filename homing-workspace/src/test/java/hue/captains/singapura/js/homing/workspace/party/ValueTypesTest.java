package hue.captains.singapura.js.homing.workspace.party;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link Envelope}, {@link Action}, {@link Step}, {@link Snapshot}
 * — the wire / dispatch / inspection value types. One file per the
 * Modest File Size doctrine; each type's tests are small enough to
 * coexist legibly.
 */
class ValueTypesTest {

    // Sample workspace message ADT — shape for the tests below.
    private sealed interface TestMsg {
        record Ping(int n) implements TestMsg {}
        record Pong(int n) implements TestMsg {}
    }

    private static final AgentId SENDER = AgentId.of("workspace", "ribbon", "ping-control");

    // ─── Envelope ──────────────────────────────────────────────────────────

    @Test
    void envelopeRequiresFromAndMessage() {
        assertThrows(NullPointerException.class, () -> new Envelope<>(null, new TestMsg.Ping(1)));
        assertThrows(NullPointerException.class, () -> new Envelope<>(SENDER, null));
    }

    @Test
    void envelopeHoldsValues() {
        var env = new Envelope<>(SENDER, new TestMsg.Ping(42));
        assertEquals(SENDER, env.from());
        assertInstanceOf(TestMsg.Ping.class, env.message());
        assertEquals(42, ((TestMsg.Ping) env.message()).n());
    }

    // ─── Action sealed ADT ─────────────────────────────────────────────────

    @Test
    void sendToParentRequiresMessage() {
        assertThrows(NullPointerException.class, () -> new Action.SendToParent<>(null));
    }

    @Test
    void broadcastRequiresMessage() {
        assertThrows(NullPointerException.class, () -> new Action.BroadcastToMembers<>(null));
    }

    @Test
    void sendToMemberRequiresTargetAndMessage() {
        assertThrows(NullPointerException.class,
                () -> new Action.SendToMember<TestMsg>(null, new TestMsg.Pong(1)));
        assertThrows(NullPointerException.class,
                () -> new Action.SendToMember<TestMsg>(SENDER, null));
    }

    @Test
    void exhaustiveSwitchOverAction() {
        Action<TestMsg> a = new Action.SendToParent<>(new TestMsg.Ping(7));
        // Sealed pattern match — compile error if a variant is missed,
        // proving the type family stays closed.
        String label = switch (a) {
            case Action.SendToParent<TestMsg> __     -> "parent";
            case Action.BroadcastToMembers<TestMsg> __ -> "broadcast";
            case Action.SendToMember<TestMsg> __     -> "member";
        };
        assertEquals("parent", label);
    }

    // ─── Step ──────────────────────────────────────────────────────────────

    @Test
    void stepRejectsNullNewState() {
        assertThrows(NullPointerException.class, () -> new Step<>(null, List.of()));
    }

    @Test
    void stepNullActionsBecomeEmpty() {
        var step = new Step<String, TestMsg>("state", null);
        assertTrue(step.actions().isEmpty());
    }

    @Test
    void stepStayHelper() {
        Step<String, TestMsg> step = Step.stay("idle");
        assertEquals("idle", step.newState());
        assertTrue(step.actions().isEmpty());
    }

    @Test
    void stepOfHelpers() {
        var one = Step.of("s1", new Action.SendToParent<>(new TestMsg.Pong(1)));
        assertEquals(1, one.actions().size());

        var many = Step.of("s2",
                new Action.SendToParent<>(new TestMsg.Pong(1)),
                new Action.BroadcastToMembers<>(new TestMsg.Pong(2)));
        assertEquals(2, many.actions().size());
    }

    @Test
    void stepActionsAreImmutableCopy() {
        var raw = new java.util.ArrayList<Action<TestMsg>>();
        raw.add(new Action.BroadcastToMembers<>(new TestMsg.Ping(1)));
        Step<String, TestMsg> step = new Step<>("s", raw);
        raw.clear();
        assertEquals(1, step.actions().size());   // mutation of the source list doesn't leak
    }

    // ─── Snapshot ──────────────────────────────────────────────────────────

    @Test
    void snapshotRequiresNonBlankLabel() {
        assertThrows(IllegalArgumentException.class, () -> new Snapshot("", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new Snapshot("  ", Map.of()));
    }

    @Test
    void snapshotRequiresNonNullFields() {
        assertThrows(NullPointerException.class, () -> new Snapshot(null, Map.of()));
        assertThrows(NullPointerException.class, () -> new Snapshot("label", null));
    }

    @Test
    void snapshotHoldsValues() {
        var snap = new Snapshot("WorkspaceSecretary", Map.of("selected", "Turtle", "count", 3));
        assertEquals("WorkspaceSecretary", snap.label());
        assertEquals("Turtle", snap.keyFacts().get("selected"));
        assertEquals(3, snap.keyFacts().get("count"));
    }

    @Test
    void snapshotOfHelper() {
        var snap = Snapshot.of("Empty");
        assertEquals("Empty", snap.label());
        assertTrue(snap.keyFacts().isEmpty());
    }
}
