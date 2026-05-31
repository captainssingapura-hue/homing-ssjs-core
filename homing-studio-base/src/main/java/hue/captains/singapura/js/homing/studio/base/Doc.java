package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.studio.base.app.CatalogueLeaf;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A typed reference to a document — markdown, HTML, plain text, JSON, or any other
 * text-based format the studio reader / browser knows how to display.
 *
 * <p>Per <a href="../../../../../../../../../../docs/rfcs/0004-typed-docs-and-doc-visibility.md">
 * RFC 0004</a>, every Doc is identified by a {@link UUID} (the wire identity, stable across
 * Java renames and file moves) and provides its own bytes via {@link #contents()}. The framework
 * never reaches outside the Doc to load anything; the Doc owns its sourcing.</p>
 *
 * <p>Most Docs are static markdown shipped on the classpath next to their record class. Use
 * {@link ClasspathMarkdownDoc} for the dominant case (zero ceremony — record class location
 * implies file location). Other static cases:</p>
 *
 * <ul>
 *   <li>{@link InlineDoc} — contents inlined as a Java text block; no companion file.</li>
 *   <li>{@link ResourceMarkdownDoc} — classpath-loaded but at an explicit path, when the
 *       co-located mirror doesn't fit.</li>
 * </ul>
 *
 * <p>For non-static sources (database, network, generated content), implement {@link Doc}
 * directly and provide a custom {@link #contents()}.</p>
 *
 * <h2>Identity vs. addressability</h2>
 *
 * <p>The {@link #uuid()} is the only thing that travels on the wire. Java class names,
 * package, on-disk file path, and even {@link #title()} are all free to change without
 * breaking external links / bookmarks / API references. Generate a UUID once with
 * {@code UUID.randomUUID()} (or a JShell session), paste as a {@code static final UUID}
 * constant, and never edit it again.</p>
 *
 * @since RFC 0004
 */
public interface Doc extends CatalogueLeaf {

    /**
     * Stable surrogate identity for this Doc on the wire. Unique within a {@link DocRegistry}.
     * Generated once and frozen — must not change across Java renames or file moves.
     *
     * <p>Prose Docs use UUID identity; this method is the source of truth for them.
     * Future non-prose Doc kinds (PlanDoc, AppDoc per RFC 0015 Phase 3) may carry
     * non-UUID identity via {@link #id()} and may leave this method unimplemented
     * once that migration completes. Until then, every Doc supplies a UUID.</p>
     */
    UUID uuid();

    /**
     * RFC 0015 Phase 2 — typed identity for this Doc. Default wraps {@link #uuid()}
     * as a {@link DocId.ByUuid}. Phase 3 introduces non-UUID variants (PlanDoc,
     * AppDoc); Doc subtypes that don't rest on UUID identity will override this
     * default to return their own DocId variant.
     *
     * <p>Realises Doc ontology axiom A2 (universality of the identifier) — every
     * Doc surfaces its identity through this single accessor regardless of which
     * underlying id shape it uses.</p>
     */
    default DocId id() {
        return new DocId.ByUuid(uuid());
    }

    /** Display title shown in browsers and reader headers. */
    String title();

    /**
     * The bytes of this Doc as a UTF-8 string. Source of truth — every server endpoint
     * calls this. The framework places no constraints on where the content originates.
     */
    String contents();

    /** Optional one-line summary shown on browser cards. Default empty. */
    default String summary() { return ""; }

    /** Optional category slug used by browsers for filtering / grouping. Default empty. */
    default String category() { return ""; }

    /** MIME type. Default {@code text/markdown}. */
    default String contentType() { return "text/markdown; charset=utf-8"; }

    /** File extension matching the content type, used by the classpath-loading subinterfaces. */
    default String fileExtension() { return ".md"; }

    /**
     * RFC 0015 Phase 3 — the content-kind discriminator. Drives JSON
     * serialization in catalogue/tree responses and routes the request to
     * the registered {@link hue.captains.singapura.js.homing.studio.base.app.ContentViewer}
     * (when Phase 5 lands). Default {@code "doc"} for prose Docs; PlanDoc
     * overrides to {@code "plan"}; AppDoc overrides to {@code "app"};
     * future Doc kinds declare their own.
     *
     * <p>Realises Viewer ontology V4 (Doc routing through kind).</p>
     */
    default String kind() { return "doc"; }

    /**
     * RFC 0015 Phase 3 — the canonical URL the framework uses to address
     * this Doc. Default {@code /app?app=doc-reader&doc=<uuid>} for prose
     * Docs; PlanDoc and AppDoc override to return their respective viewer
     * URLs. Used by catalogue/tree serialization so the JSON payload
     * carries a pre-resolved URL per entry.
     *
     * <p>Realises Viewer ontology V6 (canonical URL composition).</p>
     */
    default String url() { return "/app?app=doc-reader&doc=" + uuid(); }

    /**
     * Typed cross-references and external citations declared by this Doc, rendered by the
     * DocReader as a "References" section beneath the markdown body. Each {@link Reference}
     * is exposed as a stable in-page anchor (id="ref:&lt;name&gt;"); the markdown body cites
     * them via normal links of the form {@code [label](#ref:<name>)}.
     *
     * <p>Per RFC 0004-ext1, every out-of-document URL in the markdown body must resolve to
     * a Reference declared here — otherwise the {@code DocConformanceTest}'s reference scan
     * fails. References declared here that aren't cited inline are valid (they appear in the
     * References section as "further reading" entries).</p>
     *
     * <p>Default: empty.</p>
     */
    default List<Reference> references() { return List.of(); }

    /**
     * Resolve a child Doc by its local-to-this-doc level identifier — the
     * primitive used by the framework's generic tree walker at
     * {@code GET /doc?id=<root>&l1=...&l2=...&l3=...}. Each {@code lN} value
     * is passed to the resolver for the doc at that depth; what counts as a
     * valid identifier is doc-kind-local (ComposedDoc parses int indices;
     * a hypothetical named-children doc kind would dispatch by name).
     *
     * <p>Default: this doc has no children — return empty.</p>
     *
     * <p>The shape lets the framework address embedded sub-docs <em>by
     * containment path</em> instead of by their own UUID; embedded docs
     * don't need separate registration. The act of being referenced in
     * a parent's content IS their addressability.</p>
     *
     * @since RFC 0004 ext — leveled-tree addressing
     */
    default Optional<Doc> resolveChild(String levelId) { return Optional.empty(); }

    /**
     * Render contents with URLs rooted at the given context — used by the
     * {@code /doc} handler after walking a path. Sub-doc URLs emitted in
     * the returned content carry the {@code rootId + pathPrefix} prefix so
     * the client can navigate further without losing the containment root.
     *
     * <p>Default delegates to {@link #contents()} (no leveled-URL emission
     * needed for leaf docs). ComposedDoc overrides to thread the prefix
     * through embedded segment URLs.</p>
     *
     * @param rootId     the {@code id} parameter of the originating request
     *                   (the root of the containment tree the client is
     *                   navigating in)
     * @param pathPrefix the sequence of level identifiers consumed so far
     *                   to reach this doc (a list of {@code lN} values, in
     *                   order — {@code [l1, l2, l3, …]})
     *
     * @since RFC 0004 ext — leveled-tree addressing
     */
    default String contents(String rootId, List<String> pathPrefix) {
        return contents();
    }
}
