package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc;
import hue.captains.singapura.js.homing.studio.base.composed.MarkdownSegment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Composed docs whose constants name each other, declared lazily: both are made, each names the
 * other - not null - and comparing or hashing either never reads the other, which names it back.
 */
class LazyReferencesTest {

    static final class First {
        static final ComposedDoc INSTANCE = new ComposedDoc(UUID.fromString("7a0c1d6e-0000-4000-8000-000000000011"), "First", "", "DOC",
                List.of(new MarkdownSegment("See [the second](#ref:second).")),
                LazyReferences.of(() -> List.of(new DocReference("second", Second.INSTANCE))));
    }

    static final class Second {
        static final ComposedDoc INSTANCE = new ComposedDoc(UUID.fromString("7a0c1d6e-0000-4000-8000-000000000012"), "Second", "", "DOC",
                List.of(new MarkdownSegment("See [the first](#ref:first).")),
                LazyReferences.of(() -> List.of(new DocReference("first", First.INSTANCE))));
    }

    @Test
    void docsThatNameEachOther_eachNameTheOther() {
        assertSame(Second.INSTANCE, ((DocReference) First.INSTANCE.references().get(0)).target());
        assertSame(First.INSTANCE, ((DocReference) Second.INSTANCE.references().get(0)).target());
        assertEquals("second", First.INSTANCE.references().iterator().next().name());
    }

    @Test
    void comparingOrHashing_neverReadsWhatItNames() {
        // a record's equals and hashCode walk its components: an element-wise list would walk into the
        // other doc, and back - without end
        assertEquals(First.INSTANCE.hashCode(), First.INSTANCE.hashCode());
        assertNotEquals(First.INSTANCE, Second.INSTANCE);
        assertEquals(First.INSTANCE, First.INSTANCE.withSlug(First.INSTANCE.slug()), "the same references: the same list");
    }
}
