package hue.captains.singapura.js.homing.studio.base.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.UtilityCssClass;

import java.util.List;

/**
 * RFC 0002-ext1 Phase 06 — utility CssGroup.
 *
 * <p>A small, hand-curated set of single-property utility classes that
 * compose at the JS call site (Tailwind-style). Two flavors:</p>
 *
 * <ul>
 *   <li><b>Color/visual base utilities</b> — implement {@link UtilityCssClass}
 *       to opt into auto-generated {@code hover}/{@code focus}/{@code active}
 *       variants. Bodies reference semantic tokens
 *       (e.g. {@code var(--color-accent)}) that the active theme's
 *       the palette provides.</li>
 *   <li><b>Layout / structural utilities</b> — plain {@link CssClass} with
 *       no variants. Bodies reference the spacing scale or use universal
 *       CSS keywords.</li>
 * </ul>
 *
 * <p>Theme-independence comes for free because every body resolves through
 * tokens the active theme defines — the global palette, the prior every class
 * reaches (RFC 0066). Every record carries its own {@code body()}; no theme
 * has had a word on a utility yet.</p>
 */
public record Util() implements CssGroup<Util> {

    public static final Util INSTANCE = new Util();

    // RFC 0066 — not a prior: utilities lean on nothing, and nothing needs them
    // first. Only the global palette is prior; a group that needs a utility
    // present names it in dependsOn().

    // -------------------------------------------------------------------
    // Color / visual base utilities — UtilityCssClass opts into all three
    // hover/focus/active variants automatically.
    // -------------------------------------------------------------------

    public record bg_accent() implements UtilityCssClass<Util> {
        @Override public String body() { return "background: var(--color-accent);"; }
    }
    public record bg_accent_emphasis() implements UtilityCssClass<Util> {
        @Override public String body() { return "background: var(--color-accent-emphasis);"; }
    }
    public record bg_surface_raised() implements UtilityCssClass<Util> {
        @Override public String body() { return "background: var(--color-surface-raised);"; }
    }
    public record color_link() implements UtilityCssClass<Util> {
        @Override public String body() { return "color: var(--color-text-link);"; }
    }
    public record color_link_emphasis() implements UtilityCssClass<Util> {
        @Override public String body() { return "color: var(--color-text-link-hover);"; }
    }
    public record border_emphasis() implements UtilityCssClass<Util> {
        @Override public String body() { return "border-color: var(--color-border-emphasis);"; }
    }
    public record translate_y_neg_2() implements UtilityCssClass<Util> {
        @Override public String body() { return "transform: translateY(-2px);"; }
    }
    public record shadow_lg() implements UtilityCssClass<Util> {
        @Override public String body() { return "box-shadow: 0 6px 16px rgba(0, 0, 0, 0.10);"; }
    }

    // -------------------------------------------------------------------
    // Spacing scale utilities — plain CssClass, no variants.
    // Reference --space-1 through --space-8 from the theme's semantic
    // token layer (Phase 04).
    // -------------------------------------------------------------------

    public record p_1() implements CssClass<Util> { @Override public String body() { return "padding: var(--space-1);"; } }
    public record p_2() implements CssClass<Util> { @Override public String body() { return "padding: var(--space-2);"; } }
    public record p_3() implements CssClass<Util> { @Override public String body() { return "padding: var(--space-3);"; } }
    public record p_4() implements CssClass<Util> { @Override public String body() { return "padding: var(--space-4);"; } }
    public record p_5() implements CssClass<Util> { @Override public String body() { return "padding: var(--space-5);"; } }
    public record p_6() implements CssClass<Util> { @Override public String body() { return "padding: var(--space-6);"; } }
    public record p_7() implements CssClass<Util> { @Override public String body() { return "padding: var(--space-7);"; } }
    public record p_8() implements CssClass<Util> { @Override public String body() { return "padding: var(--space-8);"; } }

    public record m_1() implements CssClass<Util> { @Override public String body() { return "margin: var(--space-1);"; } }
    public record m_2() implements CssClass<Util> { @Override public String body() { return "margin: var(--space-2);"; } }
    public record m_3() implements CssClass<Util> { @Override public String body() { return "margin: var(--space-3);"; } }
    public record m_4() implements CssClass<Util> { @Override public String body() { return "margin: var(--space-4);"; } }
    public record m_5() implements CssClass<Util> { @Override public String body() { return "margin: var(--space-5);"; } }
    public record m_6() implements CssClass<Util> { @Override public String body() { return "margin: var(--space-6);"; } }
    public record m_7() implements CssClass<Util> { @Override public String body() { return "margin: var(--space-7);"; } }
    public record m_8() implements CssClass<Util> { @Override public String body() { return "margin: var(--space-8);"; } }

    public record gap_1() implements CssClass<Util> { @Override public String body() { return "gap: var(--space-1);"; } }
    public record gap_2() implements CssClass<Util> { @Override public String body() { return "gap: var(--space-2);"; } }
    public record gap_3() implements CssClass<Util> { @Override public String body() { return "gap: var(--space-3);"; } }
    public record gap_4() implements CssClass<Util> { @Override public String body() { return "gap: var(--space-4);"; } }
    public record gap_5() implements CssClass<Util> { @Override public String body() { return "gap: var(--space-5);"; } }
    public record gap_6() implements CssClass<Util> { @Override public String body() { return "gap: var(--space-6);"; } }

    // -------------------------------------------------------------------
    // Display + alignment utilities — plain CssClass, no variants.
    // Universal CSS keywords; theme-independent.
    // -------------------------------------------------------------------

    public record flex()             implements CssClass<Util> { @Override public String body() { return "display: flex;"; } }
    public record grid()             implements CssClass<Util> { @Override public String body() { return "display: grid;"; } }
    public record inline_flex()      implements CssClass<Util> { @Override public String body() { return "display: inline-flex;"; } }
    public record items_center()     implements CssClass<Util> { @Override public String body() { return "align-items: center;"; } }
    public record items_start()      implements CssClass<Util> { @Override public String body() { return "align-items: flex-start;"; } }
    public record items_end()        implements CssClass<Util> { @Override public String body() { return "align-items: flex-end;"; } }
    public record justify_center()   implements CssClass<Util> { @Override public String body() { return "justify-content: center;"; } }
    public record justify_between()  implements CssClass<Util> { @Override public String body() { return "justify-content: space-between;"; } }
    public record text_center()      implements CssClass<Util> { @Override public String body() { return "text-align: center;"; } }

    // -------------------------------------------------------------------
    // Group identity
    // -------------------------------------------------------------------

    @Override
    public List<CssClass<Util>> cssClasses() {
        return List.of(
                // Color / visual bases (with hover/focus/active variants)
                new bg_accent(), new bg_accent_emphasis(), new bg_surface_raised(),
                new color_link(), new color_link_emphasis(),
                new border_emphasis(),
                new translate_y_neg_2(), new shadow_lg(),
                // Spacing scale
                new p_1(), new p_2(), new p_3(), new p_4(), new p_5(), new p_6(), new p_7(), new p_8(),
                new m_1(), new m_2(), new m_3(), new m_4(), new m_5(), new m_6(), new m_7(), new m_8(),
                new gap_1(), new gap_2(), new gap_3(), new gap_4(), new gap_5(), new gap_6(),
                // Display + alignment
                new flex(), new grid(), new inline_flex(),
                new items_center(), new items_start(), new items_end(),
                new justify_center(), new justify_between(),
                new text_center()
        );
    }
}
