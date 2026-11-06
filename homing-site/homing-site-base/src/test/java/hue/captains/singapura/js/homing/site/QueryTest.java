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
        assertEquals(Query.NONE, new Query((String) null));
        assertEquals(Query.NONE, new Query(""));
        assertEquals(Query.NONE, new Query("   "));
        assertTrue(Query.NONE.isEmpty());
        assertEquals(Optional.empty(), Query.NONE.first("name"));
        assertEquals(List.of(), Query.NONE.values("name"));
    }

    @Test
    void readsTheWayTheFlatRouteDoes() {
        var q = new Query("name=Ada+Lovelace&tag=a&tag=b&flag");
        assertEquals(Optional.of("Ada Lovelace"), q.first("name"));
        assertEquals(List.of("a", "b"), q.values("tag"));
        assertEquals(Optional.of(""), q.first("flag"));
        // A leading '?' is tolerated, as the core parser tolerates it.
        assertEquals(q, new Query("?name=Ada+Lovelace&tag=a&tag=b&flag"));
    }

    @Test
    void isImmutable() {
        var q = new Query("k", "v");
        assertThrows(UnsupportedOperationException.class, () -> q.all().put("x", List.of()));
        assertThrows(UnsupportedOperationException.class, () -> q.all().get("k").add("w"));
    }

    @Test
    void addressFormIsTheCoreEncoding() {
        assertEquals("", Query.NONE.toString());
        assertEquals("name=Ada+Lovelace", new Query("name", "Ada Lovelace").toString());
    }
}
