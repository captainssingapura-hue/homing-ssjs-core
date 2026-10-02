package hue.captains.singapura.js.homing.site.demo.hello;

import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.site.Router;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The three arms, the two ways to a name, and the misses — no server. */
class HelloSiteTest {

    private static final Router ROUTER = HelloSite.INSTANCE.router();

    @Test
    void theThreeArms() {
        assertSame(HomePage.INSTANCE,  ROUTER.resolve(Path.ROOT).orElseThrow());
        assertSame(AboutPage.INSTANCE, ROUTER.resolve(Path.of("about")).orElseThrow());
        assertSame(GreetPage.FROM_QUERY, ROUTER.resolve(Path.of("greet")).orElseThrow());
        assertEquals(GreetPage.bound("Grace"), ROUTER.resolve(Path.of("greet", "Grace")).orElseThrow());
    }

    @Test
    void theMisses() {
        assertTrue(ROUTER.resolve(Path.of("nowhere")).isEmpty());
        assertTrue(ROUTER.resolve(Path.of("about", "more")).isEmpty());
        assertTrue(ROUTER.resolve(Path.of("greet", "a", "b")).isEmpty());
    }

    @Test
    void aNameFromTheQuery() {
        var body = GreetPage.FROM_QUERY.html(Query.of("name", "Ada")).body();
        assertTrue(body.contains("<h1>Hello, Ada.</h1>"), body);
        assertTrue(body.contains("read from the query"), body);
    }

    @Test
    void aNameBoundOffThePathWinsOverTheQuery() {
        var body = GreetPage.bound("Grace").html(Query.of("name", "Ada")).body();
        assertTrue(body.contains("<h1>Hello, Grace.</h1>"), body);
        assertTrue(body.contains("bound off the path"), body);
    }

    @Test
    void noNameIsAStranger() {
        var body = GreetPage.FROM_QUERY.html(Query.NONE).body();
        assertTrue(body.contains("<h1>Hello, stranger.</h1>"), body);
    }

    @Test
    void aNameIsEscapedOnTheWayIntoThePage() {
        var body = GreetPage.FROM_QUERY.html(Query.of("name", "<b>x</b>")).body();
        assertTrue(body.contains("Hello, &lt;b&gt;x&lt;/b&gt;."), body);
        assertTrue(!body.contains("<b>x</b>"), body);
    }
}
