package hue.captains.singapura.js.homing.site.demo.hello;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Query;

/** {@code /about} — the same page every time; the query is not consulted. */
final class AboutPage implements Navigable {

    static final AboutPage INSTANCE = new AboutPage();

    private AboutPage() {}

    @Override
    public HtmlPageContent html(Query query) {
        return Shell.page("About", """
                <h1>About</h1>
                <p>Four types make the base. <code>Path</code> is the segments a request came
                   in on. <code>Query</code> is what followed the question mark, possibly nothing.
                   <code>Navigable</code> is a destination, and its whole contract is
                   <code>html(Query)</code>. <code>Router</code> is <code>resolve(Path)</code>:
                   path in, navigable out, or empty.</p>
                <p>A <code>Site</code> is a router with a name; <code>SiteHost</code> puts one
                   catch-all action in front of the router and listens. Nothing here knows how
                   a page came by its HTML — this one is a string, and a page that starts a JS
                   app behind the server's scaffold would look the same from the router.</p>
                """);
    }
}
