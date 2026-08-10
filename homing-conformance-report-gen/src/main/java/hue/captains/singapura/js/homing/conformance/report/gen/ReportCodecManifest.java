package hue.captains.singapura.js.homing.conformance.report.gen;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceReport;
import hue.captains.singapura.js.homing.conformance.rules.report.CrateReport;
import hue.captains.singapura.js.homing.conformance.rules.report.FindingReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;
import hue.captains.singapura.js.homing.conformance.rules.report.RuleReport;
import hue.captains.singapura.js.homing.conformance.rules.report.RuleSetReport;

import java.util.List;

/**
 * The report data model as codec entries — every record whose Java (and, later,
 * JS) codec is generated. Java has no forward-reference constraint (all codecs
 * land in one package, compiled together), so order is immaterial; it is kept
 * leaf-first for readability.
 */
public final class ReportCodecManifest {

    private ReportCodecManifest() {}

    public static final List<ObjectDefinition<?>> ENTRIES = List.of(
            ObjectDefinition.of(FindingReport.class),
            ObjectDefinition.of(CrateReport.class),
            ObjectDefinition.of(ModuleResult.class),
            ObjectDefinition.of(RuleReport.class),
            ObjectDefinition.of(RuleSetReport.class),
            ObjectDefinition.of(ConformanceReport.class));
}
