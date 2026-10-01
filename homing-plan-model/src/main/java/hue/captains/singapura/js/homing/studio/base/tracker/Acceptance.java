package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * One acceptance criterion for a {@link Plan} — the "outcome" pillar of the
 * Plans-as-Living-Containers doctrine (RFC 0005-ext1). A plan-level statement
 * of what success means; the {@link #met()} flag flips to true as evidence
 * accumulates from the plan's phases.
 *
 * <p>The {@link #description()} is free prose; it may mention phase IDs and
 * decision IDs by reference (e.g. {@code "Driven by phase 03's metrics; closes D7."}) —
 * the same identity-by-string mechanism existing trackers already use within phase
 * notes. A future RFC may promote those cross-references to typed fields if a
 * mechanical query surface emerges.</p>
 *
 * @param label        short title (e.g. {@code "All trackers migrated"})
 * @param description  longer statement; may cite phase IDs / decision IDs in prose
 * @param met          {@code true} once the criterion is satisfied
 *
 * @since RFC 0005-ext1
 */
public record Acceptance(String label, String description, boolean met) implements ValueObject {}
