package hue.captains.singapura.js.homing.conformance.rules;

/**
 * RFC 0044 Phase 7/8 — why a {@link Finding} got the {@link Severity} it did, as
 * a closed, enumerable set (so the exported report and the studio can group and
 * colour findings by provenance, not just by pass/fail).
 */
public enum Disposition {

    /** A documented, intentional exception (an {@link Allowance}) — always a warning. */
    ALLOWED("Allowed", "allowed"),
    /** Grandfathered legacy debt in the committed baseline — a warning while pre-existing is allowed. */
    PRE_EXISTING("Pre-existing", "pre-existing"),
    /** A fresh violation — neither allowed nor baselined — always an error. */
    NEW("New", "new");

    private final String label;
    private final String slug;

    Disposition(String label, String slug) {
        this.label = label;
        this.slug  = slug;
    }

    public String label() { return label; }
    public String slug()  { return slug; }
}
