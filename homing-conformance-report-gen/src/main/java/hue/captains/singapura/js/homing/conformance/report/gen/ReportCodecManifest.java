package hue.captains.singapura.js.homing.conformance.report.gen;

import hue.captains.singapura.js.homing.base.lang.StatelessSingleton;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceReport;
import hue.captains.singapura.js.homing.conformance.rules.report.CrateReport;
import hue.captains.singapura.js.homing.conformance.rules.report.FindingReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;
import hue.captains.singapura.js.homing.conformance.rules.report.RuleReport;
import hue.captains.singapura.js.homing.conformance.rules.report.RuleSetReport;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * The report data model as codec entries — every record whose Java (and, later,
 * JS) codec is generated. Java has no forward-reference constraint (all codecs
 * land in one package, compiled together), so order is immaterial; it is kept
 * leaf-first for readability.
 */
public record ReportCodecManifest() implements StatelessFunctionalObject, StatelessSingleton {

    public static final ReportCodecManifest INSTANCE = new ReportCodecManifest();

    /** The records the report's codecs are generated for. */
    public List<ObjectDefinition<?>> entries() {
        return List.of(
            new ObjectDefinition<>(FindingReport.class),
            new ObjectDefinition<>(CrateReport.class),
            new ObjectDefinition<>(ModuleResult.class),
            new ObjectDefinition<>(RuleReport.class),
            new ObjectDefinition<>(RuleSetReport.class),
            new ObjectDefinition<>(ConformanceReport.class));
    }
}
