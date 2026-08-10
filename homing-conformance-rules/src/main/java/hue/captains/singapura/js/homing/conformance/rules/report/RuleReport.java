package hue.captains.singapura.js.homing.conformance.rules.report;

/**
 * RFC 0044 — one rule in a {@link RuleSetReport}, in exportable form: the rule's
 * stable {@code id} (slug) and its one-line {@code intent} (the invariant it
 * defends). A report is data, so this is a flat string record — serialized by
 * the polyglot codec and rendered as the foldable rule list in the studio's
 * Conformance Report pane.
 *
 * @param id     the rule's slug (RuleId value)
 * @param intent the one-line invariant the rule defends
 */
public record RuleReport(String id, String intent) {}
