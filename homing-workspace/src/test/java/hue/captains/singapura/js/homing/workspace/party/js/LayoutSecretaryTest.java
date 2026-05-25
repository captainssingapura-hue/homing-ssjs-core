package hue.captains.singapura.js.homing.workspace.party.js;

import hue.captains.singapura.js.homing.ssjs.test.SecretaryTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Diligent-Secretaries-doctrine exemplar test — RFC 0028 cycle 6 phase 2.
 *
 * <p>Exercises the {@code LayoutSecretary} pure behaviour function across
 * every documented message kind, plus the default (unknown) branch and
 * its bounded {@code recentUnknown} ring. The test reaches the JS source
 * directly via GraalVM; the same file the browser loads is the spec.</p>
 *
 * <p>State shape under test:</p>
 * <pre>{@code { fullscreen, lastChangedBy, recentUnknown } }</pre>
 */
class LayoutSecretaryTest extends SecretaryTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/party/LayoutSecretaryModule.js";

    @BeforeEach
    void loadLayoutSecretary() {
        loadSecretary(MODULE, "LayoutSecretary");
    }

    @Test
    @DisplayName("initial state — fullscreen off, no provenance, empty unknown ring")
    void initialState() {
        Value s = initial();
        assertEquals(false, s.getMember("fullscreen").asBoolean());
        assertEquals(true,  s.getMember("lastChangedBy").isNull());
        assertEquals(0L,    s.getMember("recentUnknown").getArraySize());
    }

    @Nested
    @DisplayName("FullscreenToggleRequested")
    class Toggle {
        @Test void flipsFalseToTrue() {
            Value step = dispatch(initial(),
                    envelope("FullscreenToggleRequested", Map.of(), "layout/toggle"));
            assertStateField(step, "fullscreen", true);
            assertStateField(step, "lastChangedBy", "layout/toggle");
            assertActionCount(step, 1);
            assertActionKind(step, 0, "BroadcastToMembers");
            assertEquals("FullscreenChanged",
                    action(step, 0).getMember("message").getMember("kind").asString());
            assertEquals(true,
                    action(step, 0).getMember("message").getMember("on").asBoolean());
        }

        @Test void flipsTrueBackToFalse() {
            Value once = dispatch(initial(),
                    envelope("FullscreenToggleRequested", Map.of(), "a"));
            Value twice = dispatch(once.getMember("newState"),
                    envelope("FullscreenToggleRequested", Map.of(), "b"));
            assertStateField(twice, "fullscreen", false);
            assertStateField(twice, "lastChangedBy", "b");
            assertEquals(false,
                    action(twice, 0).getMember("message").getMember("on").asBoolean());
        }
    }

    @Nested
    @DisplayName("FullscreenSetRequested")
    class Set {
        @Test void setsToTrueWhenCurrentlyFalse() {
            Value step = dispatch(initial(),
                    envelope("FullscreenSetRequested", Map.of("on", true), "ribbon/fs"));
            assertStateField(step, "fullscreen", true);
            assertActionCount(step, 1);
            assertEquals(true,
                    action(step, 0).getMember("message").getMember("on").asBoolean());
        }

        @Test void noOpWhenAlreadyAtRequestedValue() {
            Value step = dispatch(initial(),
                    envelope("FullscreenSetRequested", Map.of("on", false), "ribbon/fs"));
            // Pure no-op: identity preserved, no actions.
            assertSame(initial().getMember("fullscreen").asBoolean(),
                    step.getMember("newState").getMember("fullscreen").asBoolean());
            assertStateField(step, "lastChangedBy", null);
            assertActionCount(step, 0);
        }

        @Test void coercesTruthyToBoolean() {
            Value step = dispatch(initial(),
                    envelope("FullscreenSetRequested", Map.of("on", 1), "x"));
            assertStateField(step, "fullscreen", true);
        }
    }

    @Nested
    @DisplayName("default branch — unknown messages")
    class Unknown {
        @Test void appendsToRecentUnknownAndEmitsNoActions() {
            Value step = dispatch(initial(),
                    envelope("MysteryRequest", Map.of("p", 7), "stranger"));
            assertActionCount(step, 0);
            // fullscreen unchanged, provenance unchanged
            assertStateField(step, "fullscreen", false);
            assertStateField(step, "lastChangedBy", null);
            Value recent = step.getMember("newState").getMember("recentUnknown");
            assertEquals(1L, recent.getArraySize());
            assertEquals("MysteryRequest",
                    recent.getArrayElement(0).getMember("kind").asString());
            assertEquals("stranger",
                    recent.getArrayElement(0).getMember("from").asString());
        }

        @Test void recentUnknownRingIsBoundedAtTen() {
            Value state = initial();
            // Push 13 unknowns; oldest 3 must be evicted.
            for (int i = 0; i < 13; i++) {
                state = dispatch(state,
                        envelope("Mystery_" + i, Map.of(), "stranger")).getMember("newState");
            }
            Value recent = state.getMember("recentUnknown");
            assertEquals(10L, recent.getArraySize(), "ring bounded at 10");
            // Oldest retained should be Mystery_3 (0..2 evicted); newest Mystery_12.
            assertEquals("Mystery_3",
                    recent.getArrayElement(0).getMember("kind").asString());
            assertEquals("Mystery_12",
                    recent.getArrayElement(9).getMember("kind").asString());
        }
    }

    @Test
    @DisplayName("determinism — same (state, envelope) always yields the same Step")
    void determinism() {
        Value s = initial();
        Value env = envelope("FullscreenToggleRequested", Map.of(), "x");
        Value a = dispatch(s, env);
        Value b = dispatch(s, env);
        assertEquals(a.getMember("newState").getMember("fullscreen").asBoolean(),
                     b.getMember("newState").getMember("fullscreen").asBoolean());
        assertEquals(a.getMember("actions").getArraySize(),
                     b.getMember("actions").getArraySize());
    }
}
