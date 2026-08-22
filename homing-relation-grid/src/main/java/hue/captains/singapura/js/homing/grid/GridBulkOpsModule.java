package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — {@code GridBulkOps}: the selection materialised to IDENTITY
 * sets, then copy and delete over them; the domain never sees {@code (i, j)}.
 * {@code copyTsv()} serialises the active rect (last range, else the cursor)
 * as raw-value TSV via each cell's {@code getValueToCopy()} — draining the
 * rAF batch first, so a read-path never sees stale cell state.
 * {@code deleteSelectedRows()} hands the PK set to
 * {@code adapter.deleteRows}, then retires the rows in one squelched batch —
 * cells die (the only death), selection self-heals through the recompute.
 * No DOM in this module.
 */
public record GridBulkOpsModule() implements DomModule<GridBulkOpsModule> {

    /** The single export — the {@code GridBulkOps} JS class. */
    public record GridBulkOps() implements Exportable._Constant<GridBulkOpsModule> {}

    public static final GridBulkOpsModule INSTANCE = new GridBulkOpsModule();

    @Override
    public ImportsFor<GridBulkOpsModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridBulkOpsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridBulkOps()));
    }
}
