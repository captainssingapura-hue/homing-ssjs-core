package hue.captains.singapura.js.homing.conformance.export;

import hue.captains.singapura.js.homing.conformance.report.codec.ConformanceReportCodec;
import hue.captains.singapura.js.homing.conformance.report.codec.ModuleResultCodec;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0044 Phase 8 — reads an exported report back from disk (the D5 layout),
 * mapping a module result to its module by class id. The decoupled production
 * shape: the build enforces + exports, the studio reads through this source
 * (offline, from the exported files) rather than running the engine in-process.
 * The consolidated summary is loaded whole; per-module results are loaded lazily.
 */
public final class ConformanceReportSource {

    private final Path dir;

    public ConformanceReportSource(Path dir) {
        this.dir = Objects.requireNonNull(dir, "dir");
    }

    /** True iff a report has been exported to this directory. */
    public boolean exists() {
        return Files.exists(dir.resolve(ConformanceReportFiles.SUMMARY));
    }

    /** The consolidated summary (per-crate metadata + roll-ups). */
    public ConformanceReport summary() {
        return ConformanceReportCodec.INSTANCE.transformFrom(read(dir.resolve(ConformanceReportFiles.SUMMARY)));
    }

    /** One module's full result by class id, or empty if it was not exported. */
    public Optional<ModuleResult> module(String moduleClass) {
        Path file = dir.resolve(ConformanceReportFiles.MODULES_DIR)
                .resolve(ConformanceReportFiles.moduleFile(moduleClass));
        if (!Files.exists(file)) return Optional.empty();
        return Optional.of(ModuleResultCodec.INSTANCE.transformFrom(read(file)));
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("failed reading conformance report file " + file, e);
        }
    }
}
