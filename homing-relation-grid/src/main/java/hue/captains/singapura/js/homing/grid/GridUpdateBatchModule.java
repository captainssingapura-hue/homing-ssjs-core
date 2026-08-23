package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — {@code GridUpdateBatch}: the direct update path's queue, split
 * out of the facade by the line ratchet. One concern — coalesce domain-pushed
 * cell updates per animation frame, last write per cell wins, so a hot feed
 * (a popularity tick, a Minesweeper flood fill) collapses to one
 * {@code cell.update()} per cell per frame. Falls back to a synchronous flush
 * where no {@code requestAnimationFrame} exists (headless). {@code drain()}
 * is the read-path guard: a hidden page gets no frames, so anything that
 * READS cell state (copy, export) must drain first or read stale values.
 * No DOM, no maps, no layout.
 */
public record GridUpdateBatchModule() implements DomModule<GridUpdateBatchModule> {

    /** The single export — the {@code GridUpdateBatch} JS class. */
    public record GridUpdateBatch() implements Exportable._Constant<GridUpdateBatchModule> {}

    public static final GridUpdateBatchModule INSTANCE = new GridUpdateBatchModule();

    @Override
    public ImportsFor<GridUpdateBatchModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridUpdateBatchModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridUpdateBatch()));
    }
}
