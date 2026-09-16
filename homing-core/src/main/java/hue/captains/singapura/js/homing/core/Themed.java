package hue.captains.singapura.js.homing.core;

import java.util.Set;

/**
 * RFC 0002-ext1 Phase 09 — declares that a CssClass depends on theme-provided
 * CSS variables, and optionally varies its body per theme.
 *
 * <p>Three categories of CssClass cleanly:</p>
 * <ul>
 *   <li><b>Truly theme-agnostic</b> — implements {@link CssClass} only. Body is a
 *       constant string with no {@code var(--…)} references. Universal CSS keywords.
 *       Examples: {@code flex}, {@code grid}, {@code items_center}.</li>
 *   <li><b>Var-dependent (most common themed)</b> — implements {@link Themed}. Body is
 *       still a constant string, but uses {@code var(--token)} references. Theme
 *       variation enters through the cascade at the variable layer. Default
 *       {@link #bodyFor(Theme)} returns the constant {@link #body()} unchanged.</li>
 *   <li><b>Structurally per-theme (rare)</b> — implements {@link Themed} and overrides
 *       {@link #bodyFor(Theme)} to dispatch on the active theme. Use only when token
 *       remapping cannot express the variation (e.g. different layout per theme).</li>
 * </ul>
 *
 * <p>Build-time conformance (RFC 0002-ext1 §3.6.6):</p>
 * <ol>
 *   <li>Every {@code var(--…)} reference parsed from {@link #body()} must correspond
 *       to a {@code CssVar} declared in {@link #requiredVars()}.</li>
 *   <li>Every {@code CssVar} in any active class's {@code requiredVars()} must be
 *       provided by every registered {@link PaletteProvision}.</li>
 * </ol>
 *
 * @param <G> the CssGroup this class belongs to
 */
public interface Themed<G extends CssGroup<G>> extends CssClass<G> {

    /**
     * The CSS variables this class depends on, resolved by the active theme's
     * {@link PaletteProvision} via the cascade.
     *
     * <p>This is the canonical machine-readable contract — the body string may
     * also reference these vars textually, but the {@code requiredVars()} set
     * is what conformance tests and theme-vocabulary checks consult.</p>
     */
    Set<CssVar> requiredVars();

    /**
     * Per-theme body. Default returns the constant {@link #body()} (which may use
     * var refs that the theme cascade resolves). Override only for the rare case
     * of genuinely different body strings per theme.
     */
    default String bodyFor(Theme theme) { return body(); }
}
