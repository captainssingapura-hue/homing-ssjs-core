package hue.captains.singapura.js.homing.site;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryTest {

    @Test
    void nothingIsNone() {
        assertEquals(Query.NONE, Query.parse(null));
        assertEquals(Query.NONE, Query.parse(""));
        assertEquals(Query.NONE, Query.parse("   "));
        assertTrue(Query.NONE.isEmpty());
        assertEquals(Optional.empty(), Query.NONE.first("name"));
        assertEquals(List.of(), Query.NONE.values("name"));
    }

    @Test
    void readsTheWayTheFlatRouteDoes() {
        var q = Query.parse("name=Ada+Lovelace&tag=a&tag=b&flag");
        assertEquals(Optional.of("Ada Lovelace"), q.first("name"));
        assertEquals(List.of("a", "b"), q.values("tag"));
        assertEquals(Optional.of(""), q.first("flag"));
        // A leading '?' is tolerated, as the core parser tolerates it.
        assertEquals(q, Query.parse("?name=Ada+Lovelace&tag=a&tag=b&flag"));
    }

    @Test
    void isImmutable() {
        var q = Query.of("k", "v");
        assertThrows(UnsupportedOperationException.class, () -> q.all().put("x", List.of()));
        assertThrows(UnsupportedOperationException.class, () -> q.all().get("k").add("w"));
    }

    @Test
    void addressFormIsTheCoreEncoding() {
        assertEquals("", Query.NONE.toString());
        assertEquals("name=Ada+Lovelace", Query.of("name", "Ada Lovelace").toString());
    }
}
