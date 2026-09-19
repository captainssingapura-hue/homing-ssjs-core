package hue.captains.singapura.js.homing.site.mpa;

/**
 * What the bar says the site is: a label, and where the label goes.
 *
 * @param label the house word, shown beside the mark and after every page title
 * @param home  the address the brand links to, {@code /} unless said otherwise
 */
public record Brand(String label, String home) {

    public Brand {
        if (label == null || label.isBlank()) throw new IllegalArgumentException("Brand.label must not be blank");
        if (home == null || home.isBlank()) home = "/";
    }

    public static Brand of(String label) { return new Brand(label, "/"); }
}
