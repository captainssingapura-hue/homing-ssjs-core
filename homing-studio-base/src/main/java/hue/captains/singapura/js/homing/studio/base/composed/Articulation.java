package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.table.TableData;

import java.util.Optional;

/**
 * A per-cell visual <i>articulation</i> for an articulated relation — a small,
 * <b>extensible</b> bundle of orthogonal marks that enhance ONE cell without
 * changing its text. Articulations are attached sparsely, via
 * {@link RelationArticulations} (a side-car to the relation's plain
 * {@code rows}), so a plain relation carries none and its wire shape is
 * unchanged.
 *
 * <p>Current marks — each optional and orthogonal, so a cell may carry any
 * subset:</p>
 * <ul>
 *   <li>{@link TableData.Badge} — a status colour token (success / warning /
 *       error), rendered as an inline pill (reuses the RFC 0020 palette);</li>
 *   <li>{@link TableData.Align} — horizontal alignment (left / center / right);</li>
 *   <li>{@link Emphasis} — text weight/tone (strong / muted).</li>
 * </ul>
 *
 * <p><b>Extending.</b> Adding a new kind of articulation is a three-step change:
 * add an {@code Optional} field here (+ a {@link Builder} setter), emit its key
 * in {@code SegmentJson}'s {@code relation} arm, and map it to a CSS class in
 * {@code RelationSegmentRenderer.js}. Existing data stays valid — an absent mark
 * is the default — so the vocabulary grows without a migration.</p>
 *
 * @param badge    optional status colour
 * @param align    optional horizontal alignment
 * @param emphasis optional text weight/tone
 */
public record Articulation(
        Optional<TableData.Badge> badge,
        Optional<TableData.Align> align,
        Optional<Emphasis>        emphasis) {

    /** Text weight/tone marks. Absence = normal body text. */
    public enum Emphasis { STRONG, MUTED }

    public Articulation {
        badge    = (badge    == null) ? Optional.empty() : badge;
        align    = (align    == null) ? Optional.empty() : align;
        emphasis = (emphasis == null) ? Optional.empty() : emphasis;
    }

    /** True when this articulation carries no marks — nothing to render. */
    public boolean isEmpty() {
        return badge.isEmpty() && align.isEmpty() && emphasis.isEmpty();
    }

    /** Start a fluent articulation; set any subset of marks, then {@link Builder#build()}. */
    public static Builder of() { return new Builder(); }

    /** Fluent builder — orthogonal setters, chainable in any order. */
    public static final class Builder {
        private TableData.Badge badge;
        private TableData.Align align;
        private Emphasis        emphasis;

        public Builder badge(TableData.Badge b) { this.badge = b; return this; }
        public Builder align(TableData.Align a) { this.align = a; return this; }
        public Builder emphasis(Emphasis e)     { this.emphasis = e; return this; }
        public Builder strong()                 { this.emphasis = Emphasis.STRONG; return this; }
        public Builder muted()                  { this.emphasis = Emphasis.MUTED; return this; }

        public Articulation build() {
            return new Articulation(Optional.ofNullable(badge),
                                    Optional.ofNullable(align),
                                    Optional.ofNullable(emphasis));
        }
    }
}
