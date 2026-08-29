package hue.captains.singapura.js.homing.studio.base.app.tree;

import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.ParamCodecLaw;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** RFC 0051 — TreeAppHost's params, both directions. */
class TreeAppHostCodecTest {

    @Test
    void obeysTheRoundTripLaw() {
        ParamCodecLaw.assertRoundTrips("TreeAppHost", TreeAppHost.CODEC, List.of(
                new TreeAppHost.Params("animals", null),
                new TreeAppHost.Params("animals", ""),
                new TreeAppHost.Params("interactive-animals", "cute/otter"),
                // The values the old string concatenation silently corrupted.
                new TreeAppHost.Params("a&b", "path with spaces"),
                new TreeAppHost.Params("t", "a=b&c=d")));
    }

    @Test
    void missingIdIsNamed_notAGenericFailure() {
        var decoded = TreeAppHost.CODEC.fromQueryString("path=cute/otter");
        var missing = assertInstanceOf(ParamCodec.Decoded.Missing.class, decoded);
        assertEquals("id", missing.key());
        assertTrue(missing.describe().contains("id"));
    }

    @Test
    void absentPathIsAValueNotAnError() {
        // A tree opened at its root has no path; that is the normal case.
        var decoded = TreeAppHost.CODEC.fromQueryString("id=animals");
        assertEquals(new TreeAppHost.Params("animals", null), decoded.orNull());
    }

    @Test
    void urlForNowEscapesWhatItUsedToConcatenate() {
        String url = TreeAppHost.urlFor("animals", "a b&c");
        assertFalse(url.contains("a b&c"), "raw value leaked into the URL: " + url);
        // And it survives being read back, which the old form did not.
        var back = TreeAppHost.CODEC.fromQueryString(url);
        assertEquals(new TreeAppHost.Params("animals", "a b&c"), back.orNull());
    }

    @Test
    void unknownKeysAreIgnored() {
        // The action layer owns theme/locale; an app has no business refusing
        // a query it was never asked to read.
        var decoded = TreeAppHost.CODEC.fromQueryString("id=animals&theme=forest");
        assertEquals(new TreeAppHost.Params("animals", null), decoded.orNull());
    }
}
