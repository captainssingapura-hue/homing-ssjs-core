package hue.captains.singapura.js.homing.conformance.rules;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * RFC 0044 Phase 7 — the set of <b>pre-existing</b> violations, grandfathered so
 * the rules can be maximally strict without the accumulated debt breaking the
 * build on day one. A finding whose {@link Finding#fingerprint()} is in the
 * baseline is treated as legacy (a warning, subject to the global
 * allow-pre-existing switch); anything <em>not</em> in the baseline is a NEW
 * violation and fails the build. This is a ratchet: the debt can only shrink.
 *
 * <p>The baseline is a plain list of fingerprints (one per line; blank lines and
 * {@code #} comments ignored), recorded from the current engine output and
 * committed as a reviewable ledger. Regenerate it deliberately — never to
 * silence a fresh violation.</p>
 */
public record Baseline(Set<String> fingerprints) {

    public static final Baseline EMPTY = new Baseline(Set.of());

    public Baseline {
        fingerprints = Set.copyOf(fingerprints);
    }

    /** Parse a baseline from its line form (fingerprint per line; blanks / {@code #} comments skipped). */
    public static Baseline of(Collection<String> lines) {
        var out = new TreeSet<String>();
        for (String raw : lines) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            out.add(line);
        }
        return new Baseline(out);
    }

    /** True iff this exact violation (module + rule + offending code) was recorded. */
    public boolean contains(Finding finding) {
        return fingerprints.contains(finding.fingerprint());
    }

    public boolean isEmpty() { return fingerprints.isEmpty(); }
    public int size()        { return fingerprints.size(); }

    /** The sorted line form of a set of findings — the content to record as a baseline file. */
    public static List<String> record(Collection<Finding> findings) {
        var out = new TreeSet<String>();
        for (Finding f : findings) out.add(f.fingerprint());
        return List.copyOf(out);
    }
}
