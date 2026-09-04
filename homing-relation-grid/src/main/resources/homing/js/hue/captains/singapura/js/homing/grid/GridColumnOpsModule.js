// =============================================================================
// GridColumnOpsModule — RFC 0050-ext6's header operations, CARET tier: the
// grid-side slot and click routing (GridHeaderOps) and the stock provider that
// fills the slot (CaretColumnOps). Contract & Stock, one band up from cells:
// the <th> is layout chrome the grid keeps; what the affordance looks like and
// what a click MEANS is the provider's.
//
// GridHeaderOps — LAYOUT-branch chrome (raw DOM, PRIMITIVE — the GridHeaderDrag
//   posture). For every <th> the layout mints it appends an empty ops SLOT and
//   wires the header body's click, routed POSITIONALLY to the facade exactly as
//   td clicks are — after asking the drag whether this click merely ended a
//   reorder, in which case it is swallowed. render() then hands each visible
//   column's slot to the provider with that column's state and a column-bound,
//   intent-only api: sortBy(direction) / removeSortKey() / pinSortKey(on). The
//   provider never sees a map, a cell, a selection or the ViewState.
//
// CaretColumnOps — the stock 'caret' provider. A direction glyph (↕ while
//   sortable and unsorted), a rank badge once there are two or more keys, and
//   the PIN — multi-key without a modifier. The gesture rules:
//     free key      click cycles asc → desc → none                (ext6 row 8)
//     pinned key    click cycles asc ↔ desc only — the pin is the one way it
//                   leaves, so a mis-click cannot destroy a grouping  (row 24)
//     Alt+↑ / Alt+↓ sort the CURSOR's column; the same chord again clears a
//                   free key (a pinned one only flips)
//     Alt+Shift+↑   toggles the cursor column's pin
//   A column is sortable iff the Relation's meta orders it (columnMeta has a
//   compare): no comparator, no caret, no click. multiKey:false drops the pin.
//
// Predicates asserted on the <th> — ext6's contribution to ext3's vocabulary:
//   hgr-sortable                       mode: the Relation orders this column
//   hgr-sorted-asc | hgr-sorted-desc   the column is a sort key, that way
//   hgr-sort-pinned                    …and pinned — implies one of the above
//
// Styling is a small injected sheet of its own (theme tokens only) rather than
// lines in GridLayout's, which the line ratchet has no room for; ext3's sheet
// split is where the two converge.
// =============================================================================

var _HCO_STYLE_CSS = [
    ".hgr-th-ops{display:inline-flex;align-items:center;gap:4px;margin-left:6px;",
    "  vertical-align:middle;font-size:11px;color:var(--color-text-muted);}",
    ".hgr-th.hgr-sortable{cursor:pointer;}",
    ".hgr-th-caret{display:inline-block;min-width:1em;text-align:center;opacity:0.35;}",
    ".hgr-th.hgr-sorted-asc .hgr-th-caret,.hgr-th.hgr-sorted-desc .hgr-th-caret{",
    "  opacity:1;color:var(--color-accent);}",
    ".hgr-th-rank{font-size:10px;line-height:1.4;padding:0 4px;border-radius:8px;",
    "  background:color-mix(in srgb, var(--color-accent) 18%, transparent);}",
    ".hgr-th-pin{border:0;background:transparent;cursor:pointer;padding:0 2px;",
    "  font:inherit;line-height:1;opacity:0.4;color:inherit;}",
    ".hgr-th-pin:hover,.hgr-th.hgr-sort-pinned .hgr-th-pin{opacity:1;}",
    ""
].join("\n");
var _hcoStyled = false;

/** Once per module load — a flag, not a DOM lookup. */
function _hcoEnsureStyles() {
    if (_hcoStyled || typeof document === "undefined" || !document.head) return;
    _hcoStyled = true;
    var s = document.createElement("style");
    s.textContent = _HCO_STYLE_CSS;
    document.head.appendChild(s);
}
function _hcoAddClass(el, c) {
    var parts = el.className ? el.className.split(" ") : [];
    if (parts.indexOf(c) < 0) el.className = parts.concat(c).join(" ");
}
function _hcoIsResizeHandle(el) {
    return !!(el && typeof el.className === "string"
              && el.className.split(" ").indexOf("hgr-resize-handle") >= 0);
}

class GridHeaderOps {

    /** deps: { host, provider } — host is the facade (columns by position, the
     *  sort verbs, the ViewState's key readers, the bulk session to settle);
     *  provider is the ColumnOpsContract object that fills the slots. */
    constructor(deps) {
        deps = deps || {};
        if (!deps.host || !deps.provider) throw new Error("[GridHeaderOps] host and provider are required");
        this._host = deps.host;
        this._provider = deps.provider;
        this._slots = [];
    }

    /** GridLayout.render() re-mints every <th>; the slots start over. */
    reset() { this._slots = []; return this; }

