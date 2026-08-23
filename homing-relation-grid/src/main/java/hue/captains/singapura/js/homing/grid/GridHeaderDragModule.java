package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code GridHeaderDrag} — the header's two pointer gestures, split out of
 * {@code GridLayout} when the 250-line ratchet forced it: STAGED column
 * resize via the 8px right-edge handle (RFC 0050-ext2 — guide line, commit
 * on release, Escape abandons, D7 structural) and drag-REORDER on the header
 * body (RFC 0050-ext1 row 17 — 4px threshold, dimmed dragged header, drop
 * boundary on the same guide, commit on release). The handle stops
 * propagation, so the gestures cannot collide. Positional only — emits
 * {@code (j, …)}; the facade translates to identity.
 */
public record GridHeaderDragModule() implements DomModule<GridHeaderDragModule> {

    /** The single export — the {@code GridHeaderDrag} JS class. */
    public record GridHeaderDrag() implements Exportable._Constant<GridHeaderDragModule> {}

    public static final GridHeaderDragModule INSTANCE = new GridHeaderDragModule();

    @Override
    public ImportsFor<GridHeaderDragModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridHeaderDragModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridHeaderDrag()));
    }
}
