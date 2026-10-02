package hue.captains.singapura.js.homing.site;

/**
 * The one thing every navigable that writes a query value into its page must
 * do first. Query values are the client's text; a page that interpolates one
 * unescaped has handed the client its markup.
 */
public final class Html {

    private Html() {}

    /** The five markup characters as entities; null as empty. */
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
