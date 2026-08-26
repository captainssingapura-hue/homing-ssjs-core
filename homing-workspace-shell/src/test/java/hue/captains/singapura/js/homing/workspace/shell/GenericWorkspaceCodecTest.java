package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.ParamCodecLaw;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * RFC 0051 Phase 2 — the round-trip law for this module's coded apps.
 *
 * <p>The law holds for what the record NAMES. It says nothing about B2: this
 * app's JS reads {@code ?widget=} and forwards every remaining query key to
 * the mounted widget, so its real parameter surface is wider than
 * {@code Params(ws_kind)}. A passing round-trip is therefore not a licence to
 * treat it as fully converted — see {@link #theLawCoversOnlyWhatTheRecordNames()}.</p>
 */
class GenericWorkspaceCodecTest {

    private static final Set<String> COVERED = Set.of("genericWorkspace");

    @Test
    void genericWorkspace() {
        ParamCodecLaw.assertRoundTrips("GenericWorkspace", GenericWorkspace.CODEC, List.of(
                new GenericWorkspace.Params("studio"),
                new GenericWorkspace.Params("animals-playground"),
                new GenericWorkspace.Params("a&b=c")));
    }

    @Test
    void aMissingKindIsNamed() {
        var decoded = GenericWorkspace.CODEC.fromQueryString("widget=tabs");
        var missing = assertInstanceOf(ParamCodec.Decoded.Missing.class, decoded);
        assertEquals("ws_kind", missing.key());
    }

    /**
     * B2, stated as a test rather than only as a note. {@code ?widget=} is a
     * parameter this app genuinely reads, and the codec neither writes it nor
     * reads it back — which is exactly why a path-addressed workspace cannot
     * carry one. The day the surface is modelled, this test fails and says so.
     */
    @Test
    void theLawCoversOnlyWhatTheRecordNames() {
        var written = GenericWorkspace.CODEC.to(new GenericWorkspace.Params("studio"));
        assertEquals(Set.of("ws_kind"), written.keySet(),
                "the codec's surface changed — if `widget` is now modelled, B2 is resolvable");

        // Read back a URL the app itself would honour on the flat route: the
        // widget key survives in the query but not in the params, so a path
        // URL (which has no query at all) loses it entirely.
        var back = GenericWorkspace.CODEC.fromQueryString("ws_kind=studio&widget=tabs");
        assertEquals(new GenericWorkspace.Params("studio"), back.orNull());
    }

    @Test
    void everyCodedAppInThisCrateIsCovered() {
        var uncovered = new TreeSet<String>();
        for (var entry : WorkspaceShellCrate.INSTANCE.entries()) {
            if (entry.module() instanceof AppModule<?, ?> app
                    && !(app.paramCodec() instanceof ParamCodec.None)
                    && !COVERED.contains(app.simpleName())) {
                uncovered.add(app.simpleName());
            }
        }
        assertEquals(Set.of(), uncovered,
                "these apps declare a ParamCodec but no round-trip samples — add them to "
              + GenericWorkspaceCodecTest.class.getSimpleName());
    }
}
