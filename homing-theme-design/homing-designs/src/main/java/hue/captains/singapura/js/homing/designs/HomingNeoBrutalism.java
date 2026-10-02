package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;


/**
 * Brutalist theme — riso-print order form: ink on paper, a grid behind
 * everything, hard 4px rules, zero radius, solid offset shadows, and one
 * yellow ({@code #FFE800}) that shouts. Blue ({@code #2B4CFF}) for links,
 * red ({@code #FF3B21}) for hover and the destructive edge.
 *
 * <p>Built to find out how far a theme can go on CSS alone. Every rule is
 * as a per-class override (RFC 0066) and touches only classes the studio
 * already renders — no module, no markup, no script:</p>
 *
 * <ul>
 *   <li><b>Press-into-shadow.</b> Cards, list rows, buttons and dialog
 *       actions carry a solid offset shadow; {@code :active} translates the
 *       element by the shadow's offset and zeroes the shadow, with a
 *       {@code steps(2)} transition so it snaps rather than eases. Hover
 *       lifts the other way.</li>
 *   <li><b>Stamped labels.</b> The kicker becomes the rotated yellow box;
 *       section, panel and sidebar titles become inverted ink tags; the
 *       page title goes Arial Black, uppercase, tight.</li>
 *   <li><b>Loud focus.</b> The search field inverts to yellow and grows a
 *       shadow on focus; every button gets a 4px blue outline on
 *       {@code :focus-visible}. Affordance is kept and amplified, not
 *       stripped.</li>
 *   <li><b>Hatching for the inert.</b> Disabled actions and the dialog scrim
 *       use a 45° repeating gradient; progress fills are hatched ink on
 *       yellow.</li>
 * </ul>
 *
 * <p>What CSS could not reach is recorded where it was found: the shared
 * tree's rows (the picker's list, the catalogue tree) are inline-styled by
 * {@code TreeRendererModule} and keep their soft selection tint under every
 * theme. Everything else on the page follows.</p>
 *
 * <p>Dark mode swaps ink and paper — the shadows go white, the yellow stays.</p>
 */
public record HomingNeoBrutalism() implements Design {

    public static final DesignId ID = new DesignId("neo-brutalism");
    public static final HomingNeoBrutalism INSTANCE = new HomingNeoBrutalism();

    /** Over Default: its own word where it has one, Default's for the rest — the base is a plain call. */
    @Override public Impl impl(DesignClass<?> pair) {
        Impl own = NeoBrutalismDesign.WORDS.get(pair);
        return own != null ? own : HomingEditorial.INSTANCE.impl(pair);
    }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Neo-Brutalism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "A riso-print order form — ink rules, offset shadows, one loud yellow."; }
}
