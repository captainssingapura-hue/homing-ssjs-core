package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.ssjs.test.SecretaryTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The one field, stepped: a claim is a fact that evicts, and the evicted is
 * told before the new holder is; a claim by the holder is nothing; a release
 * or a leave by anyone but the holder is nothing; keys go to the holder or
 * nowhere; anything else is dropped; the state handed in is never mutated.
 */
class KeyboardSecretaryTest extends SecretaryTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardSecretaryModule.js";

    @BeforeEach
    void load() { loadSecretary(MODULE, "KeyboardSecretary"); }

    private Value held(String by) { return obj(Map.of("holder", by)); }
    private static String to(Value step, int i) { return step.getMember("actions").getArrayElement(i).getMember("to").asString(); }
    private static String said(Value step, int i) { return step.getMember("actions").getArrayElement(i).getMember("message").getMember("kind").asString(); }

    @Test
    void initiallyNoOneHolds() {
        assertTrue(initial().getMember("holder").isNull());
    }

    @Test
    void aClaimGrantsTheSenderWhenNoOneHolds() {
        Value step = dispatch(initial(), envelope("Claim", Map.of(), "slider-1"));
        assertStateField(step, "holder", "slider-1");
        assertActionCount(step, 1);
        assertActionKind(step, 0, "SendToMember");
        assertEquals("slider-1", to(step, 0));
        assertEquals("Granted", said(step, 0));
    }

    @Test
    void aClaimMayNameTheMemberInsteadOfBeingSentByIt() {
        Value step = dispatch(initial(), envelope("Claim", Map.of("id", "grid"), "steward"));
        assertStateField(step, "holder", "grid");
        assertEquals("grid", to(step, 0));
    }

    @Test
    void aClaimEvictsTheHolderWhoIsToldFirstAndByWhom() {
        Value step = dispatch(held("a"), envelope("Claim", Map.of("id", "b"), "steward"));
        assertStateField(step, "holder", "b");
        assertActionCount(step, 2);
        assertEquals("a", to(step, 0));
        assertEquals("Taken", said(step, 0));
        assertEquals("b", action(step, 0).getMember("message").getMember("by").asString());
        assertEquals("b", to(step, 1));
        assertEquals("Granted", said(step, 1));
    }

    @Test
    void aClaimByTheHolderIsNothing() {
        Value step = dispatch(held("a"), envelope("Claim", Map.of("id", "a"), "steward"));
        assertStateField(step, "holder", "a");
        assertActionCount(step, 0);
    }

    @Test
    void aReleaseByTheHolderEmptiesTheFieldAndTellsIt() {
        Value step = dispatch(held("a"), envelope("Release", Map.of("id", "a"), "steward"));
        assertTrue(step.getMember("newState").getMember("holder").isNull());
        assertActionCount(step, 1);
        assertEquals("a", to(step, 0));
        assertEquals("Taken", said(step, 0));
        assertTrue(action(step, 0).getMember("message").getMember("by").isNull(), "by no one");
    }

    @Test
    void aReleaseByAnyoneElseIsNothing() {
        Value step = dispatch(held("a"), envelope("Release", Map.of("id", "b"), "steward"));
        assertStateField(step, "holder", "a");
        assertActionCount(step, 0);
        assertActionCount(dispatch(initial(), envelope("Release", Map.of(), "b")), 0);
    }

    @Test
    void aLeaveIsARelease() {
        Value step = dispatch(held("a"), envelope("Left", Map.of("id", "a"), "steward"));
        assertTrue(step.getMember("newState").getMember("holder").isNull());
        assertActionCount(step, 1);
        assertActionCount(dispatch(held("a"), envelope("Left", Map.of("id", "b"), "steward")), 0);
    }

    @Test
    void keysGoToTheHolderOrNowhere() {
        Value down = dispatch(held("a"), envelope("KeyDown", Map.of("ev", Map.of("key", "ArrowUp")), "steward"));
        assertStateField(down, "holder", "a");
        assertActionCount(down, 1);
        assertEquals("a", to(down, 0));
        assertEquals("KeyDown", said(down, 0));
        assertEquals("ArrowUp", action(down, 0).getMember("message").getMember("ev").getMember("key").asString());
        Value up = dispatch(held("a"), envelope("KeyUp", Map.of("ev", Map.of("key", "ArrowUp")), "steward"));
        assertEquals("KeyUp", said(up, 0));
        assertActionCount(dispatch(initial(), envelope("KeyDown", Map.of("ev", Map.of("key", "ArrowUp")), "steward")), 0);
    }

    @Test
    void anythingElseIsDropped() {
        Value step = dispatch(held("a"), envelope("Hold", Map.of("id", "b"), "steward"));
        assertStateField(step, "holder", "a");
        assertActionCount(step, 0);
    }

    @Test
    void theStateHandedInIsNeverMutated() {
        Value before = held("a");
        dispatch(before, envelope("Claim", Map.of("id", "b"), "steward"));
        dispatch(before, envelope("Release", Map.of("id", "a"), "steward"));
        assertEquals("a", before.getMember("holder").asString());
    }
}
