package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — {@code GridEditController}: deep mode and the single keyboard
 * dispatch point. At most one editing cell (deep ⇒ cell); while editing the
 * controller owns the keyboard (Enter commits to the ADAPTER, Escape cancels,
 * everything else stays with the editor input); idle keys flow through the
 * shallow keyboard, unconsumed Enter/F2 begins an edit, idle Escape requests
 * release to the host (RFC 0049 citizenship). With editing disabled, action
 * keys dispatch {@code onAction(key, pk, column)} and return to shallow —
 * the Minesweeper variant.
 *
 * <p>D7 lives here: view ops arriving during an edit queue through
 * {@code defer()} and drain on commit/cancel — a re-sort never tears the
 * editor out from under the user. No DOM in this module: cells render their
 * own editors.</p>
 */
public record GridEditControllerModule() implements DomModule<GridEditControllerModule> {

    /** The single export — the {@code GridEditController} JS class. */
    public record GridEditController() implements Exportable._Constant<GridEditControllerModule> {}

    public static final GridEditControllerModule INSTANCE = new GridEditControllerModule();

    @Override
    public ImportsFor<GridEditControllerModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridEditControllerModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridEditController()));
    }
}
