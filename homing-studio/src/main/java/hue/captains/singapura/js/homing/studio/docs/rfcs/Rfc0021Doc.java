package hue.captains.singapura.js.homing.studio.docs.rfcs;

import hue.captains.singapura.js.homing.studio.base.ClasspathMarkdownDoc;
import hue.captains.singapura.js.homing.studio.base.DocReference;
import hue.captains.singapura.js.homing.studio.base.Reference;
import hue.captains.singapura.js.homing.studio.docs.defects.Defect0005Doc;
import hue.captains.singapura.js.homing.studio.docs.doctrines.FunctionalObjectsDoc;
import hue.captains.singapura.js.homing.studio.docs.doctrines.WeighedComplexityDoc;
import hue.captains.singapura.js.homing.studio.docs.releases.Release0_0_101Doc;
import hue.captains.singapura.js.homing.studio.docs.releases.Release0_0_110Doc;

import java.util.List;
import java.util.UUID;

/**
 * RFC 0021 — Codebase Separation. Formalises the four-repo target shape
 * for splitting the current monorepo by audience:
 *
 * <ul>
 *   <li>{@code homing-ssjs-core} (public, framework)</li>
 *   <li>{@code homing-doc-plus-demo} (public, design history + demos)</li>
 *   <li>{@code homing-private-studio} (private, maintainer's working surface)</li>
 *   <li>{@code homing-components} (public, deferred — one repo eventually)</li>
 * </ul>
 *
 * <p>Top-down migration order — public/private firewall extracts first,
 * framework code stays in the legacy repo through Phases 1+2. Phase 3
 * is the {@code homing.js} → {@code homing-ssjs-core} Maven coordinate
 * rename. Components extraction is deferred to "evidence of external
 * need" with no scheduled trigger.</p>
 *
 * <p>Operational sibling: {@code SEPARATION_PLAN.md} at the legacy repo
 * root carries the executable detail (pre-flight classification,
 * git-filter-repo invocations, Maven re-wiring, verification gates).</p>
 *
 * @see Rfc0012Doc — Typed Studio Composition; enables this separation
 * @see Rfc0011Doc — Cross-tree Composition; allows composing studios across repos
 */
public record Rfc0021Doc() implements ClasspathMarkdownDoc {
    private static final UUID ID = UUID.fromString("a1b2c3d4-5678-49ab-bcde-f0123456789a");
    public static final Rfc0021Doc INSTANCE = new Rfc0021Doc();

    @Override public UUID   uuid()    { return ID; }
    @Override public String title()   { return "RFC 0021 — Codebase Separation"; }
    @Override public String summary() { return "Separate the monorepo into four repos by audience: homing-ssjs-core (framework), homing-doc-plus-demo (public design history + runnable demos), homing-private-studio (in-flight work), homing-components (deferred — eventually one repo for building blocks). Top-down migration — public/private firewall extracts first, framework code stays put until Phase 3. The codebase IS the documentation for the public face; the firewall is a repo boundary not a folder convention. Cross-repo CI deferred; manual coordination is initial policy. Builds on RFC 0012 (typed studio composition) and RFC 0011 (cross-tree composition)."; }
    @Override public String category(){ return "RFC"; }

    @Override public List<Reference> references() {
        return List.of(
                new DocReference("rfc-11",       Rfc0011Doc.INSTANCE),
                new DocReference("rfc-12",       Rfc0012Doc.INSTANCE),
                new DocReference("rfc-19",       Rfc0019Doc.INSTANCE),
                new DocReference("doc-fo",       FunctionalObjectsDoc.INSTANCE),
                new DocReference("doc-wc",       WeighedComplexityDoc.INSTANCE),
                new DocReference("def-5",        Defect0005Doc.INSTANCE),
                new DocReference("rel-0-0-101",  Release0_0_101Doc.INSTANCE),
                new DocReference("rel-0-0-110",  Release0_0_110Doc.INSTANCE)
        );
    }
}
