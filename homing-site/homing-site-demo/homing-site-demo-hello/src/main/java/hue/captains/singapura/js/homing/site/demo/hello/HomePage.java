package hue.captains.singapura.js.homing.site.demo.hello;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Query;

/** {@code /} — says what the site is and where its pages are. Ignores its query. */
final class HomePage implements Navigable {

    static final HomePage INSTANCE = new HomePage();

    private HomePage() {}

    @Override
    public HtmlPageContent html(Query query) {
        return Shell.page("Home", """
                <h1>hello</h1>
                <p>The smallest site on <code>homing-site-base</code>: a router of three arms
                   over three pages. Each page is a <code>Navigable</code> — given the query
                   it was reached with, possibly none, it returns the page — and the router
                   is one function from a path to one of them.</p>
                <ul>
                    <li><a href="/">/</a> — this page; ignores its query</li>
                    <li><a href="/about">/about</a> — a page that is the same every time</li>
                    <li><a href="/greet?name=Ada">/greet?name=Ada</a> — a page that reads its query</li>
                    <li><a href="/greet/Grace">/greet/Grace</a> — the same page, its name bound off the path by the router</li>
                    <li><a href="/nowhere">/nowhere</a> — nothing there: the router's empty, the host's 404</li>
                </ul>
                """);
    }
}
