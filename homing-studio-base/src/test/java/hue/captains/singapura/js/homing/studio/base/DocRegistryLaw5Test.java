package hue.captains.singapura.js.homing.studio.base;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0051 Law 5 (RFC 0053 Phase 6) — a reference resolves.
 *
 * <p>The law has to FAIL on a constructed violation before it is believed. RFC
 * 0051 Phase 1 twice shipped a law that quietly did not hold while passing — one
 * vacuous under the type system, one comparing the wrong thing — so a law that
 * has only ever been seen to pass has not been tested.</p>
 *
 * <p>Law 5 did fire on thirty real references when it landed, which is stronger
 * evidence than any construction. But the flat folder then fixed those, so
 * nothing keeps it honest any more: the check could be broken tomorrow and every
 * suite would stay green. That is what these tests are for.</p>
 */
class DocRegistryLaw5Test {

    private record TestDoc(UUID uuid, String title, List<Reference> references) implements Doc {
        @Override public String contents() { return "# " + title; }
    }

    private static TestDoc doc(String name, Reference... refs) {
        return new TestDoc(
                UUID.nameUUIDFromBytes(("law5:" + name).getBytes()), name, List.of(refs));
    }

    // ── The bite ───────────────────────────────────────────────────────────

    /**
     * The whole point of the law. The reference is type-safe — {@code
     * DocReference(String, Doc)} would not compile with anything else — and the
     * target is a real object with a real uuid. It is still broken, because
     * nothing makes it reachable. No compiler catches this; only a law does.
     */
    @Test
    void fails_whenAReferenceNamesADocTheRegistryDoesNotHold() {
        TestDoc absent = doc("absent");
        TestDoc citing = doc("citing", new DocReference("the-missing-one", absent));

        var registry = new DocRegistry(List.of(citing));   // absent deliberately left out

        var ex = assertThrows(IllegalStateException.class, registry::assertReferencesResolve);
        assertTrue(ex.getMessage().contains("Law 5"), ex.getMessage());
        assertTrue(ex.getMessage().contains("the-missing-one"), ex.getMessage());
    }

    /** The offender is named on BOTH sides — who cited, and what they cited. */
    @Test
    void namesTheCitingDocAndTheTarget() {
        TestDoc absent = doc("Absent Target");
        TestDoc citing = doc("Citing Doc", new DocReference("r", absent));

        var ex = assertThrows(IllegalStateException.class,
                () -> new DocRegistry(List.of(citing)).assertReferencesResolve());

        assertTrue(ex.getMessage().contains("Citing Doc"),  ex.getMessage());
        assertTrue(ex.getMessage().contains("Absent Target"), ex.getMessage());
    }

    // ── And does not bite where it should not ──────────────────────────────

    /** Registered is enough. The law was relaxed from "placed" on purpose (D4). */
    @Test
    void passes_whenTheTargetIsRegistered() {
        TestDoc target = doc("target");
        TestDoc citing = doc("citing", new DocReference("r", target));

        var registry = new DocRegistry(List.of(citing, target));

        assertDoesNotThrow(registry::assertReferencesResolve);
    }

    /**
     * External and image references are outside the law by construction — it
     * governs the citation graph between Docs, and an external URL is not a
     * vertex of it.
     */
    @Test
    void ignores_referenceKindsThatNameNoDoc() {
        TestDoc citing = doc("citing",
                new ExternalReference("spec", "https://example.invalid", "Spec", "an external"),
                new ImageReference("pic", "/img/x.png", "alt", "caption"));

        assertDoesNotThrow(new DocRegistry(List.of(citing))::assertReferencesResolve);
    }

    /**
     * A PARTIAL registry is a legitimate thing to build — DocConformanceTest
     * builds one from a sub-closure purely to exercise the collision check. The
     * first cut of this law ran in the constructor and failed on ~66 references
     * that merely left the subset. Construction must stay silent; only the boot,
     * which knows its registry is complete, asks.
     */
    @Test
    void constructionIsSilent_soPartialRegistriesRemainLegal() {
        TestDoc absent = doc("absent");
        TestDoc citing = doc("citing", new DocReference("r", absent));

        assertDoesNotThrow(() -> new DocRegistry(List.of(citing)));
    }
}
