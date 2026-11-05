package hue.captains.singapura.js.homing.site;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathTest {

    @Test
    void slashesContributeNothing() {
        assertEquals(Path.ROOT, Path.parse("/"));
        assertEquals(Path.ROOT, Path.parse(""));
        assertEquals(Path.ROOT, Path.parse("//"));
        assertEquals(Path.ROOT, Path.parse(null));
        assertEquals(new Path("about"), Path.parse("/about/"));
        assertEquals(new Path("a", "b"), Path.parse("a//b"));
    }

    @Test
    void segmentsRoundTripThroughTheAddressForm() {
        var p = new Path("greet", "Ada Lovelace", "a/b", "1+1");
        assertEquals("/greet/Ada%20Lovelace/a%2Fb/1%2B1", p.toString());
        assertEquals(p, Path.parse(p.toString()));
        // A literal plus in a path is a plus, not a space.
        assertEquals(new Path("1+1"), Path.parse("/1+1"));
    }

    @Test
    void headTailChild() {
        var p = new Path("a", "b", "c");
        assertEquals(Optional.of("a"), p.head());
        assertEquals(new Path("b", "c"), p.tail());
        assertEquals(new Path("a", "b", "c", "d"), p.child("d"));
        assertEquals(Optional.empty(), Path.ROOT.head());
        assertEquals(Path.ROOT, Path.ROOT.tail());
        assertTrue(Path.ROOT.isRoot());
        assertEquals(3, p.depth());
    }

    @Test
    void anEmptySegmentIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Path(List.of("a", "")));
    }
}
