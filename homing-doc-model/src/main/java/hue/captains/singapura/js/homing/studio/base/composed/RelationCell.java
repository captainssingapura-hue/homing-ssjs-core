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
 * <p>Fluent and terse — chain any subset of marks, each giving a new cell:</p>
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
 *
 * @param text         the plain cell text (goes into the relation's {@code rows})
 * @param articulation the cell's marks — {@link Articulation#isEmpty() empty} when none was set
 */
public record RelationCell(String text, Articulation articulation) {

    public RelationCell {
        Objects.requireNonNull(text, "RelationCell.text (use \"\" for empty)");
        Objects.requireNonNull(articulation, "RelationCell.articulation");
    }

    /** A cell carrying {@code text}; add marks by chaining. */
    public RelationCell(String text) { this(text, Articulation.NONE); }

    // ── Status badge (inline pill) ──────────────────────────────────────────
    /** This cell with this badge. */
    public RelationCell badge(TableData.Badge b) { return new RelationCell(text, articulation.withBadge(b)); }
    /** This cell with the success badge. */
    public RelationCell success() { return badge(TableData.Badge.SUCCESS); }
    /** This cell with the warning badge. */
    public RelationCell warning() { return badge(TableData.Badge.WARNING); }
    /** This cell with the error badge. */
    public RelationCell error()   { return badge(TableData.Badge.ERROR); }

    // ── Horizontal alignment ────────────────────────────────────────────────
    /** This cell aligned this way. */
    public RelationCell align(TableData.Align a) { return new RelationCell(text, articulation.withAlign(a)); }
    /** This cell aligned to the left. */
    public RelationCell left()   { return align(TableData.Align.LEFT); }
    /** This cell centred. */
    public RelationCell center() { return align(TableData.Align.CENTER); }
    /** This cell aligned to the right. */
    public RelationCell right()  { return align(TableData.Align.RIGHT); }

    // ── Emphasis ────────────────────────────────────────────────────────────
    /** This cell with strong emphasis. */
    public RelationCell strong() { return emphasis(Articulation.Emphasis.STRONG); }
    /** This cell with muted emphasis. */
    public RelationCell muted()  { return emphasis(Articulation.Emphasis.MUTED); }

    private RelationCell emphasis(Articulation.Emphasis e) { return new RelationCell(text, articulation.withEmphasis(e)); }
}
