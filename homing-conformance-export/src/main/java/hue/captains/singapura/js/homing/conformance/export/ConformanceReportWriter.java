package hue.captains.singapura.js.homing.conformance.export;

import hue.captains.singapura.js.homing.conformance.report.codec.ConformanceReportCodec;
import hue.captains.singapura.js.homing.conformance.report.codec.ModuleResultCodec;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * RFC 0044 Phase 8 — writes an assembled {@link ConformanceRun} to the report
 * layout (D5) under a directory: the consolidated summary to {@code report.json}
 * and each module to {@code modules/<classId>.json}, serialized by the generated
 * Java codec. The build runs this; the studio reads via {@link
 * ConformanceReportSource}.
 */
public final class ConformanceReportWriter {

    public void write(Path dir, ConformanceRun run) throws IOException {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(ConformanceReportFiles.SUMMARY),
                ConformanceReportCodec.INSTANCE.transformTo(run.summary()), StandardCharsets.UTF_8);

        Path modulesDir = dir.resolve(ConformanceReportFiles.MODULES_DIR);
        Files.createDirectories(modulesDir);
        for (ModuleResult m : run.modules()) {
            Files.writeString(modulesDir.resolve(ConformanceReportFiles.moduleFile(m.moduleClass())),
                    ModuleResultCodec.INSTANCE.transformTo(m), StandardCharsets.UTF_8);
        }
    }
}
