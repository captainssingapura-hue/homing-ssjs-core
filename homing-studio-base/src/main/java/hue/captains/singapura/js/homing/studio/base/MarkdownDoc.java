package hue.captains.singapura.js.homing.studio.base;

import java.util.UUID;

/**
 * The simplest renderable {@link Doc}: a raw markdown string plus metadata, in
 * one instantiable record. No companion {@code .md} file, no classpath
 * convention, no interface to implement — just
 * {@code new MarkdownDoc(uuid, title, body)}.
 *
 * <p>This is the cheap wrapper for two cases the framework now serves directly
 * in the workspace (kind {@code "doc"} → {@code MarkdownDocNormalizer} →
 * {@code DocContentWidget}):</p>
 * <ul>
 *   <li><b>Legacy / external markdown</b> — content that already exists as
 *       {@code .md} and is too costly to migrate to a {@code ComposedDoc}. Read
 *       the file and wrap it: {@code new MarkdownDoc(id, title, Files.readString(p))}.</li>
 *   <li><b>Agent skills</b> — a {@code SKILL.md} surfaced for reading in the
 *       studio: wrap its text so it renders foldably in the Document pane with
 *       no authoring ceremony.</li>
 * </ul>
 *
 * <p>Unlike {@link InlineDoc} (a bare interface you must implement) this is a
 * concrete value — construct it inline. It is <b>not</b> deprecated: for raw /
 * imported markdown a {@code ComposedDoc} would be needless ceremony; this is
 * the supported one-shot path. Reach for {@code ComposedDoc} only when you want
 * typed references or segment composition.</p>
 *
 * <p>Kind is the default {@code "doc"}, so it renders flat (its own markdown
 * headings become the in-body structure; the doc is a single TOC node).</p>
 *
 * @param uuid     wire-stable identity (generate once, never change)
 * @param title    display title
 * @param summary  one-line summary for catalogue cards (may be empty)
 * @param category category slug for filtering (may be empty)
 * @param body     the raw markdown
 * @since homing-studio-base — markdown-in-workspace
 */
public record MarkdownDoc(UUID uuid, String title, String summary, String category, String body)
        implements Doc {

    public MarkdownDoc {
        if (uuid == null)  throw new IllegalArgumentException("MarkdownDoc.uuid");
        if (title == null) throw new IllegalArgumentException("MarkdownDoc.title");
        if (body == null)  throw new IllegalArgumentException("MarkdownDoc.body");
        summary  = summary  == null ? "" : summary;
        category = category == null ? "" : category;
    }

    /** Convenience — title + body only (empty summary / category). */
    public MarkdownDoc(UUID uuid, String title, String body) {
        this(uuid, title, "", "", body);
    }

    /** The raw markdown — the {@link Doc} content seam. */
    @Override public String contents() { return body; }
}