    /**
     * Called by GridLayout for every <th> it mints: an empty ops slot after the
     * label, and the header body's click routed by position. Two clicks are
     * not clicks: one on the resize handle, and one that merely ended a
     * reorder — the drag marks the header on release and is asked here.
     */
    wire(th, j, drag) {
        var self = this;
        var slot = document.createElement("span");
        slot.className = "hgr-th-ops";
        th.appendChild(slot);
        this._slots[j] = slot;
        th.addEventListener("click", function (e) {
            if (e && _hcoIsResizeHandle(e.target)) return;
            if (drag && drag.consumeDragged(th)) return;
            self._click(j);
        });
        return this;
    }

    slotAt(j) { return this._slots[j] || null; }

    /** After a refresh: every visible column's slot goes to the provider. */
    render() {
        var m = this._host._maps;
        for (var j = 0; j < m.cols(); j++) {
            var slot = this._slots[j], c = m.columnAt(j);
            if (slot) this._provider.renderHeader(slot, c, this._state(c), this._api(c));
        }
        return this;
    }

    _click(j) {
        var c = this._host._maps.columnAt(j);
        if (c === undefined || c === null || c === -1) return;
        var session = this._host._session;
        if (session && session.isActive()) session.settle();         // as a cell click does
        this._provider.onHeaderClick(c, this._state(c), this._api(c));
    }

    /** The cursor column's chord — 'asc' | 'desc' | 'pin' — for providers that take one. */
    key(kind) {
        var cur = this._host._selection.cursorId();
        if (!cur || typeof this._provider.onHeaderKey !== "function") return;
        this._provider.onHeaderKey(cur.column, this._state(cur.column), this._api(cur.column), kind);
    }

    /** What the provider may READ about a column. */
    _state(c) {
        var vs = this._host._vs;
        return { sortable: vs.canSort(c), multiKey: this._host._multiKey,
                 sort: vs.sortKey(c), keyCount: vs.keyCount() };
    }

    /** What the provider may ASK for a column — intents only, column-bound. */
    _api(c) {
        var h = this._host;
        return { sortBy:        function (d)  { h.sortBy(c, d); },
                 removeSortKey: function ()   { h.removeSortKey(c); },
                 pinSortKey:    function (on) { h.pinSortKey(c, on); } };
    }
}

class CaretColumnOps {

    constructor(opts) {
        opts = opts || {};
        this._multiKey = opts.multiKey !== false;
        _hcoEnsureStyles();
    }

    /** Fill a FRESH slot (the layout re-mints the <th> on every view change):
     *  the glyph, the rank once there are two keys, the pin when multi-key. */
    renderHeader(slot, column, state, api) {
        if (!state.sortable) return;
        var th = slot.parentNode, s = state.sort;
        _hcoAddClass(th, "hgr-sortable");
        var caret = document.createElement("span");
        caret.className = "hgr-th-caret";
        caret.textContent = !s ? "↕" : (s.direction === "desc" ? "▼" : "▲");
        slot.appendChild(caret);
        th.setAttribute("aria-sort", !s ? "none" : (s.direction === "desc" ? "descending" : "ascending"));
        if (!s) return;
        _hcoAddClass(th, s.direction === "desc" ? "hgr-sorted-desc" : "hgr-sorted-asc");
        if (s.pinned) _hcoAddClass(th, "hgr-sort-pinned");
        if (state.keyCount > 1) {
            var rank = document.createElement("span");
            rank.className = "hgr-th-rank";
            rank.textContent = String(s.rank + 1);
            rank.setAttribute("aria-label", "sort key " + (s.rank + 1) + " of " + state.keyCount);
            slot.appendChild(rank);
        }
        if (this._multiKey) {
            var pin = document.createElement("button");
            pin.className = "hgr-th-pin";
            pin.setAttribute("type", "button");
            pin.setAttribute("tabindex", "-1");                        // reached by chord, not Tab
            pin.setAttribute("aria-pressed", s.pinned ? "true" : "false");
            pin.setAttribute("aria-label", s.pinned ? "Unpin sort key" : "Pin sort key");
            pin.textContent = "📌";
            // The pin is its own control: it neither sorts nor starts a drag.
            pin.addEventListener("mousedown", function (e) { if (e && e.stopPropagation) e.stopPropagation(); });
            pin.addEventListener("click", function (e) {
                if (e && e.stopPropagation) e.stopPropagation();
                api.pinSortKey(!s.pinned);
            });
            slot.appendChild(pin);
        }
    }

    /** The header body. Free key: asc → desc → none. Pinned: asc ↔ desc only. */
    onHeaderClick(column, state, api) {
        if (!state.sortable) return;
        var s = state.sort;
        if (!s)                    return api.sortBy("asc");
        if (s.direction === "asc") return api.sortBy("desc");
        if (s.pinned)              return api.sortBy("asc");        // two states; the pin is the way out
        return api.removeSortKey();
    }

    /** The cursor column's chord: 'asc' | 'desc' sort it (the same again
     *  clears a free key; a pinned key only flips); 'pin' toggles its pin. */
    onHeaderKey(column, state, api, kind) {
        if (!state.sortable) return;
        var s = state.sort;
        if (kind === "pin") { if (s && this._multiKey) api.pinSortKey(!s.pinned); return; }
        if (s && s.direction === kind && !s.pinned) return api.removeSortKey();
        return api.sortBy(kind);
    }

    dispose() {}
}
