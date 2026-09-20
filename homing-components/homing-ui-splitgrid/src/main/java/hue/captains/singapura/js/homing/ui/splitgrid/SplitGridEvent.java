package hue.captains.singapura.js.homing.ui.splitgrid;

import java.util.List;
import java.util.Objects;

/**
 * The closed vocabulary of the grid's arrangement — the sealed sum the JS
 * {@code SplitGridEvents} factories mirror, record for record, component
 * for component. The grid reports each change of arrangement as one frozen
 * object tagged by {@code kind}; {@code SplitGridEventsTest} holds the two
 * together.
 *
 * <p>Four kinds: a split's tracks re-shared, a cell subdivided, a cell
 * removed, and a mirror's cursor moved to a cell. A path names a split by
 * child indexes from the root, joined by {@code /}; the root split's path is
 * empty.</p>
 */
public sealed interface SplitGridEvent {

    /** The kind: the record's simple name, the JS object's {@code kind}. */
    default String kind() { return getClass().getSimpleName(); }

    private static String requireId(String id, String what) {
        Objects.requireNonNull(id, what);
        if (id.isEmpty()) throw new IllegalArgumentException(what + ": must not be empty");
        return id;
    }

    /** A split's children were re-shared, by a divider drag or by {@code setRatios}; the ratios sum to one. */
    record TracksChanged(String path, List<Double> ratios) implements SplitGridEvent {
        public TracksChanged {
            Objects.requireNonNull(path, "TracksChanged.path");
            if (!path.matches("(\\d+(/\\d+)*)?")) throw new IllegalArgumentException("TracksChanged.path: child indexes joined by '/', or empty");
            Objects.requireNonNull(ratios, "TracksChanged.ratios");
            if (ratios.size() < 2) throw new IllegalArgumentException("TracksChanged.ratios: two or more");
            double sum = 0;
            for (Double r : ratios) {
                if (r == null || !(r > 0)) throw new IllegalArgumentException("TracksChanged.ratios: positive numbers");
                sum += r;
            }
            if (Math.abs(sum - 1) > 1e-6) throw new IllegalArgumentException("TracksChanged.ratios: must sum to one");
            ratios = List.copyOf(ratios);
        }
    }

    /** A cell was subdivided: a new, empty cell beside it on the side named — left, right, top or bottom — each taking half the room. */
    record Subdivided(String cellId, String newCellId, String side) implements SplitGridEvent {
        public Subdivided {
            requireId(cellId, "Subdivided.cellId");
            requireId(newCellId, "Subdivided.newCellId");
            Objects.requireNonNull(side, "Subdivided.side");
            if (!List.of("left", "right", "top", "bottom").contains(side)) throw new IllegalArgumentException("Subdivided.side: left, right, top or bottom");
        }
    }

    /** A cell was removed: its room went to its neighbour, and a split left with one child gave way to it. */
    record Removed(String cellId) implements SplitGridEvent {
        public Removed { requireId(cellId, "Removed.cellId"); }
    }

    /** A mirror's cursor is at a cell: moved there by an arrow — left, right, up, down — a pointer, or a call. */
    record CursorMoved(String cellId, String by) implements SplitGridEvent {
        public CursorMoved {
            requireId(cellId, "CursorMoved.cellId");
            Objects.requireNonNull(by, "CursorMoved.by");
            if (!List.of("left", "right", "up", "down", "pointer", "call").contains(by)) throw new IllegalArgumentException("CursorMoved.by: left, right, up, down, pointer or call");
        }
    }
}
