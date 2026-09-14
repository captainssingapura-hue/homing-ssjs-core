package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.ParamCodecLaw;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RFC 0058 — the authentic-path app's params are the group and nothing else;
 * the kind is the anchor and never a parameter. Round-trip law as for every
 * coded app (RFC 0051 Phase 2).
 */
class WorkspaceGroupAppCodecTest {

    @Test
    void theGroupRoundTrips() {
        ParamCodecLaw.assertRoundTrips("WorkspaceGroupApp", WorkspaceGroupApp.CODEC, List.of(
                new WorkspaceGroupApp.Params("fx-desk"),
                new WorkspaceGroupApp.Params("studio-ws"),
                new WorkspaceGroupApp.Params("a&b=c")));
        assertEquals(Set.of("ws_group"), WorkspaceGroupApp.CODEC.to(new WorkspaceGroupApp.Params("fx-desk")).keySet());
    }

    @Test
    void aMissingGroupIsNamed_andAKindIsNotAParameter() {
        var decoded = WorkspaceGroupApp.CODEC.fromQueryString("ws_kind=trader");
        var missing = assertInstanceOf(ParamCodec.Decoded.Missing.class, decoded);
        assertEquals("ws_group", missing.key());
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceGroupApp.Params(" "));
        // A ws_kind beside the group is not read: the anchor names the kind.
        assertEquals(new WorkspaceGroupApp.Params("fx-desk"),
                WorkspaceGroupApp.CODEC.fromQueryString("ws_group=fx-desk&ws_kind=trader").orNull());
    }
}
