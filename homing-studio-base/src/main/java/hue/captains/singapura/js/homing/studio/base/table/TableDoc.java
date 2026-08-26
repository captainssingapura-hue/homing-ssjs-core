package hue.captains.singapura.js.homing.studio.base.table;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocId;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * RFC 0020 — first-class Doc subtype carrying a {@link TableData}.
 * Kind = {@code "table"}; viewer is {@code TableViewer} (extends
 * {@link hue.captains.singapura.js.homing.studio.base.app.DocViewer DocViewer}).
 *
 * <p>Like {@link hue.captains.singapura.js.homing.studio.base.SvgDoc SvgDoc},
 * every TableDoc is a registered, citable, addressable artifact — even when
 * primarily used inline as a {@code TableSegment} target in a
 * {@link hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc
 * ComposedDoc}.</p>
 *
 * <p>Identity is supplied explicitly: pass a constant UUID, or use the
 * {@link #deterministicUuid(String)} helper to derive one from a stable
 * seed (e.g., {@code "phase-status:0.0.103"}).</p>
 *
 * <p>{@link #contents()} returns the JSON envelope; the framework's
 * polymorphic doc router serves it via {@code /doc?id=<uuid>} and
 * {@code TableViewer} fetches it client-side and builds the {@code <table>}
 * using the framework's themed cell CSS (RFC 0017 token discipline).</p>
 *
 * @param uuid    durable wire identity
 * @param title   display title
 * @param summary one-line summary (rendered in the meta line)
 * @param data    the typed table payload
 *
 * @since RFC 0020
 */
public record TableDoc(UUID uuid, String title, String summary, TableData data) implements Doc {

    public TableDoc {
        Objects.requireNonNull(uuid,  "TableDoc.uuid");
        Objects.requireNonNull(title, "TableDoc.title");
        Objects.requireNonNull(data,  "TableDoc.data");
        if (summary == null) summary = "";
        if (title.isBlank()) {
            throw new IllegalArgumentException("TableDoc.title must not be blank");
        }
    }

    @Override public DocId  id()            { return new DocId.ByUuid(uuid); }
    @Override public String title()         { return title; }
    @Override public String summary()       { return summary; }
    @Override public String category()      { return "TABLE"; }
    @Override public String kind()          { return "table"; }
    @Override public String url() {
        return hue.captains.singapura.js.homing.core.AppUrl.flat(
                "table-viewer", TableViewer.CODEC, new TableViewer.Params(uuid.toString()));
    }
    @Override public String contentType()   { return "application/json; charset=utf-8"; }
    @Override public String fileExtension() { return ""; }

    @Override public String contents() {
        return data.toJson();
    }

    /** Deterministic UUID derivation for code-defined TableDocs. */
    public static UUID deterministicUuid(String seed) {
        return UUID.nameUUIDFromBytes(("table:" + seed).getBytes(StandardCharsets.UTF_8));
    }
}
