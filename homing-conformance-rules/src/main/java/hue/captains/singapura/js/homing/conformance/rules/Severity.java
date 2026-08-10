package hue.captains.singapura.js.homing.conformance.rules;

/**
 * RFC 0044 Phase 7 — the weight of a {@link Finding}. Only {@link #ERROR} fails
 * the build; {@link #WARNING} is surfaced (studio + build log) but non-fatal.
 * Newly-ported rules start as warnings so porting can't break the build; a rule
 * is promoted to error once the codebase is clean for it. An allowlisted
 * exception is always a warning (documented, visible), never a silent pass.
 */
public enum Severity {
    WARNING,
    ERROR
}
