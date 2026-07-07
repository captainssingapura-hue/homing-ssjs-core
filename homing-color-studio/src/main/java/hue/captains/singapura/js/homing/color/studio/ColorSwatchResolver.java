package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.color.semantic.Degree;
import hue.captains.singapura.js.homing.color.semantic.PhysicalColor;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.SemanticColorResolver;
import hue.captains.singapura.js.homing.color.semantic.affective.Cheerful;
import hue.captains.singapura.js.homing.color.semantic.affective.Neutral;
import hue.captains.singapura.js.homing.color.semantic.affective.Serene;
import hue.captains.singapura.js.homing.color.semantic.affective.Somber;
import hue.captains.singapura.js.homing.color.semantic.affective.Tense;
import hue.captains.singapura.js.homing.color.semantic.identity.Brand;
import hue.captains.singapura.js.homing.color.semantic.identity.BrandEmphasis;
import hue.captains.singapura.js.homing.color.semantic.structural.Border;
import hue.captains.singapura.js.homing.color.semantic.structural.BorderEmphasis;
import hue.captains.singapura.js.homing.color.semantic.structural.Surface;
import hue.captains.singapura.js.homing.color.semantic.structural.SurfaceInverted;
import hue.captains.singapura.js.homing.color.semantic.structural.SurfaceRaised;
import hue.captains.singapura.js.homing.color.semantic.structural.SurfaceRecessed;
import hue.captains.singapura.js.homing.color.semantic.structural.Text;
import hue.captains.singapura.js.homing.color.semantic.structural.TextMuted;
import hue.captains.singapura.js.homing.color.semantic.symbolic.Danger;
import hue.captains.singapura.js.homing.color.semantic.symbolic.Info;
import hue.captains.singapura.js.homing.color.semantic.symbolic.Success;
import hue.captains.singapura.js.homing.color.semantic.symbolic.Warning;
import hue.captains.singapura.js.homing.studio.base.theme.StudioVars;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RFC 0037 seam, V1 — the first <em>real</em> {@link SemanticColorResolver},
 * replacing {@code PlaceholderResolver}'s {@code "name@n/d"} stubs with actual
 * CSS colour strings. Two realisation strategies, split by group nature:
 *
 * <ul>
 *   <li><b>Structural + Identity</b> ({@link Surface}, {@link Text},
 *       {@link Border}, {@link Brand}, …) map onto the framework's live theme
 *       roles ({@link StudioVars}) and resolve to the <em>CSS variable
 *       reference</em> — e.g. {@code var(--color-accent)}. A swatch painted with
 *       this value follows whatever theme is active, with no per-theme plumbing
 *       (the workspace page already loads {@code /theme-vars}).</li>
 *   <li><b>Affective + Symbolic</b> ({@link Serene}, {@link Danger}, …) have no
 *       theme role, so they carry curated anchor hexes — a valence×arousal /
 *       signal-convention starting palette. These are the natural first target
 *       for the deferred OKLCH {@link Degree} ramp math.</li>
 * </ul>
 *
 * <p>{@code Degree} is accepted but not yet consumed — a single anchor per
 * colour for V1. When the interpolation math lands it changes here alone, with
 * no call-site change, exactly as the resolver seam was designed for.</p>
 */
public final class ColorSwatchResolver implements SemanticColorResolver {

    public static final ColorSwatchResolver INSTANCE = new ColorSwatchResolver();

    /** Every colour leaf → its V1 CSS colour string (theme var ref or anchor hex). */
    private static final Map<SemanticColor, String> SWATCH = buildSwatchMap();

    private ColorSwatchResolver() {}

    private static Map<SemanticColor, String> buildSwatchMap() {
        var m = new LinkedHashMap<SemanticColor, String>();

        // ── Structural → live theme surface/text/border roles ───────────────
        m.put(Surface.INSTANCE,         StudioVars.COLOR_SURFACE.ref());
        m.put(SurfaceRaised.INSTANCE,   StudioVars.COLOR_SURFACE_RAISED.ref());
        m.put(SurfaceRecessed.INSTANCE, StudioVars.COLOR_SURFACE_RECESSED.ref());
        m.put(SurfaceInverted.INSTANCE, StudioVars.COLOR_SURFACE_INVERTED.ref());
        m.put(Text.INSTANCE,            StudioVars.COLOR_TEXT_PRIMARY.ref());
        m.put(TextMuted.INSTANCE,       StudioVars.COLOR_TEXT_MUTED.ref());
        m.put(Border.INSTANCE,          StudioVars.COLOR_BORDER.ref());
        m.put(BorderEmphasis.INSTANCE,  StudioVars.COLOR_BORDER_EMPHASIS.ref());

        // ── Identity → the theme's accent (its "signature hue") ─────────────
        m.put(Brand.INSTANCE,           StudioVars.COLOR_ACCENT.ref());
        m.put(BrandEmphasis.INSTANCE,   StudioVars.COLOR_ACCENT_EMPHASIS.ref());

        // ── Affective → curated anchors on the valence×arousal plane ────────
        m.put(Serene.INSTANCE,   "#6CB4A4");   // high valence, low arousal — calm sea-green
        m.put(Cheerful.INSTANCE, "#F5C542");   // high valence, high arousal — warm gold
        m.put(Tense.INSTANCE,    "#E4572E");   // low valence, high arousal — hot red-orange
        m.put(Somber.INSTANCE,   "#4A5568");   // low valence, low arousal — muted slate
        m.put(Neutral.INSTANCE,  "#9AA5B1");   // centre of the plane — balanced grey

        // ── Symbolic → learned signal colours ───────────────────────────────
        m.put(Danger.INSTANCE,   "#D64545");   // harm / error / stop
        m.put(Success.INSTANCE,  "#3E9E5B");   // ok / complete / go
        m.put(Warning.INSTANCE,  "#E8A93A");   // caution
        m.put(Info.INSTANCE,     "#3B82C4");   // neutral notice

        return Map.copyOf(m);
    }

    @Override
    public PhysicalColor resolve(SemanticColor semantic, Degree degree) {
        // V1: one anchor per colour; Degree is not yet sampled. Unknown colours
        // fall back to muted text so a swatch always paints something visible.
        String value = SWATCH.getOrDefault(semantic, StudioVars.COLOR_TEXT_MUTED.ref());
        return new PhysicalColor(value);
    }

    /**
     * The full colour vocabulary as a {@code name → CSS colour} map, in taxonomy
     * order — the payload {@code GET /color-swatches} serves and the
     * {@code ColorListWidget} looks colours up in. Keyed by
     * {@link SemanticColor#name()} (the same identifier the tree's
     * {@code displayLabel} carries), so the widget joins swatches to tree nodes
     * by name with no extra wiring.
     */
    public Map<String, String> swatchesByName() {
        var out = new LinkedHashMap<String, String>();
        for (var e : SWATCH.entrySet()) {
            out.put(e.getKey().name(), e.getValue());
        }
        return out;
    }
}
