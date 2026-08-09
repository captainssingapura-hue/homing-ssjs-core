package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0044 Phase 8 — the crate-conformance feed serves rule findings sourced from
 * the BUILD-EXPORTED report (read via the ResultSource), not from an in-process
 * engine run. Requires the export to have run at process-classes.
 */
class CrateConformanceFeedTest {

    @Test
    void feedIsSourcedFromTheExportedReport() throws Exception {
        var action = new CrateConformanceGetAction(TopLevelCrates.ALL);
        DocContent dc = action.execute(new CrateConformanceGetAction.Query(null),
                new EmptyParam.NoHeaders()).get();
        String json = dc.body();

        assertTrue(json.contains("\"crates\"") && json.contains("\"modules\""),
                () -> "feed shape: " + json.substring(0, Math.min(200, json.length())));
        // A finding that lives in the exported report must surface in the feed —
        // proof the studio read the report rather than recomputing nothing.
        assertTrue(json.contains("no-raw-href") || json.contains("no-dom-destruction"),
                "exported rule findings must appear in the feed");
    }
}
