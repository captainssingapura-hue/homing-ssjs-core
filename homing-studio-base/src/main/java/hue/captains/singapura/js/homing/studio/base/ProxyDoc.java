package hue.captains.singapura.js.homing.studio.base;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * RFC 0015 Phase 4 — Doc subtype that delegates content to another Doc
 * while carrying its own identity, home, and optional metadata overrides.
 *
 * <p>Realises Doc ontology axiom A6: multi-home is not a property of a
 * single Doc. The same canonical content appearing in two trees is
 * realised by two Docs (the original plus one or more ProxyDocs), each
 * with its own UUID and its own single home. The framework's single-home
 * invariant is preserved by construction — each appearance is a fresh
 * identity.</p>
 *
 * <p>Server-side resolution is transparent. {@link DocGetAction} serves
 * the proxy's URL ({@code /doc?id=<proxy-uuid>}); {@link #contents()}
 * delegates to the target so the response body comes from the canonical
 * content; metadata (title, summary, category) come from the proxy
 * (when overridden) or from the target (when not). The viewer sees what
 * looks like a regular Doc with the proxy's UUID and the proxy-or-target
 * framing.</p>
 *
 * <p><b>Phase 4 constraint: prose targets only.</b> A ProxyDoc target
 * must be a prose Doc (kind == "doc"). Proxying PlanDoc or AppDoc would
 * require viewer-level UUID overlay (the /plan and /app URL grammars
 * don't carry a UUID slot today); deferred until a concrete need
 * surfaces. Non-prose multi-home in the meantime: register the same
 * Plan or Navigable in multiple catalogues — the existing value-equality
 * harvest collapses duplicates into one identity.</p>
 *
 * <p><b>Chain depth = 1.</b> A ProxyDoc's target must not itself be a
 * ProxyDoc. Validated at construction; eliminates cycle-detection
 * complexity at zero practical cost (proxy-of-proxy adds no expressive
 * power that direct proxying doesn't already cover).</p>
 *
 * <p><b>Override scope.</b> Title, summary, category are overridable —
 * the same canonical content can be "API Reference" in one tree and
 * "How to use the API" in another. Body content is never overridable
 * (defeats the point). References come from the target (changing what
 * a doc cites would mislead readers).</p>
 *
 * @param uuid             this proxy's own UUID (distinct from target's)
 * @param target           the canonical Doc this proxy delegates to;
 *                         must not be another ProxyDoc; must be prose
 * @param titleOverride    optional title; falls through to target's title
 * @param summaryOverride  optional summary; falls through to target's summary
 * @param categoryOverride optional category; falls through to target's category
 *
 * @since RFC 0015 Phase 4
 */
public record ProxyDoc(
        UUID             uuid,
        Doc              target,
        Optional<String> titleOverride,
        Optional<String> summaryOverride,
        Optional<String> categoryOverride
) implements Doc {

    public ProxyDoc {
        Objects.requireNonNull(uuid,             "ProxyDoc.uuid");
        Objects.requireNonNull(target,           "ProxyDoc.target");
        Objects.requireNonNull(titleOverride,    "ProxyDoc.titleOverride (use Optional.empty)");
        Objects.requireNonNull(summaryOverride,  "ProxyDoc.summaryOverride (use Optional.empty)");
        Objects.requireNonNull(categoryOverride, "ProxyDoc.categoryOverride (use Optional.empty)");

        // Chain depth = 1. Proxy-of-proxy is rejected at construction;
        // eliminates cycle-detection complexity at zero practical cost.
        if (target instanceof ProxyDoc) {
            throw new IllegalArgumentException(
                    "ProxyDoc.target must not itself be a ProxyDoc — chain depth limit is 1");
        }

        // Phase 4 constraint: prose targets only. The /doc endpoint's UUID
        // grammar is the only viewer URL that can carry the proxy's identity
        // today. Non-prose proxying lands when /plan and /app gain UUID-overlay
        // support (deferred — no current consumer needs it).
        if (!"doc".equals(target.kind())) {
            throw new IllegalArgumentException(
                    "ProxyDoc.target must be a prose Doc (kind=\"doc\"); got kind=\""
                  + target.kind() + "\" from " + target.getClass().getName()
                  + ". Non-prose proxying is deferred in Phase 4.");
        }
    }

    /** Convenience constructor — no overrides; delegates display fields to target. */
    public ProxyDoc(UUID uuid, Doc target) {
        this(uuid, target, Optional.empty(), Optional.empty(), Optional.empty());
    }

    // -----------------------------------------------------------------------
    // Identity — proxy's own UUID. The target's UUID is invisible to consumers
    // resolving via the proxy URL.
    // -----------------------------------------------------------------------

    @Override public UUID  uuid() { return uuid; }
    @Override public DocId id()   { return new DocId.ByUuid(uuid); }

    // -----------------------------------------------------------------------
    // Display fields — override or fall through to target.
    // -----------------------------------------------------------------------

    @Override public String title()    { return titleOverride.orElseGet(target::title); }
    @Override public String summary()  { return summaryOverride.orElseGet(target::summary); }
    @Override public String category() { return categoryOverride.orElseGet(target::category); }

    // -----------------------------------------------------------------------
    // Content — always delegated to target. Body / references / contentType /
    // fileExtension all come from the canonical content. Per Doc A6, the
    // proxy is a framing of the same content, not a fork.
    // -----------------------------------------------------------------------

    @Override public String contents()      { return target.contents(); }
    @Override public String contentType()   { return target.contentType(); }
    @Override public String fileExtension() { return target.fileExtension(); }

    @Override public List<Reference> references() { return target.references(); }

    // -----------------------------------------------------------------------
    // Routing — proxy's own URL (carries proxy's UUID).
    // -----------------------------------------------------------------------

    @Override public String kind() { return target.kind(); }
    @Override public String url()  {
        // /app?app=doc-reader&doc=<proxy-uuid> — Phase 4 restricts target to
        // prose, so the DocReader viewer is correct for the kind.
        return hue.captains.singapura.js.homing.core.AppUrl.flat(
                "doc-reader",
                hue.captains.singapura.js.homing.studio.base.app.DocReader.CODEC,
                new hue.captains.singapura.js.homing.studio.base.app.DocReader.Params(uuid.toString()));
    }
}
