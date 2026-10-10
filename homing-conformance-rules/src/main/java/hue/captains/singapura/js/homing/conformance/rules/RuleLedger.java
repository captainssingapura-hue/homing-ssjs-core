package hue.captains.singapura.js.homing.conformance.rules;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * One rule's debt in a gate that is otherwise strict: the fingerprints of the rule's findings when
 * the rule came, recorded in a file beside the gate's tests, so a new rule can be applied at once
 * without its every finding fixed first. Only this rule's recorded findings are let through, as
 * warnings; every other rule, and any new finding of this one, still fails. A ratchet: a recorded
 * finding that no longer occurs is stale, and the gate says so, so the ledger only shrinks.
 *
 * <p>Read and written in UTF-8 - a fingerprint carries the offending line, dashes and all. Written
 * by the gate itself when it is run with {@code -Dconformance.recordLedger=true}, to {@code
 * src/test/resources/<file>} of the module under test - or its main resources, for a ledger a
 * build-time export reads too: deliberately, never to silence a finding.</p>
 *
 * @param rule the rule whose debt it records
 * @param file the ledger's file name, on the gate's classpath
 */
public record RuleLedger(RuleId rule, String file) {

    /** The fingerprints recorded; none when there is no ledger beside the gate. */
    public Baseline read(Class<?> anchor) {
        try (InputStream in = anchor.getClassLoader().getResourceAsStream(file)) {
            if (in == null) return Baseline.EMPTY;
            return Baseline.of(new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList());
        } catch (IOException e) {
            throw new UncheckedIOException(file, e);
        }
    }

    /**
     * The gate's grader: strict, but for this rule's recorded findings - warnings, unless pre-existing
     * debt is made to fail ({@code -Dconformance.allowPreExisting=false}).
     */
    public FindingGrader grader(Class<?> anchor) {
        return new FindingGrader(List.of(), read(anchor), allowPreExisting());
    }

    /** A gate's own grader, with this rule's recorded findings let through besides its own allowances and baseline. */
    public FindingGrader over(FindingGrader own, Class<?> anchor) {
        var both = new java.util.TreeSet<>(own.baseline().fingerprints());
        both.addAll(read(anchor).fingerprints());
        return new FindingGrader(own.allowlist(), new Baseline(both), allowPreExisting());
    }

    private static boolean allowPreExisting() {
        return Boolean.parseBoolean(System.getProperty("conformance.allowPreExisting", "true"));
    }

    /** The recorded fingerprints no finding matches any more: fixed, or gone - to be struck from the ledger. */
    public List<String> stale(Class<?> anchor, List<Finding> raw) {
        Set<String> live = raw.stream().map(Finding::fingerprint).collect(Collectors.toSet());
        return read(anchor).fingerprints().stream().filter(fp -> !live.contains(fp)).sorted().toList();
    }

    /** Under {@code -Dconformance.recordLedger=true}, this rule's findings now, written as the ledger in the test resources; nothing otherwise. */
    public void recordIfAsked(List<Finding> raw) { recordIfAsked(raw, "src/test/resources"); }

    /**
     * Under {@code -Dconformance.recordLedger=true}, this rule's findings now, written as the ledger
     * in the given resources folder of the module under test - {@code src/main/resources} for a ledger
     * a build-time export reads too; nothing otherwise.
     */
    public void recordIfAsked(List<Finding> raw, String resources) {
        if (!Boolean.getBoolean("conformance.recordLedger")) return;
        var debt = Baseline.record(raw.stream().filter(f -> f.rule().equals(rule)).toList());
        var lines = new ArrayList<String>();
        lines.add("# " + rule.value() + ": the debt recorded when the rule came. Only shrinks; regenerate deliberately.");
        lines.addAll(debt);
        try {
            Path to = Path.of(resources, file);
            if (debt.isEmpty()) {
                if (Files.deleteIfExists(to)) System.out.println("[ledger] no debt of " + rule.value() + " left: " + to.toAbsolutePath() + " removed");
                return;
            }
            Files.createDirectories(to.getParent());
            Files.write(to, lines, StandardCharsets.UTF_8);
            System.out.println("[ledger] " + (lines.size() - 1) + " finding(s) of " + rule.value() + " recorded in " + to.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(file, e);
        }
    }
}
