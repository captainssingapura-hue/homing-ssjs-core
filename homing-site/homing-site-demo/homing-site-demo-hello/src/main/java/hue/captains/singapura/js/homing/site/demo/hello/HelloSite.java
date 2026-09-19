package hue.captains.singapura.js.homing.site.demo.hello;

import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;

import java.util.Optional;

/**
 * The smallest site: a router of three arms over three pages, each a
 * {@link Navigable} that turns the query it was reached with into HTML.
 *
 * <p>The three arms show the three things a router does. {@code /} and
 * {@code /about} match a segment and hand back a page that ignores its query.
 * {@code /greet} hands back a page that reads its query. {@code /greet/Ada}
 * reads a segment as a parameter and binds it before handing the page back,
 * so the same {@link GreetPage} is reached with the name in the path or in
 * the query and cannot tell which — which is the contract: the router
 * consumes the path, the navigable gets the rest.</p>
 */
public record HelloSite() implements Site {

    public static final HelloSite INSTANCE = new HelloSite();

    @Override public String name() { return "hello"; }

    @Override
    public Router router() {
        return path -> switch (path.head().orElse("")) {
            case ""      -> path.isRoot() ? Optional.of(HomePage.INSTANCE) : Optional.empty();
            case "about" -> path.depth() == 1 ? Optional.of(AboutPage.INSTANCE) : Optional.empty();
            case "greet" -> greet(path.tail());
            default      -> Optional.empty();
        };
    }

    /** {@code /greet} reads the name from the query; {@code /greet/<name>} binds it from the path. */
    private static Optional<Navigable> greet(Path rest) {
        return switch (rest.depth()) {
            case 0  -> Optional.of(GreetPage.FROM_QUERY);
            case 1  -> Optional.of(GreetPage.bound(rest.head().orElseThrow()));
            default -> Optional.empty();
        };
    }
}
