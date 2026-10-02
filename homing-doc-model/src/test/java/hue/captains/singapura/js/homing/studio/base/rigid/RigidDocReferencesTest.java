package hue.captains.singapura.js.homing.studio.base.rigid;

import hue.captains.singapura.js.homing.studio.base.DocReference;
import hue.captains.singapura.js.homing.tree.NodeName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A rigid doc's references: none unless it declares some; declared lazily, so two docs whose
 * constants name each other are both made, and each names the other - not null.
 */
class RigidDocReferencesTest {

    /** Two docs' constants, each naming the other: whichever class is made first, neither holds null. */
    static final class First {
        static final RigidDoc INSTANCE = RigidDoc.root(UUID.fromString("7a0c1d6e-0000-4000-8000-000000000001"), "First", "", "DOC")
                .text("Names the second.").build()
                .withReferences(() -> List.of(new DocReference("second", Second.INSTANCE)));
    }

    static final class Second {
        static final RigidDoc INSTANCE = RigidDoc.root(UUID.fromString("7a0c1d6e-0000-4000-8000-000000000002"), "Second", "", "DOC")
                .text("Names the first.").build()
                .withReferences(() -> List.of(new DocReference("first", First.INSTANCE)));
    }

    @Test
    void docsThatNameEachOther_eachNameTheOther() {
        assertSame(Second.INSTANCE, ((DocReference) First.INSTANCE.references().get(0)).target());
        assertSame(First.INSTANCE, ((DocReference) Second.INSTANCE.references().get(0)).target());
    }

    @Test
    void aRigidDoc_declaresNone_unlessItDeclaresSome_andKeepsThemWithItsSlug() {
        var plain = RigidDoc.root(UUID.fromString("7a0c1d6e-0000-4000-8000-000000000003"), "Plain", "", "DOC").text("x").build();
        assertTrue(plain.references().isEmpty());
        var named = First.INSTANCE.withSlug(new NodeName("first-one"));
        assertEquals("first-one", named.authoredSlug().value());
        assertEquals(1, named.references().size(), "a slug given after keeps the references");
        assertEquals("first-one", named.withReferences(List::of).authoredSlug().value(), "references given after keep the slug");
    }
}
