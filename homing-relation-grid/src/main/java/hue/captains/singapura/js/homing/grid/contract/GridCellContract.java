package hue.captains.singapura.js.homing.grid.contract;

/**
 * RFC 0050 — the substrate-free contract between the table and its cells:
 * the standard API every ActualCell renderer implements. Documentation in
 * code, RFC 0035 style — the JS cell classes conform structurally, and a
 * conformance test gates the match once the stock cells land (Phase 2).
 *
 * <p>The load-bearing rules, from the RFC:</p>
 * <ul>
 *   <li>{@link #onSelect} is <b>pure lifecycle</b> — it never focuses; the
 *       layout places focus (the RFC 0049 caveat-1 lesson).</li>
 *   <li>The three value forms are distinct: {@link #getValue} (raw),
 *       {@link #getValueToCopy} (clipboard text — a formatted price copies as
 *       {@code 12.5}, not {@code "12.50 ▲"}), {@link #getEditValue} (editor
 *       seed).</li>
 *   <li>{@link #dispose} fires only when the row leaves the Relation — never
 *       from any layout operation (detach is not disposal).</li>
 *   <li>Cell → table communication is by <b>bubbling events</b> (the declared-
 *       intent convention): {@code valueChanged} on edit commit; give-up /
 *       request-deep exactly as RFC 0049 widgets do. No handed-in callbacks.</li>
 * </ul>
 */
public interface GridCellContract {

    // ─── Mount + update (table → cell) ───────────────────────────────────
    void render(Object host, Object value, Object meta);   // mount ONCE into the cell's element
    void update(Object value);                             // cheap value refresh (b.2i — no re-mount)

    // ─── Selection lifecycle (table → cell; PURE — never focuses) ────────
    void onSelect(String mode);                            // 'none' | 'shallow' | 'deep'

    // ─── The deep (editing) lifecycle — optional per cell kind ───────────
    void beginEdit(Object host);                           // mount the editor (cell's own sub-branch)
    void commitEdit();                                     // → bubbles valueChanged(newValue)
    void cancelEdit();                                     // discard; back to shallow

    // ─── The action variant (editing-disabled grids; Minesweeper appendix) ─
    void onAction(String key);                             // momentary — fires and stays shallow

    // ─── The three value forms ───────────────────────────────────────────
    Object getValue();                                     // raw domain value
    String getValueToCopy();                               // clipboard text (TSV cell)
    Object getEditValue();                                 // editor seed

    // ─── End of life (ONLY when the row leaves the Relation) ─────────────
    void dispose();

    /** Methods a minimal read-only cell must implement; the rest are optional. */
    String[] REQUIRED_METHODS = { "render", "update", "onSelect", "getValue", "getValueToCopy", "dispose" };

    /** The full method set, for the stock-cell conformance test (Phase 2). */
    String[] ALL_METHODS = { "render", "update", "onSelect", "beginEdit", "commitEdit",
                             "cancelEdit", "onAction", "getValue", "getValueToCopy",
                             "getEditValue", "dispose" };
}
