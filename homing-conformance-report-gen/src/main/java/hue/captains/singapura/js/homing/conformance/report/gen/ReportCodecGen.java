package hue.captains.singapura.js.homing.conformance.report.gen;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.codec.java.JavaFunctionsCodeGen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Build-time driver: apply {@link JavaFunctionsCodeGen} to every entry in the
 * {@link ReportCodecManifest} and write the emitted Java codec source under the
 * output directory (arg 0), in the codec package's directory layout.
 *
 * <p>Invoked by the output-only {@code homing-conformance-report-codec} module's
 * {@code exec} plugin at {@code generate-sources} — the generated {@code .java}
 * is "data" and lives only in {@code target/generated-sources}, never committed.
 * The generator + this driver are the managed "functions"; their output is not.</p>
 */
public final class ReportCodecGen {

    private ReportCodecGen() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: ReportCodecGen <output-source-directory>");
        }
        Path pkgDir = Paths.get(args[0]).resolve(JavaFunctionsCodeGen.CODEC_PACKAGE.replace('.', '/'));
        Files.createDirectories(pkgDir);

        for (ObjectDefinition<?> def : ReportCodecManifest.ENTRIES) {
            String source = JavaFunctionsCodeGen.INSTANCE.generate(def);
            Path file = pkgDir.resolve(def.type().getSimpleName() + "Codec.java");
            Files.writeString(file, source, StandardCharsets.UTF_8);
        }

        System.out.println("[ReportCodecGen] wrote " + ReportCodecManifest.ENTRIES.size()
                + " codec(s) to " + pkgDir);
    }
}
