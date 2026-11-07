package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.tao.ontology.Scalar;

import java.util.List;
import java.util.Set;

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
public record Baseline(Set<String> fingerprints) implements Scalar {

    public static final Baseline EMPTY = new Baseline(Set.of());

    public Baseline {
        fingerprints = Set.copyOf(fingerprints);
    }

    /** Parse a baseline from its line form (fingerprint per line; blanks / {@code #} comments skipped). */
    public Baseline(List<String> lines) {
        this(lines.stream().map(String::strip).filter(line -> !line.isEmpty() && !line.startsWith("#")).collect(java.util.stream.Collectors.toSet()));
    }

    /** True iff this exact violation (module + rule + offending code) was recorded. */
    public boolean contains(Finding finding) {
        return fingerprints.contains(finding.fingerprint());
    }

    /** Whether nothing is grandfathered. */
    public boolean isEmpty() { return fingerprints.isEmpty(); }
    /** How many findings are grandfathered. */
    public int size()        { return fingerprints.size(); }
}
