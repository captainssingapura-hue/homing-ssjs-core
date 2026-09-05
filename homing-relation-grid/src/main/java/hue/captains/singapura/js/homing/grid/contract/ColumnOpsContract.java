package hue.captains.singapura.js.homing.grid.contract;

/**
 * RFC 0050-ext6 — the contract between the grid and a COLUMN-OPS provider:
 * the object that fills the header's affordance and decides what a click on
 * the header means. Documentation in code, RFC 0035 style; the JS providers
 * conform structurally. Contract &amp; Stock, one band up from
 * {@link GridCellContract}: the {@code 'caret'} tier is the stock
 * implementation and nothing in the grid privileges it.
 *
 * <p>The split, from the appendix: the grid keeps the {@code <th>} (layout
 * chrome), the sort MODEL (keys, comparators, ViewState) and the gesture
 * ROUTING — it wires the header click itself and routes it by position, as it
 * routes cell clicks, which is also where a click that merely ended a reorder
 * drag is swallowed. The provider owns the affordance's rendering and the
 * meaning of a click or a chord, and reaches the grid only through the
 * intent-only {@code api} it is handed.</p>
 *
 * <p>{@code state} is read-only: {@code { sortable, sort: {direction, rank,
 * pinned} | null, keyCount, multiKey }} — {@code sortable} is true iff the
 * Relation's meta orders the column. {@code api} is column-bound:
 * {@code { sortBy(direction), removeSortKey(), pinSortKey(on) }}.</p>
 */
public interface ColumnOpsContract {

    // ─── The header affordance (grid → provider) ─────────────────────────
    void renderHeader(Object slot, ColumnName column, Object state, Object api);  // fill a FRESH grid-minted slot inside the <th>

    // ─── Gestures the grid routed (grid → provider; the provider answers with intents) ──
    void onHeaderClick(ColumnName column, Object state, Object api);             // the header body was clicked
    void onHeaderKey(ColumnName column, Object state, Object api, String kind);  // the cursor column's chord: 'asc' | 'desc' | 'pin' (optional)

    // ─── Tiers with a panel (ext6 'excel' — not yet built) ───────────────
    void renderMenu(Object host, ColumnName column, Object state, Object api);   // optional; absent ⇒ no panel
    void closeMenu(ColumnName column);                                           // optional

    // ─── End of life ─────────────────────────────────────────────────────
    void dispose();

    /** What a provider must implement; the rest is optional. */
    String[] REQUIRED_METHODS = { "renderHeader", "onHeaderClick", "dispose" };

    /** The full surface. */
    String[] ALL_METHODS = { "renderHeader", "onHeaderClick", "onHeaderKey", "renderMenu", "closeMenu", "dispose" };

    /** The stock tiers the facade's {@code columnOps} option names; {@code 'none'} leaves the header inert. */
    String[] STOCK_TIERS = { "caret" };
}
