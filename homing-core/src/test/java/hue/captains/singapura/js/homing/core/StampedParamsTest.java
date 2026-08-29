package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0051 — the escaping law for values the server puts inside a
 * {@code <script>} element.
 *
 * <p>Every value here arrives off a query string, so this is the boundary
 * between a parameter and executable page content. Hazard characters are
 * built from code points rather than written literally, so the test file
 * itself stays readable and no editor or encoding step can quietly normalise
 * the thing under test.</p>
 */
class StampedParamsTest {

    private static final String LINE_SEP  = String.valueOf((char) 0x2028);
    private static final String PARA_SEP  = String.valueOf((char) 0x2029);
    private static final String SOH       = String.valueOf((char) 0x01);

    @Test
    void closingScriptTagCannotSurvive() {
        // </script ends the element wherever it appears, string literal or not.
        String js = StampedParams.jsString("</script><script>alert(1)</script>");
        assertFalse(js.contains("</script"), js);
        assertFalse(js.contains("<script"), js);
    }

    @Test
    void htmlCommentOpenerCannotSurvive() {
        // <!-- opens a comment that legacy script parsing still honours.
        String js = StampedParams.jsString("<!--");
        assertFalse(js.contains("<!--"), js);
    }

    @Test
    void ecmascriptLineTerminatorsAreEscaped() {
        // U+2028/9 are line terminators in JS but not in JSON: a value with
        // one parses fine as JSON and then breaks the surrounding script.
        String js = StampedParams.jsString("a" + LINE_SEP + "b" + PARA_SEP + "c");
        assertFalse(js.contains(LINE_SEP), "raw U+2028 survived: " + js);
        assertFalse(js.contains(PARA_SEP), "raw U+2029 survived: " + js);
        assertTrue(js.contains("\\u2028"), js);
        assertTrue(js.contains("\\u2029"), js);
    }

    @Test
    void quotesAndBackslashesAreEscaped() {
        assertEquals("\"a\\\\b\"", StampedParams.jsString("a\\b"));
        assertEquals("\"a\\\"b\"", StampedParams.jsString("a\"b"));
    }

    @Test
    void controlCharactersAreEscaped() {
        String js = StampedParams.jsString("a" + SOH + "b");
        assertFalse(js.contains(SOH), js);
        assertTrue(js.contains("\\u0001"), js);
    }

    @Test
    void nullIsTheJsNullNotTheStringNull() {
        assertEquals("null", StampedParams.jsString(null));
    }

    @Test
    void objectIsFrozenAndKeepsCardinality() {
        var p = QueryString.params();
        QueryString.put(p, "id", "animals");
        QueryString.put(p, "tag", "one");
        QueryString.put(p, "tag", "two");
        String js = StampedParams.jsObject(p);
        assertTrue(js.startsWith("Object.freeze({"), js);
        assertTrue(js.contains("\"id\":\"animals\""), js);
        // A repeated key stays repeated — the page sees what the URL carried.
        assertTrue(js.contains("Object.freeze([\"one\",\"two\"])"), js);
    }

    @Test
    void emptyParamsAreStillAnObject() {
        assertEquals("Object.freeze({})", StampedParams.jsObject(QueryString.params()));
    }

    @Test
    void aHostileParamCannotEscapeTheScriptBlock() {
        // The end-to-end shape: whatever arrives, the stamped text contains no
        // sequence that could terminate or reopen the element.
        var p = QueryString.of("path", "</script><img src=x onerror=alert(1)>");
        String js = StampedParams.jsObject(p);
        assertFalse(js.contains("</script"), js);
        assertFalse(js.contains("<img"), js);
    }
}
