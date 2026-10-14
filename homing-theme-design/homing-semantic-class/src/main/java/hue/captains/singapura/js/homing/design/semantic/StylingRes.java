package hue.captains.singapura.js.homing.design.semantic;

/**
 * What a style does with a leaf (RFC 0067, appendix Two Classes per Leaf, D25): a style is a function giving a design
 * for each leaf, and this is its answer, typed, so what a sheet holds is read off the type - never off a file found or
 * missing.
 *
 * <ul>
 *   <li>{@link CssContent} - the leaf's look as the style writes it: the style's own file for the leaf (D9);</li>
 *   <li>{@link Absence} - nothing of its own: the base layer's content is used.</li>
 * </ul>
 *
 * The base layer - a style that derives from nothing - is never absent: it answers content for every leaf, an
 * extension's included (D26). Delegation, an explicit reuse of another style's design, comes later (D27): for now no
 * style delegates, and a design two styles share is written in each.
 */
public sealed interface StylingRes permits StylingRes.CssContent, StylingRes.Absence {

    /** The leaf's look as the style writes it, in the style's own file for the leaf. */
    record CssContent() implements StylingRes {
        public static final CssContent INSTANCE = new CssContent();
    }

    /** Nothing of the style's own: the base layer's content is used. */
    record Absence() implements StylingRes {
        public static final Absence INSTANCE = new Absence();
    }
}
