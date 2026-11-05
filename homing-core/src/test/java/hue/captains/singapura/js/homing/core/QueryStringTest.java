package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** RFC 0051 — the one place query strings are encoded and decoded. */
class QueryStringTest {

    @Test
    void encodesAndParsesBack() {
        var p = QueryString.INSTANCE.params();
        QueryString.INSTANCE.put(p, "id", "animals");
        QueryString.INSTANCE.put(p, "path", "cute/otter");
        assertEquals("id=animals&path=cute%2Fotter", QueryString.INSTANCE.encode(p));
        assertEquals(p, QueryString.INSTANCE.parse(QueryString.INSTANCE.encode(p)));
    }

    @Test
    void survivesValuesThatWouldBreakConcatenation() {
        // The reason this facility exists: these are exactly the values the
        // hand-rolled sites got wrong, and got wrong silently.
        for (String nasty : List.of("a&b=c", "a b", "100%", "=", "ü", "a+b", "")) {
            var p = QueryString.INSTANCE.of("v", nasty);
            var back = QueryString.INSTANCE.parse(QueryString.INSTANCE.encode(p));
            assertEquals(List.of(nasty), back.get("v"), "round-trip failed for: " + nasty);
        }
    }

    @Test
    void repeatedKeysAreOrdinary() {
        var p = QueryString.INSTANCE.params();
        QueryString.INSTANCE.put(p, "tag", "one");
        QueryString.INSTANCE.put(p, "tag", "two");
        assertEquals("tag=one&tag=two", QueryString.INSTANCE.encode(p));
        assertEquals(List.of("one", "two"), QueryString.INSTANCE.parse("tag=one&tag=two").get("tag"));
    }

    @Test
    void nullValueIsAnAbsentKeyNotTheStringNull() {
        var p = QueryString.INSTANCE.params();
        QueryString.INSTANCE.put(p, "path", null);
        assertTrue(p.isEmpty());
        assertEquals("", QueryString.INSTANCE.encode(p));
    }

    @Test
    void encodingIsStableSoUrlsStayComparable() {
        var a = QueryString.INSTANCE.params();
        QueryString.INSTANCE.put(a, "x", "1");
        QueryString.INSTANCE.put(a, "y", "2");
        var b = QueryString.INSTANCE.params();
        QueryString.INSTANCE.put(b, "x", "1");
        QueryString.INSTANCE.put(b, "y", "2");
        assertEquals(QueryString.INSTANCE.encode(a), QueryString.INSTANCE.encode(b));
    }

    @Test
    void parsingIsTotal() {
        // Untrusted input: none of these may throw.
        assertTrue(QueryString.INSTANCE.parse(null).isEmpty());
        assertTrue(QueryString.INSTANCE.parse("").isEmpty());
        assertTrue(QueryString.INSTANCE.parse("&&&").isEmpty());
        assertEquals(List.of(""), QueryString.INSTANCE.parse("flag").get("flag"));   // no '='
        assertEquals(List.of("%zz"), QueryString.INSTANCE.parse("v=%zz").get("v"));  // bad escape kept raw
    }

    @Test
    void acceptsAFullUrlOrABareQuery() {
        assertEquals(List.of("doc-reader"),
                QueryString.INSTANCE.parse("/app?app=doc-reader").get("app"));
        assertEquals(List.of("doc-reader"),
                QueryString.INSTANCE.parse("app=doc-reader").get("app"));
        assertEquals(List.of("doc-reader"),
                QueryString.INSTANCE.parse("/app?app=doc-reader#frag").get("app"));
    }

    @Test
    void suffixFormLetsCallersAppendUnconditionally() {
        assertEquals("", QueryString.INSTANCE.encodeSuffix(QueryString.INSTANCE.params()));
        assertEquals("?a=1", QueryString.INSTANCE.encodeSuffix(QueryString.INSTANCE.of("a", "1")));
    }
}
