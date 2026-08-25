package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocId;
import hue.captains.singapura.js.homing.studio.base.Reference;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * RFC 0015 Phase 3 — Doc subtype wrapping an AppModule + typed Params
 * binding (a {@link Navigable}). Lets interactive AppModule "leaves"
 * participate in the unified {@link Doc} family while keeping their
 * existing per-AppModule rendering pipeline.
 *
 * <p>Identity: {@link DocId.ByClassAndParams} keyed by the AppModule's
 * class plus the typed Params value. Two AppDocs binding the same
 * AppModule with different Params values are different Docs.
 * The {@link #uuid()} is derived deterministically from
 * {@code (app simpleName, params.toString())} via UUID v3 so legacy
 * UUID-keyed paths see a stable identifier — but the canonical id is
 * the typed {@code DocId.ByClassAndParams} variant.</p>
 *
 * <p>Body: empty. AppDocs are not served through {@code /doc?id=…} —
 * their viewer is the bound AppModule at the URL returned by
 * {@link #url()} (delegates to {@link Navigable#url()}). The body string
 * is part of the {@link Doc} protocol for prose Docs; for AppDoc it
 * stays empty.</p>
 *
 * <p>Phase 3a: the record is defined but no Entry factory yet wraps
 * incoming Navigables in AppDoc. Phase 3b rewires the factories; until
 * then AppDoc is available for opt-in use by callers who want the
 * unified Doc identity.</p>
 *
 * @param <P> typed Params type bound by the wrapped Navigable
 * @param <M> AppModule self-type bound by the wrapped Navigable
 * @since RFC 0015 Phase 3
 */
public record AppDoc<P extends AppModule._Param, M extends AppModule<P, M>>(
        Navigable<P, M> nav
) implements Doc {

    public AppDoc {
        Objects.requireNonNull(nav, "AppDoc.nav");
    }

    @Override public UUID uuid() {
        // Seed from the full Navigable's toString — captures app, params, name,
        // summary so two AppDocs with the same (app, params) but different
        // display framings get distinct UUIDs. Two AppDocs wrapping equal
        // Navigable records (placed in two catalogues) share a UUID and
        // collapse harmlessly via DocRegistry's record-equality collision check.
        return UUID.nameUUIDFromBytes(
                ("appdoc:" + nav.toString()).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * RFC 0051 Law 2. Every AppDoc shares this one class, so the inherited
     * class-derived slug would collide the moment a catalogue holds two app
     * tiles. The app's {@code simpleName} is closer, but still collides when
     * one catalogue mounts the same app twice under different params — which
     * the demo does with {@code TreeAppHost}. The tile's display name is the
     * only per-tile datum, and it is already required to be distinct enough
     * for a human to tell the tiles apart.
     */
    @Override public NodeName slug() {
        return NodeName.conciseSlug(nav.name());
    }

    @Override public DocId id() {
        // The whole Navigable participates in identity — its `params` field
        // alone is insufficient because two Navigables can share (app, params)
        // yet differ on display name/summary, which the framework treats as
        // distinct catalogue tiles. The {@code params} slot of ByClassAndParams
        // is documented as "opaque value that disambiguates instances of this
        // class"; for AppDoc, that disambiguator is the full Navigable record.
        return new DocId.ByClassAndParams(nav.app().getClass(), nav);
    }

    @Override public String title()    { return nav.name(); }
    @Override public String summary()  { return nav.summary(); }
    @Override public String category() { return "APP"; }
    @Override public String kind()     { return "app"; }

    @Override public String url() { return nav.url(); }

    /** AppDocs have no markdown body; viewer is the wrapped AppModule. */
    @Override public String contents() { return ""; }

    @Override public String contentType()   { return ""; }
    @Override public String fileExtension() { return ""; }

    @Override public List<Reference> references() { return List.of(); }
}
