package hue.captains.singapura.js.homing.design;

import java.util.regex.Pattern;

/**
 * A design's identity, held without the design: what a registry keys on,
 * what a page's {@code ?theme=} names, what a palette points at when it says
 * which design it was crafted for. A design declares its own as a constant —
 * {@code public static final DesignId ID = new DesignId("sketchy")} — and
 * anything that needs to name the design names the constant, so a reference
 * is checked by the compiler and initialises nothing: an id is a value, and
 * a palette that names five designs pulls in none of their words.
 *
 * <p>The slug is the wire form — kebab-case, {@code [a-z0-9-]+}. An
 * underscore is refused: it is the seam of a composed slug, {@code
 * physique_palette}, which only {@link Composed} mints, through
 * {@link #cross}.</p>
 *
 * @param slug the design's slug — stable, kebab-case, the {@code ?theme=} value
 */
public record DesignId(String slug) {

    private static final Pattern SIMPLE = Pattern.compile("[a-z0-9-]+");
    private static final Pattern CROSS  = Pattern.compile("[a-z0-9-]+_[a-z0-9-]+");

    public DesignId {
        if (slug == null || !(SIMPLE.matcher(slug).matches() || CROSS.matcher(slug).matches()))
            throw new IllegalArgumentException("a design id is kebab-case, or two joined by one '_': " + slug);
    }

    /** The id of a physique worn in a palette: {@code physique_palette}. */
    public static DesignId cross(DesignId physique, DesignId palette) {
        if (physique.isCross() || palette.isCross())
            throw new IllegalArgumentException("a cross composes two plain ids: " + physique + " × " + palette);
        return new DesignId(physique.slug + "_" + palette.slug);
    }

    /** Whether this names a physique worn in another's colours. */
    public boolean isCross() { return slug.indexOf('_') >= 0; }

    @Override public String toString() { return slug; }
}
