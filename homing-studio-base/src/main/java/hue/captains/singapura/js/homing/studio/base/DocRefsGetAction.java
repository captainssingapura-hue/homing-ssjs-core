package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /doc-refs?id=<uuid>} — serves the typed {@link Reference}s declared by a Doc as
 * a JSON list, consumed by {@code DocReaderRenderer.js} to render the References section.
 *
 * <p>Per <a href="../../../../../../../../../../docs/rfcs/Rfc0004Ext1Doc.md">RFC 0004-ext1</a>,
 * the DocReader page renders two parts: the markdown body (fetched from {@code /doc?id=<uuid>})
 * and the References section (fetched from this endpoint). Two parallel fetches; the renderer
 * concatenates the results.</p>
 *
 * <p>Response JSON shape: an array of objects, each with a {@code kind} discriminator plus
 * subtype-specific fields:</p>
 *
 * <pre>{@code
 * [
 *   { "kind": "doc",      "name": "pcv",       "uuid": "...", "title": "...", "summary": "..." },
 *   { "kind": "external", "name": "css-spec",  "url":  "...", "label": "...", "description": "..." },
 *   { "kind": "image",    "name": "arch",      "resourcePath": "...", "alt": "...", "caption": "..." }
 * ]
 * }</pre>
 *
 * <p>Empty array when the Doc has no declared references; 404 for unknown / malformed UUIDs.</p>
 *
 * @since RFC 0004-ext1
 */
public class DocRefsGetAction
        implements GetAction<RoutingContext, DocRefsGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

    private final DocRegistry registry;

    public DocRefsGetAction(DocRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("id"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String raw = query.id();
        if (raw == null || raw.isBlank()) {
            return CompletableFuture.failedFuture(notFound("id", "Required query parameter 'id' was not provided"));
        }
        UUID id;
        try {
            id = UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return CompletableFuture.failedFuture(notFound(raw, "Malformed UUID"));
        }
        Doc doc = registry.resolve(id);
        if (doc == null) {
            return CompletableFuture.failedFuture(notFound(raw, "No Doc registered with this UUID"));
        }
        try {
            return CompletableFuture.completedFuture(
                    new DocContent(serialize(doc), "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(raw, "Failed to serialise references: " + e.getMessage()));
        }
    }

    /**
     * Serialise the Doc's metadata + references as JSON.
     * Shape: {@code { "title": "...", "category": "...", "references": [...] }}.
     *
     * <p>Two fields have left this payload, each for its own reason, and the
     * inventory (RFC 0015 appendix) records both:</p>
     * <ul>
     *   <li>{@code breadcrumbs} — the trail is stamped into the page by the
     *       server that rendered it, so no client asks for its own position.</li>
     *   <li>{@code summary} — measured to have zero readers in any of the three
     *       repos. It was emitted on every response and consumed by nobody.
     *       (The {@code summary} inside each doc REFERENCE stays: that one is
     *       rendered on the reference card.)</li>
     * </ul>
     *
     * <p>What is left is what only the doc can answer. {@code title} still
     * duplicates the stamped leaf crumb, and {@code category} is placement
     * framing rather than doc data — both move under phase 6, after which
     * this is a references endpoint and nothing else.</p>
     */
    static String serialize(Doc doc) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"title\":")    .append(jstr(doc.title())).append(',');
        sb.append("\"category\":") .append(jstr(doc.category())).append(',');
        sb.append("\"references\":");
        sb.append(serializeReferences(doc.references()));
        sb.append('}');
        return sb.toString();
    }

    /** Serialise a list of References to a compact JSON array. */
    static String serializeReferences(List<Reference> refs) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Reference r : refs) {
            if (!first) sb.append(',');
            first = false;
            sb.append('{');
            switch (r) {
                case DocReference dr -> {
                    sb.append("\"kind\":\"doc\",");
                    sb.append("\"name\":")    .append(jstr(dr.name())).append(',');
                    sb.append("\"uuid\":")    .append(jstr(dr.target().uuid().toString())).append(',');
                    // RFC 0051 — the reference's address as (app, args), taken
                    // from the TARGET's own url(). A client holds only a UUID
                    // and a UUID carries no kind, so every emitter that built
                    // this itself guessed doc-reader and opened composed docs
                    // in the markdown viewer. The target knows its viewer; the
                    // client should never have been deciding.
                    //
                    // Emitted as the flat form on purpose rather than as a
                    // path: /app is the generic redirect, so one mechanism
                    // resolves every (app, args) to its authentic address, and
                    // this payload needs no opinion about the tree.
                    sb.append("\"url\":")     .append(jstr(hue.captains.singapura.js.homing.studio.base.app.GotoNavigableGetAction.hrefFor(
                            hue.captains.singapura.js.homing.studio.base.app.DocViewers.addressOf(dr.target()).flat()))).append(',');
                    sb.append("\"title\":")   .append(jstr(dr.target().title())).append(',');
                    sb.append("\"summary\":") .append(jstr(dr.target().summary()));
                }
                case ExternalReference er -> {
                    sb.append("\"kind\":\"external\",");
                    sb.append("\"name\":")        .append(jstr(er.name())).append(',');
                    sb.append("\"url\":")         .append(jstr(er.url())).append(',');
                    sb.append("\"label\":")       .append(jstr(er.label())).append(',');
                    sb.append("\"description\":") .append(jstr(er.description()));
                }
                case ImageReference ir -> {
                    sb.append("\"kind\":\"image\",");
                    sb.append("\"name\":")         .append(jstr(ir.name())).append(',');
                    sb.append("\"resourcePath\":") .append(jstr(ir.resourcePath())).append(',');
                    sb.append("\"alt\":")          .append(jstr(ir.alt())).append(',');
                    sb.append("\"caption\":")      .append(jstr(ir.caption()));
                }
            }
            sb.append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String jstr(String v) {
        if (v == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
