package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.core.SvgGroup;
import hue.captains.singapura.js.homing.core.SvgRef;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * RFC 0015 Phase 3 + RFC 0016 — Doc subtype wrapping a typed
 * {@link SvgRef}. Lets SVG assets participate in the unified
 * {@link Doc} family as first-class citable, viewable artefacts.
 *
 * <p>Each SvgDoc is identified by a deterministic UUID v3 derived from
 * the SvgRef's classpath resource path, so the same {@code (group, being)}
 * pair always produces the same Doc UUID across rebuilds. The body
 * ({@link #contents()}) is the resolved SVG markup; the URL routes
 * through the framework's registered {@code SvgViewer} AppModule
 * (kind {@code "svg"}; see {@code Fixtures.contentViewers()}).</p>
 *
 * <p>Realises the Viewer ontology's per-kind dispatch by being kind-tagged
 * ({@code "svg"}) and pairing with the registered SvgViewer. Tree leaves
 * that wrap SvgDoc instances render through the standard catalogue Card
 * (the framework's chrome) and open the SvgViewer on click.</p>
 *
 * <p>Usage in a {@code ContentTree}:</p>
 * <pre>{@code
 * new TreeLeaf("turtle", "Turtle", "Slow, steady.", "ANIMAL", "",
 *              new SvgDoc(new SvgRef<>(CuteAnimal.INSTANCE, new CuteAnimal.turtle()),
 *                         "Turtle", "Slow, steady, ancient."));
 * }</pre>
 *
 * @param <G>     the SvgGroup the wrapped being belongs to
 * @param ref     typed SvgRef pointing at the asset
 * @param title   display title (e.g. "Turtle")
 * @param summary one-line summary
 */
public record SvgDoc<G extends SvgGroup<G>>(
        SvgRef<G> ref,
        String    title,
        String    summary
) implements Doc {

    public SvgDoc {
        Objects.requireNonNull(ref,   "SvgDoc.ref");
        Objects.requireNonNull(title, "SvgDoc.title");
        if (summary == null) summary = "";
    }

    @Override public UUID uuid() {
        return UUID.nameUUIDFromBytes(
                ("svg:" + ref.resourcePath()).getBytes(StandardCharsets.UTF_8));
    }

    @Override public DocId id() {
        return new DocId.ByUuid(uuid());
    }

    @Override public String title()       { return title; }
    @Override public String summary()     { return summary; }
    @Override public String category()    { return "SVG"; }
    @Override public String kind()        { return "svg"; }
    @Override public String url() {
        return hue.captains.singapura.js.homing.core.AppUrl.flat(
                "svg-viewer",
                hue.captains.singapura.js.homing.studio.base.app.SvgViewer.CODEC,
                new hue.captains.singapura.js.homing.studio.base.app.SvgViewer.Params(uuid().toString()));
    }
    @Override public String contentType() { return "image/svg+xml"; }
    @Override public String fileExtension() { return ".svg"; }

    /** The resolved SVG markup from the classpath; empty string if the resource is absent. */
    @Override public String contents() {
        return ref.resolve().orElse("");
    }

    @Override public List<Reference> references() {
        return List.of();
    }
}
