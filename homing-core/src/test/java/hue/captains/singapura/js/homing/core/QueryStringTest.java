package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** RFC 0051 — the one place query strings are encoded and decoded. */
class QueryStringTest {

    @Test
    void encodesAndParsesBack() {
        var p = QueryString.params();
        QueryString.put(p, "id", "animals");
        QueryString.put(p, "path", "cute/otter");
        assertEquals("id=animals&path=cute%2Fotter", QueryString.encode(p));
        assertEquals(p, QueryString.parse(QueryString.encode(p)));
    }

    @Test
    void survivesValuesThatWouldBreakConcatenation() {
        // The reason this facility exists: these are exactly the values the
        // hand-rolled sites got wrong, and got wrong silently.
        for (String nasty : List.of("a&b=c", "a b", "100%", "=", "ü", "a+b", "")) {
            var p = QueryString.of("v", nasty);
            var back = QueryString.parse(QueryString.encode(p));
            assertEquals(List.of(nasty), back.get("v"), "round-trip failed for: " + nasty);
        }
    }

    @Test
    void repeatedKeysAreOrdinary() {
        var p = QueryString.params();
        QueryString.put(p, "tag", "one");
        QueryString.put(p, "tag", "two");
        assertEquals("tag=one&tag=two", QueryString.encode(p));
        assertEquals(List.of("one", "two"), QueryString.parse("tag=one&tag=two").get("tag"));
    }

    @Test
    void nullValueIsAnAbsentKeyNotTheStringNull() {
        var p = QueryString.params();
        QueryString.put(p, "path", null);
        assertTrue(p.isEmpty());
        assertEquals("", QueryString.encode(p));
    }

    @Test
    void encodingIsStableSoUrlsStayComparable() {
        var a = QueryString.params();
        QueryString.put(a, "x", "1");
        QueryString.put(a, "y", "2");
        var b = QueryString.params();
        QueryString.put(b, "x", "1");
        QueryString.put(b, "y", "2");
        assertEquals(QueryString.encode(a), QueryString.encode(b));
    }

    @Test
    void parsingIsTotal() {
        // Untrusted input: none of these may throw.
        assertTrue(QueryString.parse(null).isEmpty());
        assertTrue(QueryString.parse("").isEmpty());
        assertTrue(QueryString.parse("&&&").isEmpty());
        assertEquals(List.of(""), QueryString.parse("flag").get("flag"));   // no '='
        assertEquals(List.of("%zz"), QueryString.parse("v=%zz").get("v"));  // bad escape kept raw
    }

    @Test
    void acceptsAFullUrlOrABareQuery() {
        assertEquals(List.of("doc-reader"),
                QueryString.parse("/app?app=doc-reader").get("app"));
        assertEquals(List.of("doc-reader"),
                QueryString.parse("app=doc-reader").get("app"));
        assertEquals(List.of("doc-reader"),
                QueryString.parse("/app?app=doc-reader#frag").get("app"));
    }

    @Test
    void suffixFormLetsCallersAppendUnconditionally() {
        assertEquals("", QueryString.encodeSuffix(QueryString.params()));
        assertEquals("?a=1", QueryString.encodeSuffix(QueryString.of("a", "1")));
    }
}
