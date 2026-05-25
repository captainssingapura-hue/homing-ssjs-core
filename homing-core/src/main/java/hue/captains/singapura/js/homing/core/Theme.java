package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A typed theme — server-side identity token used as a lookup key when
 * resolving the {@link CssGroupImpl} for a {@link CssGroup}.
 *
 * <p>Each implementing record is a stateless singleton. Themes don't extend
 * {@code Exportable} (they're never JS-module exports) and don't use the
 * F-bound pattern (they have no self-returning methods — see RFC 0002 §3.1).
 *
 * <p>Example:
 * <pre>{@code
 * public record HomingDefault() implements Theme {
 *     public static final HomingDefault INSTANCE = new HomingDefault();
 *     @Override public String slug()  { return "homing-default"; }
 *     @Override public String label() { return "Homing default"; }
 * }
 * }</pre>
 *
 * @see CssGroupImpl
 * @see <a href="../../../../../../../../docs/rfcs/0002-typed-themes-for-cssgroups.md">RFC 0002 — Typed Themes for CssGroups</a>
 */
public interface Theme extends StatelessFunctionalObject {

    /**
     * URL/filename slug. Stable, kebab-case. Used as the {@code theme=…}
     * query-string parameter value when the browser fetches a themed CSS
     * stylesheet.
     */
    String slug();

    /**
     * Optional human-readable name. Defaults to {@link #slug()} when not
     * overridden.
     */
    default String label() { return slug(); }

    /**
     * Optional atmospheric backdrop — a typed {@link SvgRef} pointing at an
     * SVG asset that the framework renders as <i>inline DOM</i> behind the
     * studio chrome, on every page, when this theme is active.
     *
     * <p>Returning a non-null {@code SvgRef} causes
     * {@code AppHtmlGetAction} to embed the resolved SVG markup as the first
     * child of {@code <body>}, wrapped in {@code <div class="theme-backdrop">}.
     * Because the SVG is real DOM (not a {@code background-image} sandbox),
     * its individual elements participate in the host document's CSS cascade
     * — themes can add per-element {@code :hover} animations, transitions,
     * pointer-event handlers, etc.</p>
     *
     * <p>Default {@code null} → no backdrop. Most themes return null and
     * tint the chrome via CSS variables alone. Atmospheric themes
     * ({@code HomingMapleBridge} is the framework's first) return an
     * {@code SvgRef} pointing at an inline SVG illustration.</p>
     */
    default SvgRef<?> backdrop() { return null; }

    /**
     * Optional theme-audio binding — a {@link ThemeAudio} that maps
     * typed {@link ClickTarget}s on this theme's surface to typed
     * {@link Cue}s. RFC 0007.
     *
     * <p>Returning non-null causes the framework to:</p>
     * <ol>
     *   <li>Import the {@code ToneJs} bundled module on every page where
     *       this theme is active.</li>
     *   <li>Generate a small ES module containing this binding as a JS
     *       object literal.</li>
     *   <li>Inject a delegated click listener (the theme-audio runtime)
     *       that plays the bound cue when the user clicks an element
     *       carrying a target's {@link ClickTarget#classToken()}.</li>
     * </ol>
     *
     * <p>Default {@code null} → no audio cues. Tone.js stays out of the
     * page bundle entirely on audio-less themes.</p>
     */
    default ThemeAudio<?> audio() { return null; }

    /**
     * Whether this theme installs <i>backdrop event handlers</i> — typically
     * implemented as universal {@code body, body * { pointer-events: none }}
     * with a selective whitelist of interactive elements (forms, framework
     * chrome classes) plus the backdrop's own clickable parts (e.g.
     * {@code .mb-moon} on Maple Bridge, drum/instrument classes on
     * Jazz Drums, retro icons on Retro 90s).
     *
     * <p>Themes returning {@code true} <b>must wrap</b> their universal
     * pointer-events restriction in a {@code body:not(.homing-bg-passive)}
     * selector. Apps that opt out via
     * {@link AppModule#acceptsBackdropInteractivity()} get the
     * {@code homing-bg-passive} body class added by the framework, which
     * disables the restriction for that app's body. The backdrop SVG stays
     * visible — only its event handling is suppressed.</p>
     *
     * <p>Default {@code false} → theme doesn't manage page-wide
     * pointer-events; the {@code homing-bg-passive} class has no effect
     * because there's no rule referencing it.</p>
     *
     * @since RFC 0025 L2.2 — added when MultiTabPane's drag handlers ran
     *        into Maple Bridge's universal {@code pointer-events: none}
     *        whitelist (which didn't include the new primitive's classes)
     */
    default boolean backdropInteractivity() { return false; }
}
