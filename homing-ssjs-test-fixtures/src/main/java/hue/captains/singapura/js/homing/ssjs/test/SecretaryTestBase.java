package hue.captains.singapura.js.homing.ssjs.test;

import org.graalvm.polyglot.Value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Diligent-Secretaries-doctrine test base (RFC 0028) — layers Step-contract
 * helpers on top of {@link JsModuleTestBase}.
 *
 * <p>A "Secretary" is the homing convention for a pure-function message
 * router with the shape:</p>
 *
 * <pre>{@code
 * var XxxSecretary = {
 *     initial : { ...state fields... },
 *     behavior: function (state, envelope) {
 *         return { newState: {...}, actions: [ {kind, ...}, ... ] };
 *     }
 * };
 * }</pre>
 *
 * <p>The doctrine requires that {@code behavior} be pure, deterministic,
 * and exhaustively tested. This base makes that ergonomic: load the
 * module, dispatch envelopes, assert the {@code Step} record.</p>
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * class MySecretaryTest extends SecretaryTestBase {
 *     @BeforeEach void load() { loadSecretary(
 *         "/path/to/MySecretaryModule.js", "MySecretary"); }
 *
 *     @Test void requestHandled() {
 *         var step = dispatch(initial(),
 *             envelope("DoThing", Map.of("x", 1), "actor/a"));
 *         assertStateField(step, "lastValue", 1);
 *         assertActionCount(step, 1);
 *         assertActionKind(step, 0, "BroadcastToMembers");
 *     }
 * }
 * }</pre>
 */
public abstract class SecretaryTestBase extends JsModuleTestBase {

    /**
     * The loaded Secretary object — has {@code initial} and {@code behavior}
     * members. Set by {@link #loadSecretary(String, String)}.
     */
    protected Value secretary;

    /**
     * Load a Secretary module and capture {@code globalName} into
     * {@link #secretary}, asserting the {@code initial} + {@code behavior}
     * members exist.
     */
    protected void loadSecretary(String resourcePath, String globalName) {
        secretary = loadGlobal(resourcePath, globalName);
        assertTrue(secretary.hasMember("initial"),
                globalName + ".initial missing");
        assertTrue(secretary.hasMember("behavior"),
                globalName + ".behavior missing");
    }

    /**
     * A deep clone of the Secretary's declared initial state — safe to
     * mutate (or pass through dispatch chains) without disturbing the
     * canonical record on the loaded module.
     */
    protected Value initial() {
        assertNotNull(secretary, "loadSecretary(...) must be called before initial()");
        return deepCopy(secretary.getMember("initial"));
    }

    /** Invoke {@code behavior(state, envelope)}; returns the {@code Step} record. */
    protected Value dispatch(Value state, Value envelope) {
        assertNotNull(secretary, "loadSecretary(...) must be called before dispatch()");
        return secretary.getMember("behavior").execute(state, envelope);
    }

    // ─── Step assertions — terse so test bodies read like fact tables ──────

    /** Assert {@code step.newState.<field>} equals {@code expected}. */
    protected void assertStateField(Value step, String field, Object expected) {
        Value newState = step.getMember("newState");
        assertNotNull(newState, "step.newState missing");
        assertEquals(expected, newState.getMember(field).as(Object.class),
                "newState." + field);
    }

    /** Assert {@code step.actions.length} equals {@code expected}. */
    protected void assertActionCount(Value step, int expected) {
        Value actions = step.getMember("actions");
        assertNotNull(actions, "step.actions missing");
        assertEquals(expected, (int) actions.getArraySize(), "actions.length");
    }

    /** Assert {@code step.actions[index].kind} equals {@code expected}. */
    protected void assertActionKind(Value step, int index, String expected) {
        assertEquals(expected, step.getMember("actions").getArrayElement(index)
                .getMember("kind").asString(), "actions[" + index + "].kind");
    }

    /** Pluck {@code step.actions[index]} for further inspection. */
    protected Value action(Value step, int index) {
        return step.getMember("actions").getArrayElement(index);
    }
}
