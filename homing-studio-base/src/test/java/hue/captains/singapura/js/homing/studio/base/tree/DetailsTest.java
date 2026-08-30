package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.NavKey;
import hue.captains.singapura.js.homing.tree.NodeIdentity;
import hue.captains.singapura.js.homing.tree.NodeResolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DetailsTest {

    private static NavKey cat(String fqn) { return CatalogueAppHost.identityFor(fqn); }

    // ── Illustration ───────────────────────────────────────────────────────

    @Test
    void anIllustrationNeedsOnlyALabel() {
        var i = Illustration.of("Ontology", "What kinds of things exist");
        assertEquals("Ontology", i.label());
        assertEquals("", i.badge());
        assertEquals("", i.icon());
        assertEquals("", i.kind());
    }

    @Test
    void absentChromeIsEmptyRatherThanNull() {
        var i = new Illustration("Meta", null, null, null, null);
        assertEquals("", i.summary());
        assertEquals("", i.badge());
        assertEquals("", i.icon());
        assertEquals("", i.kind());
    }

    /** The label is the one field that must be supplied — it cannot be derived. */
    @Test
    void aLabelIsRequired() {
        assertThrows(NullPointerException.class,
                () -> new Illustration(null, "", "", "", ""));
    }

    /**
     * The measured reason the label travels here rather than riding on NodeName:
     * a segment is a lossy slug, so humanising it does not reconstruct the label.
     * Over the live studio 213 of 223 labels differ from their humanised segment;
     * these are two of them.
     */
    @Test
    void aLabelIsNotRecoverableFromItsSegment() {
        assertEquals("Ontology — Doc", Illustration.of("Ontology — Doc", "").label());
        // humanising "doc-ontology" would give "Doc Ontology" — a different string.
        assertEquals("Ontology — Doc", new Illustration("Ontology — Doc", "", "", "", "doc").label());
    }

    // ── The two cases ──────────────────────────────────────────────────────

    @Test
    void aBranchIsAnIllustrationAndNothingMore() {
        Details d = new Details.OfBranch(Illustration.of("Meta", "the meta catalogue"));
        assertInstanceOf(Details.OfBranch.class, d);
        assertEquals("Meta", d.illustration().label());
    }

    @Test
    void bothCasesExposeTheIllustrationThroughTheInterface() {
        // The shared half: a consumer that only draws does not switch at all.
        List<Details> both = List.of(
                new Details.OfBranch(Illustration.of("Meta", "")),
                new Details.OfBranch(Illustration.of("RFCs", "")));
        assertEquals(List.of("Meta", "RFCs"), both.stream().map(x -> x.illustration().label()).toList());
    }

    /** Sealed, so a switch over the cases is exhaustive without a default. */
    @Test
    void theCasesAreExhaustiveWithoutADefault() {
        Details d = new Details.OfBranch(Illustration.of("Meta", ""));
        String kind = switch (d) {
            case Details.OfLeaf leaf     -> "leaf:" + leaf.nav().name();
            case Details.OfBranch branch -> "branch:" + branch.illustration().label();
        };
        assertEquals("branch:Meta", kind);
    }

    // ── Through the resolver ───────────────────────────────────────────────

    /**
     * The point of typing the answer: a union over heterogeneous IDENTITIES still
     * answers in one shape, so the access path needs no cast and no wildcard.
     */
    @Test
    void aUnionAnswersInOneShapeAcrossIdentityKinds() {
        record OtherId(String name) implements NodeIdentity {}

        Map<NavKey, Details> catalogues = Map.of(
                cat("a.MetaCatalogue"), new Details.OfBranch(Illustration.of("Meta", "")));
        Map<OtherId, Details> others = Map.of(
                new OtherId("crate"), new Details.OfBranch(Illustration.of("Crates", "")));

        NodeResolver<Details> union = NodeResolver.union(List.of(
                NodeResolver.forKind(NavKey.class,   catalogues::get),
                NodeResolver.forKind(OtherId.class, others::get)));

        // No cast at either call site, and the two identity kinds are unrelated.
        assertEquals(Optional.of("Meta"),
                union.resolve(cat("a.MetaCatalogue")).map(d -> d.illustration().label()));
        assertEquals(Optional.of("Crates"),
                union.resolve(new OtherId("crate")).map(d -> d.illustration().label()));
        assertEquals(Optional.empty(), union.resolve(new OtherId("absent")));
    }

    @Test
    void anIdentityNobodyOwnsAnswersEmpty() {
        NodeResolver<Details> r = NodeResolver.forKind(NavKey.class, k -> null);
        assertEquals(Optional.empty(), r.resolve(cat("a.Absent")));
    }
}
