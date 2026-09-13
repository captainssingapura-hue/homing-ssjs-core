package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.ParamCodecLaw;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0051 Phase 2 — the round-trip law for this module's coded apps; RFC 0058
 * — the two forms the codec reads. {@code ws_group} is canonical and what a
 * placement writes; {@code ws_kind} is the legacy form, resolved to the group
 * holding the kind (the pair rides), or left groupless when none does.
 *
 * <p>The law holds for what the record NAMES. It says nothing about B2: this
 * app's JS reads {@code ?widget=} and forwards every remaining query key to
 * the mounted widget, so its real parameter surface is wider than the record.
 * A passing round-trip is therefore not a licence to treat it as fully
 * converted — see {@link #theLawCoversOnlyWhatTheRecordNames()}.</p>
 */
class GenericWorkspaceCodecTest {

    private static final Set<String> COVERED = Set.of("genericWorkspace");

    @AfterEach
    void reset() {
        WorkspaceGroupRegistry.INSTANCE.resetForTesting();
        WorkspaceSpecRegistry.INSTANCE.resetForTesting();
    }

    @Test
    void genericWorkspaceRoundTripsBothForms() {
        ParamCodecLaw.assertRoundTrips("GenericWorkspace", GenericWorkspace.CODEC, List.of(
                new GenericWorkspace.Params("studio-ws", null),          // a placement
                new GenericWorkspace.Params("fx-desk", "trader"),         // legacy kind, its group resolved
                new GenericWorkspace.Params(null, "orphan"),              // legacy kind no group holds
                new GenericWorkspace.Params("a&b=c", null)));
    }

    @Test
    void aMissingPairIsNamed() {
        var decoded = GenericWorkspace.CODEC.fromQueryString("widget=tabs");
        var missing = assertInstanceOf(ParamCodec.Decoded.Missing.class, decoded);
        assertEquals("ws_group", missing.key());
        assertThrows(IllegalArgumentException.class, () -> new GenericWorkspace.Params(null, null));
    }

    @Test
    void aLegacyKindResolvesItsGroup() {
        var trader = WorkspaceGroupTest.spec("trader", "Trader Desk", "Trading");
        WorkspaceSpecRegistry.INSTANCE.register(trader);
        WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of("fx-desk", "FX Desk", "", List.of(trader)));

        assertEquals(new GenericWorkspace.Params("fx-desk", "trader"),
                GenericWorkspace.CODEC.fromQueryString("ws_kind=trader").orNull());
        // Groupless when no group holds the kind — the address a pre-group deployment minted.
        assertEquals(new GenericWorkspace.Params(null, "orphan"),
                GenericWorkspace.CODEC.fromQueryString("ws_kind=orphan").orNull());
        // The canonical form is read as written; a kind beside it rides along.
        assertEquals(new GenericWorkspace.Params("fx-desk", null),
                GenericWorkspace.CODEC.fromQueryString("ws_group=fx-desk").orNull());
        assertEquals(new GenericWorkspace.Params("fx-desk", "trader"),
                GenericWorkspace.CODEC.fromQueryString("ws_group=fx-desk&ws_kind=trader").orNull());
    }

    @Test
    void aKindIsASubNodeOfItsGroup() {
        var trader = WorkspaceGroupTest.spec("trader", "Trader Desk", "Trading");
        WorkspaceSpecRegistry.INSTANCE.register(trader);
        var group = WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of("fx-desk", "FX Desk", "", List.of(trader)));

        var anchored = GenericWorkspace.INSTANCE.anchorOf(new GenericWorkspace.Params("fx-desk", "trader")).orElseThrow();
        assertEquals(GenericWorkspace.ofGroup(group), anchored.node());
        assertEquals("ws/trading/trader", anchored.anchor());
        // The group itself, and a groupless kind, name no sub-node.
        assertTrue(GenericWorkspace.INSTANCE.anchorOf(new GenericWorkspace.Params("fx-desk", null)).isEmpty());
        assertTrue(GenericWorkspace.INSTANCE.anchorOf(new GenericWorkspace.Params(null, "orphan")).isEmpty());
        assertTrue(GenericWorkspace.INSTANCE.anchorOf(new GenericWorkspace.Params("fx-desk", "not-a-member")).isEmpty());
    }

    /**
     * B2, stated as a test rather than only as a note. {@code ?widget=} is a
     * parameter this app genuinely reads, and the codec neither writes it nor
     * reads it back — which is exactly why a path-addressed workspace cannot
     * carry one. The day the surface is modelled, this test fails and says so.
     */
    @Test
    void theLawCoversOnlyWhatTheRecordNames() {
        var written = GenericWorkspace.CODEC.to(new GenericWorkspace.Params("studio-ws", null));
        assertEquals(Set.of("ws_group"), written.keySet(),
                "the codec's surface changed — if `widget` is now modelled, B2 is resolvable");

        // Read back a URL the app itself would honour on the flat route: the
        // widget key survives in the query but not in the params, so a path
        // URL (which has no query at all) loses it entirely.
        var back = GenericWorkspace.CODEC.fromQueryString("ws_group=studio-ws&widget=tabs");
        assertEquals(new GenericWorkspace.Params("studio-ws", null), back.orNull());
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
