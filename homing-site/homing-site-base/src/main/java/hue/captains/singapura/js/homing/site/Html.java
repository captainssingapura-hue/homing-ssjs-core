package hue.captains.singapura.js.homing.site;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * The one thing every navigable that writes a query value into its page must
 * do first. Query values are the client's text; a page that interpolates one
 * unescaped has handed the client its markup.
 */
public record Html() implements StatelessFunctionalObject {

    public static final Html INSTANCE = new Html();

    /** The five markup characters as entities; null as empty. */
    public String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
