package hue.captains.singapura.js.homing.design.styling;

/**
 * A derived sheet made from the styling of the site serving it: one empty value, the address a page loads it by, handed
 * the styling by whatever renders it ({@link StylingRenderer}).
 */
public interface StyledSheet {

    /** The whole sheet under a slug, as this styling makes it: a slug the styling does not serve is refused. */
    String css(Styling styling, String slug);

    /** What this styling serves the sheet under until the address or a pick says otherwise. */
    String fallback(Styling styling);
}
