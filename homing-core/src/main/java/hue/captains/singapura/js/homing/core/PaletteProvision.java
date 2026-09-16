package hue.captains.singapura.js.homing.core;

import java.util.Map;

/**
 * RFC 0066 — a theme's body for a {@link PaletteClass}: the value it binds to
 * every token the palette declares. Registered like any {@link CssGroupImpl},
 * resolved by {@code (group, theme.slug)}, and rendered as one
 * {@code :root { … }} block in place of the palette class's rule.
 *
 * <p>Hardcoded for now — a map per theme, as the retired {@code ThemeVariables}
 * held it. Episode 2 of the RFC generates the map from a semantic tree and
 * reusable providers, in every mode the theme claims; nothing that reads this
 * interface changes when it does.</p>
 *
 * <p>Example, the studio's global palette under one theme:</p>
 * <pre>{@code
 * public record Palette() implements GlobalColorPalette.Provision<HomingForest> {
 *     public static final Palette INSTANCE = new Palette();
 *     @Override public HomingForest theme() { return HomingForest.INSTANCE; }
 *     @Override public Map<CssVar, String> values() { return VALUES; }
 *     private static final Map<CssVar, String> VALUES = Map.ofEntries(…);
 * }
 * }</pre>
 *
 * @param <G>  the group holding the palette this provision fills
 * @param <TH> the theme it fills it for
 */
public interface PaletteProvision<G extends CssGroup<G>, TH extends Theme> extends CssGroupImpl<G, TH> {

    /** Every token bound, keyed by the typed {@link CssVar}. */
    Map<CssVar, String> values();

    /**
     * The served form: one {@code :root { … }} block, a declaration per token,
     * in the map's iteration order. Empty values render an empty string so a
     * theme that binds nothing serves nothing rather than an empty block.
     */
    default String rootBlock() {
        Map<CssVar, String> values = values();
        if (values.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(":root {\n");
        for (Map.Entry<CssVar, String> e : values.entrySet()) {
            sb.append("    ").append(e.getKey().name()).append(": ").append(e.getValue()).append(";\n");
        }
        return sb.append("}\n").toString();
    }
}
