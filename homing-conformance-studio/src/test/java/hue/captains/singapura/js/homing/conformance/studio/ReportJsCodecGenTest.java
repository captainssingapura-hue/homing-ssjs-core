package hue.captains.singapura.js.homing.conformance.studio;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0044 Phase 8 — the polyglot JS target generates a class + codec for every
 * report record from the shared manifest (identity wire shape, which round-trips
 * the exported JSON into typed instances for the widgets). Same schema as the
 * Java target; different language.
 */
class ReportJsCodecGenTest {

    @Test
    void generatesAClassAndCodecPerReportRecord() {
        String js = ReportJsCodecGen.generate();
        for (String name : List.of("ConformanceReport", "CrateReport", "ModuleResult", "FindingReport")) {
            assertTrue(js.contains("class " + name + " {"), () -> name + " JS class missing:\n" + js);
            assertTrue(js.contains("class " + name + "Codec {"), () -> name + "Codec missing:\n" + js);
        }
        assertTrue(js.contains("static transformTo(typed)"), "codecs must expose transformTo");
        assertTrue(js.contains("static transformFrom(wire)"), "codecs must expose transformFrom");
    }
}
