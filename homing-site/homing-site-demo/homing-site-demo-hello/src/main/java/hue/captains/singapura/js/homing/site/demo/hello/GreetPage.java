package hue.captains.singapura.js.homing.site.demo.hello;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Html;
import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Query;

import java.util.Optional;

/**
 * {@code /greet} — the page that reads its query, and the page a name was
 * bound into by the router. One class, because the page cannot tell the
 * difference: a bound name is a name the router already consumed, and the
 * query is whatever it did not.
 *
 * <p>Precedence is the router's, not the page's: {@code /greet/Grace?name=Ada}
 * greets Grace, because the router bound Grace before the page saw the
 * query. A site that wanted the query to win would bind nothing.</p>
 *
 * @param bound the name the router read off the path, or empty for {@code /greet}
 */
record GreetPage(Optional<String> bound) implements Navigable {

    static final GreetPage FROM_QUERY = new GreetPage(Optional.empty());

    static GreetPage bound(String name) { return new GreetPage(Optional.of(name)); }

    @Override
    public HtmlPageContent html(Query query) {
        String name  = bound.or(() -> query.first("name")).filter(s -> !s.isBlank()).orElse("stranger");
        String where = bound.isPresent() ? "bound off the path by the router"
                     : query.first("name").isPresent() ? "read from the query"
                     : "nobody said — the query had no <code>name</code>";
        return Shell.page("Hello, " + name, """
                <h1>Hello, %s.</h1>
                <p>Your name was %s.</p>
                <p>Try <a href="/greet?name=Ada">/greet?name=Ada</a>,
                   <a href="/greet/Grace">/greet/Grace</a>,
                   <a href="/greet/Grace?name=Ada">/greet/Grace?name=Ada</a>
                   or <a href="/greet">/greet</a>.</p>
                """.formatted(Html.escape(name), where));
    }
}
