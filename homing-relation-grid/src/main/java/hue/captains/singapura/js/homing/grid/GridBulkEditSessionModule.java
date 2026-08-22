package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — the VIRTUAL bulk-edit session: value replacement over a
 * homogeneous selection with NO input element. Pure state
 * ({@code {type, targets, buffer, caret}}), so the table never loses native
 * focus and every cell stays SHALLOW — the session, not a cell, owns the
 * keyboard while active. Printable chars insert at the caret, arrows move it
 * (cells render the caret marker — no textbox), every change validates
 * through the EffectiveType (invalid fans the error paint across the
 * selection), Enter commits a valid buffer as one {@code adapter.update} per
 * target, Escape cancels. Instantaneous types commit on the opening
 * keystroke — how game grids ride the same primitive. A type-name mismatch
 * on an opening key REPORTS an error (onRejected) instead of silence.
 */
public record GridBulkEditSessionModule() implements DomModule<GridBulkEditSessionModule> {

    /** The single export — the {@code GridBulkEditSession} JS class. */
    public record GridBulkEditSession() implements Exportable._Constant<GridBulkEditSessionModule> {}

    public static final GridBulkEditSessionModule INSTANCE = new GridBulkEditSessionModule();

    @Override
    public ImportsFor<GridBulkEditSessionModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridBulkEditSessionModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridBulkEditSession()));
    }
}
