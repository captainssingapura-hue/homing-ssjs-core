package hue.captains.singapura.js.homing.core;

import java.util.Map;

/**
 * RFC 0066 — a theme's body for a {@link PaletteClass}: the value it binds to
 * every token the palette declares, in each mode it claims. Registered like
 * any {@link CssGroupImpl}, resolved by {@code (group, theme.slug)}, and
 * rendered as the {@code :root} blocks in place of the palette class's rule.
 *
 * <p><b>Modes.</b> {@link #values()} is the light binding and the one that must
 * be complete. {@link #darkValues()} is the re-binding under
 * {@code prefers-color-scheme: dark} — a theme that has one binds the same
 * tokens again (the completeness gate reads both); a theme that has none is
 * single-scheme, and says which with {@link #colorScheme()}. RFC 0056 found
 * nine dark palettes written as raw strings nobody could test or show; here
 * they are a map like the light one, served from the same node, and the
 * page's {@code color-scheme} is declared from the same fact.</p>
 *
 * <p>Hardcoded for now — a map per theme per mode. Episode 2 of the RFC
 * generates the maps from a semantic tree and reusable providers; nothing
 * that reads this interface changes when it does.</p>
 *
 * <p>Example, the studio's global palette under one theme:</p>
 * <pre>{@code
 * public record Palette() implements GlobalColorPalette.Provision<HomingForest> {
 *     public static final Palette INSTANCE = new Palette();
 *     @Override public HomingForest theme() { return HomingForest.INSTANCE; }
 *     @Override public Map<CssVar, String> values()     { return LIGHT; }
 *     @Override public Map<CssVar, String> darkValues() { return DARK; }
 * }
 * }</pre>
 *
 * @param <G>  the group holding the palette this provision fills
 * @param <TH> the theme it fills it for
 */
public interface PaletteProvision<G extends CssGroup<G>, TH extends Theme> extends CssGroupImpl<G, TH> {

    /** Every token bound, keyed by the typed {@link CssVar} — the light binding. */
    Map<CssVar, String> values();

    /**
     * The re-binding under {@code prefers-color-scheme: dark}. Empty (the
     * default) means the theme is single-scheme: {@link #values()} stands in
     * both modes.
     */
    default Map<CssVar, String> darkValues() { return Map.of(); }

    /**
     * What the page tells the browser it is — scrollbars, form controls, the
     * canvas behind the document follow it. {@code "light dark"} when a dark
     * binding exists, {@code "light"} otherwise; a theme that is dark in every
     * mode (Carbon, Turbo C) returns {@code "dark"}.
     */
    default String colorScheme() { return darkValues().isEmpty() ? "light" : "light dark"; }

    /**
     * The served form: {@code :root { color-scheme; light tokens }}, then, when
     * a dark binding exists, the same under {@code @media (prefers-color-scheme:
     * dark)}. Map iteration order; an empty light binding serves nothing.
     */
    default String rootBlock() {
        Map<CssVar, String> light = values();
        if (light.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(":root {\n    color-scheme: ").append(colorScheme()).append(";\n");
        declarations(sb, light, "    ");
        sb.append("}\n");
        Map<CssVar, String> dark = darkValues();
        if (!dark.isEmpty()) {
            sb.append("@media (prefers-color-scheme: dark) {\n    :root {\n");
            declarations(sb, dark, "        ");
            sb.append("    }\n}\n");
        }
        return sb.toString();
    }

    private static void declarations(StringBuilder sb, Map<CssVar, String> values, String indent) {
        for (Map.Entry<CssVar, String> e : values.entrySet()) {
            sb.append(indent).append(e.getKey().name()).append(": ").append(e.getValue()).append(";\n");
        }
    }
}
