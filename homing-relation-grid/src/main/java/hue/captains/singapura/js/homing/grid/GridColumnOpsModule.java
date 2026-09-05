package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050-ext6 — header operations, the CARET tier. Two classes, one band up
 * from the cell contract: {@code GridHeaderOps} is the grid's half — the ops
 * slot appended to every {@code <th>} the layout mints, the header-body click
 * routed by position exactly as {@code <td>} clicks are (and swallowed when it
 * merely ended a reorder drag), and the hand-off of each visible column's slot
 * to a provider with that column's state and a column-bound, intent-only api;
 * {@code CaretColumnOps} is the stock provider — direction glyph, rank badge
 * once there are two or more keys, and the PIN that gives the tier multi-key
 * without a modifier. A pinned header cycles asc ↔ desc only; the free key
 * cycles asc → desc → none; Alt+↑/↓ sort the cursor's column and Alt+Shift+↑
 * pins it. A column is sortable iff the Relation's meta orders it.
 *
 * <p>PRIMITIVE: raw DOM inside the layout's own {@code <th>}, the
 * {@code GridHeaderDrag} posture. It asserts four predicates on the header —
 * {@code hgr-sortable}, {@code hgr-sorted-asc} / {@code -desc},
 * {@code hgr-sort-pinned} — ext6's contribution to ext3's vocabulary.</p>
 */
public record GridColumnOpsModule() implements DomModule<GridColumnOpsModule> {

    public record GridHeaderOps()  implements Exportable._Constant<GridColumnOpsModule> {}
    public record CaretColumnOps() implements Exportable._Constant<GridColumnOpsModule> {}

    public static final GridColumnOpsModule INSTANCE = new GridColumnOpsModule();

    @Override
    public ImportsFor<GridColumnOpsModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridColumnOpsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridHeaderOps(), new CaretColumnOps()));
    }
}
