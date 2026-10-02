package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.table.TableData;

import java.util.Objects;

/**
 * An <b>authoring</b> cell for the {@code .articulatedRelation(...)} DSL: a
 * cell's text with its {@link Articulation} attached <i>in place</i>. The
 * builder derives the sparse {@link RelationArticulations} side-car from where
 * each cell sits — the author never tracks {@code (row, col)} indices; the
 * position in the headers/rows lists <i>is</i> the coordinate.
 *
 * <p>Fluent and terse — chain any subset of marks:</p>
 * <pre>{@code
 * cell("homing-core").strong()
 * cell("passing").success()
 * cell("94%").right()
 * cell("71%").right().muted()
 * }</pre>
 *
 * <p>This is purely an authoring convenience over the underlying data model
 * ({@link RelationSegment} + {@link RelationArticulations}); it holds no
 * coordinate itself. Static-import {@link #cell(String)} for the terse form.</p>
 */
public final class RelationCell {

    private final String text;
    private TableData.Badge      badge;
    private TableData.Align      align;
    private Articulation.Emphasis emphasis;

    private RelationCell(String text) {
        this.text = Objects.requireNonNull(text, "RelationCell.text (use \"\" for empty)");
    }

    /** A cell carrying {@code text}; add marks by chaining. */
    public static RelationCell cell(String text) { return new RelationCell(text); }

    // ── Status badge (inline pill) ──────────────────────────────────────────
    public RelationCell badge(TableData.Badge b) { this.badge = b; return this; }
    public RelationCell success() { this.badge = TableData.Badge.SUCCESS; return this; }
    public RelationCell warning() { this.badge = TableData.Badge.WARNING; return this; }
    public RelationCell error()   { this.badge = TableData.Badge.ERROR;   return this; }

    // ── Horizontal alignment ────────────────────────────────────────────────
    public RelationCell align(TableData.Align a) { this.align = a; return this; }
    public RelationCell left()   { this.align = TableData.Align.LEFT;   return this; }
    public RelationCell center() { this.align = TableData.Align.CENTER; return this; }
    public RelationCell right()  { this.align = TableData.Align.RIGHT;  return this; }

    // ── Emphasis ────────────────────────────────────────────────────────────
    public RelationCell strong() { this.emphasis = Articulation.Emphasis.STRONG; return this; }
    public RelationCell muted()  { this.emphasis = Articulation.Emphasis.MUTED;  return this; }

    /** The plain cell text (goes into the relation's {@code rows}). */
    public String text() { return text; }

    /** The cell's articulation — {@link Articulation#isEmpty() empty} when no mark was set. */
    public Articulation articulation() {
        return Articulation.of().badge(badge).align(align).emphasis(emphasis).build();
    }
}
