package hue.captains.singapura.js.homing.studio.base.composed;

import java.util.List;
import java.util.Objects;

/**
 * The sparse articulation side-car of an articulated relation — a set of
 * {@link ArticulatedCell}s covering ONLY the cells that need visual
 * enhancement, held separately from (and parallel to) the relation's plain
 * string {@code rows}.
 *
 * <p>Think of it as a second, sparse relation whose "cells" are
 * {@link Articulation}s rather than text: it names a handful of coordinates and
 * the marks each carries, and says nothing about the rest. A plain relation has
 * {@link #NONE}. Because it rides alongside the main relation rather than
 * replacing its cell model, the plain {@code rows} and their wire shape stay
 * untouched — the articulations are emitted only when present.</p>
 *
 * @param cells the articulated cells, in author order (sparse — most cells absent)
 */
public record RelationArticulations(List<ArticulatedCell> cells) {

    /** The empty side-car — a plain relation with no articulated cells. */
    public static final RelationArticulations NONE = new RelationArticulations(List.of());

    public RelationArticulations {
        Objects.requireNonNull(cells, "RelationArticulations.cells");
        cells = List.copyOf(cells);
    }

    /** True when no cell is articulated — the relation renders as a plain table. */
    public boolean isEmpty() { return cells.isEmpty(); }

    /** Build a side-car from a varargs list of articulated cells. */
    public static RelationArticulations of(ArticulatedCell... cells) {
        return new RelationArticulations(List.of(cells));
    }
}
