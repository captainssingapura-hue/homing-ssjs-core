package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.core.Crate;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0044 — generates the crate conformance report to {@code target/crate-report.txt}
 * and asserts the whole crate set is conformant. The report is the presentable,
 * verifiable artifact; the assertion makes it self-checking.
 */
class CrateReportTest {

    @Test
    void crateSetIsConformantAndReportIsWritten() throws IOException {
        // The report covers the full closure of the top-level crates (owned + external).
        List<Crate> all = CrateClosure.of(TopLevelCrates.ALL);
        String report = CrateReport.render(all);

        Path out = Path.of("target", "crate-report.txt");
        Files.createDirectories(out.getParent());
        Files.writeString(out, report, StandardCharsets.UTF_8);
        System.out.println(report);

        assertTrue(CrateReport.isGreen(all),
                "every crate must be conformant — see report:\n" + report);
    }
}
