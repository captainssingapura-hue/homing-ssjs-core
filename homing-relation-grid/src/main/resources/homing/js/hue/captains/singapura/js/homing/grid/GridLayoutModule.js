// =============================================================================
// GridLayoutModule — RFC 0050's LAYOUT branch: the structural chrome. Owns the
// <table> skeleton, the header band, and the <td> CellSlot hosts (the slot the
// selection will paint on — the FocusManager-wrapper pattern). Positional
// only: this class addresses everything by (i, j) and NEVER sees a PK or a
// column name as identity — headers arrive as display labels.
//
// It also never touches cell content: a slot's children belong to the cells
// branch; render() rebuilds the slot matrix and the facade re-places content
// afterwards (one appendChild per cell — b.2i, state preserved).
//
// It also owns the VIEWPORT FOLLOW: the selection says which slot to reveal,
// this class does the scrolling, because the scrollport is DOM and the
// selection is forbidden to touch it.
//
// Like MultiTabPane / SplitPane, this primitive uses raw DOM internally (no
// DomOpsParty) so it stays portable; it is declared PRIMITIVE in the crate.
// Styling is an injected stylesheet with theme tokens only (the MTP pattern).
//
//   new GridLayout({ container }).render({ headers, rows });  … .destroy();
// =============================================================================

var _HGR_STYLE_ID = "homing-relation-grid-style";
var _HGR_STYLE_CSS = [
    ".hgr-table{border-collapse:collapse;width:100%;",
    "  background:var(--color-surface);color:var(--color-text-primary);",
    "  font:13px sans-serif;",
    // The grid is a CONTROL, not prose: native text selection only fights the
    // cell-selection model (and any header drag turns into blue smears).
    // Copy is Ctrl+C via the grid's own clipboard path. Editors re-enable it.
    "  user-select:none;-webkit-user-select:none;}",
    ".hgr-table input,.hgr-table select{user-select:text;-webkit-user-select:text;}",
    // Header borders as INSET BOX-SHADOW, not border: with border-collapse,
    // Chromium drops cell borders on position:sticky headers while scrolling —
    // shadows ride the cell, so the separators stay visible.
    // Stickiness is OPTIONAL (header.sticky) — the class carries it, so a
    // downstream that scrolls its own container can turn it off.
    ".hgr-th{text-align:left;padding:6px 10px;",
    "  background:var(--color-surface-raised);color:var(--color-text-muted);",
    "  white-space:nowrap;",
    "  box-shadow:inset -1px 0 0 var(--color-border),",
    "             inset 0 -2px 0 var(--color-border);}",
    ".hgr-table.hgr-sticky-head .hgr-th{position:sticky;top:0;z-index:2;}",
    // ext2 — the resize HANDLE: a real element on the header's right edge, so
    // the pointer shows col-resize on hover and the drag has a reliable target
    // (sticky is a positioned ancestor, so absolute resolves against the th).
    ".hgr-resize-handle{position:absolute;top:0;right:0;width:8px;height:100%;",
    "  cursor:col-resize;}",
    // ext1 drag-reorder: grab affordance on the header body; the dragged
    // header dims while its drop position rides the guide line.
    ".hgr-th{cursor:grab;}",
    ".hgr-th.hgr-dragging{opacity:0.45;}",
    ".hgr-td{padding:0;border-bottom:1px solid var(--color-border);",
    "  border-right:1px solid color-mix(in srgb, var(--color-border) 50%, transparent);",
    "  vertical-align:middle;}",
    // Selection visuals: painted on the td by the layout at GridSelection's
    // direction (the td is the CellSlot host — the FocusManager-wrapper pattern).
    ".hgr-td.hgr-sel{background:color-mix(in srgb, var(--color-accent) 18%, transparent);}",
    ".hgr-td.hgr-cursor{outline:2px solid var(--color-accent);outline-offset:-2px;}",
    // Invalid bulk-edit buffer: dashed = the error affordance (no danger token
    // exists in the theme surface yet; emphasis carries it until one does).
    ".hgr-td.hgr-invalid{outline:2px dashed var(--color-accent-emphasis);outline-offset:-2px;}",
    ".hgr-table:focus{outline:none;}",
    // ext2 — column sizing: widths ride a custom property per <col> (the
    // sanctioned dynamic-value hatch), consumed here; hgr-fixed engages
    // deterministic layout once any explicit width exists.
    ".hgr-table.hgr-fixed{table-layout:fixed;}",
    ".hgr-table col{width:var(--hgr-col-w,auto);}",
    ".hgr-resize-guide{position:fixed;top:var(--hgr-guide-top);height:var(--hgr-guide-h);",
    "  pointer-events:none;",
    "  left:var(--hgr-guide-x);width:2px;background:var(--color-accent);z-index:99;}",
    // ext1 drag-reorder: the LANDING SLOT as a band the dragged column's
    // width — both edges drawn, so the destination reads the same whether
    // the drag came from the left or the right.
    ".hgr-drop-band{position:fixed;top:var(--hgr-guide-top);height:var(--hgr-guide-h);",
    "  left:var(--hgr-guide-x);width:var(--hgr-band-w);box-sizing:border-box;",
    "  border-left:2px solid var(--color-accent);border-right:2px solid var(--color-accent);",
    "  background:color-mix(in srgb, var(--color-accent) 12%, transparent);",
    "  pointer-events:none;z-index:99;}",
    ""
].join("\n");

function _hgrAddClass(el, c) {
    var parts = el.className ? el.className.split(" ") : [];
    if (parts.indexOf(c) < 0) el.className = parts.concat(c).join(" ");
}
function _hgrRemoveClass(el, c) {
    if (!el.className) return;
    el.className = el.className.split(" ").filter(function (x) { return x !== c; }).join(" ");
}

function _hgrEnsureStyles() {
    if (document.getElementById(_HGR_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _HGR_STYLE_ID;
    s.textContent = _HGR_STYLE_CSS;
    document.head.appendChild(s);
}

// ── the viewport follow ──────────────────────────────────────────────────
// The keyboard moves the cursor; the scrollport has to move with it, or the
// cursor walks out of the visible band and the grid looks dead. Native
// scrollIntoView({block:'nearest'}) has exactly the right SEMANTICS — least
// movement, and none at all when the slot already shows — but it knows
// nothing about the sticky header, so an upward move parks the cursor
// underneath the band. Hence the same arithmetic done here, one inset wiser.

/** The least delta that brings [er] inside the port. A slot TALLER than the
 *  port aligns to its top rather than its bottom: seeing where you are beats
 *  seeing where you end. */
function _hgrDelta(er, top, bottom, left, right) {
    var dy = 0, dx = 0;
    if (er.top < top)            dy = er.top - top;
    else if (er.bottom > bottom) dy = Math.min(er.bottom - bottom, er.top - top);
    if (er.left < left)          dx = er.left - left;
    else if (er.right > right)   dx = Math.min(er.right - right, er.left - left);
    return { dy: dy, dx: dx };
}

/** Does this element actually scroll? Overflowing content is not enough —
 *  overflow:visible spills without scrolling. Where no computed style is to
 *  be had, the overflow measurement stands on its own. */
function _hgrScrolls(el) {
    if (!(el.scrollHeight > el.clientHeight || el.scrollWidth > el.clientWidth)) return false;
    var cs = (typeof window !== "undefined" && window.getComputedStyle)
           ? window.getComputedStyle(el) : null;
    if (!cs) return true;
    return /auto|scroll|overlay/.test(
        (cs.overflowY || "") + " " + (cs.overflowX || "") + " " + (cs.overflow || ""));
}

/** Scroll one element scrollport the minimum. clientTop/clientLeft step over
 *  the border, and clientWidth/clientHeight already exclude the scrollbars. */
function _hgrRevealIn(scroller, el, topInset) {
    var sr = scroller.getBoundingClientRect();
    var top  = sr.top  + (scroller.clientTop  || 0);
    var left = sr.left + (scroller.clientLeft || 0);
    var d = _hgrDelta(el.getBoundingClientRect(),
                      top + topInset, top + scroller.clientHeight,
                      left,           left + scroller.clientWidth);
    if (d.dy) scroller.scrollTop  += d.dy;
    if (d.dx) scroller.scrollLeft += d.dx;
}

/** …and the window, the scrollport of last resort. */
function _hgrRevealInWindow(el, topInset) {
    if (typeof window === "undefined" || !window.scrollBy) return;
    var w = window.innerWidth || 0, h = window.innerHeight || 0;
    if (!w || !h) return;
    var d = _hgrDelta(el.getBoundingClientRect(), topInset, h, 0, w);
    if (d.dy || d.dx) window.scrollBy(d.dx, d.dy);
}

class GridLayout {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[GridLayout] opts.container is required");
        _hgrEnsureStyles();
        this._container = opts.container;
        this._onCellClick = opts.onCellClick || null;   // (i, j, {shift, ctrl})
        this._painted = { cursorTd: null, selTds: [] }; // paint memo (diff target)
        this._table = document.createElement("table");
        this._table.className = "hgr-table";
        this._table.setAttribute("tabindex", "0");      // the keyboard host
        // RFC 0050 D9 — the STATIC reading path only: no role=grid (a real
        // <table> with <th> already maps to role=table, and declaring grid
        // would flip readers out of browse mode without the interactive layer
        // to back it). The baseline: name the focusable table, and say plainly
        // when nothing in it can be edited.
        if (opts.label) this._table.setAttribute("aria-label", opts.label);
        if (opts.readOnly) this._table.setAttribute("aria-readonly", "true");
        // the header gestures live in GridHeaderDrag (split by the ratchet)
        this._drag = (opts.onColResize || opts.onColReorder)
                   ? new GridHeaderDrag({ table: this._table,
                                          onColResize: opts.onColResize || null,
                                          onColReorder: opts.onColReorder || null })
                   : null;
        this._colgroup = document.createElement("colgroup");
        this._table.appendChild(this._colgroup);
        // header.show=false builds NO thead at all — not display:none. Nothing
        // to mint, nothing to wire gestures onto, nothing for a reader to
        // announce. (Minesweeper used to need a CSS trick for this.)
        this._showHead = opts.showHeader !== false;
        // Held, not re-read off the class list: the follow needs to know
        // whether the band overlaps the top of the scrollport.
        this._stickyHead = this._showHead && opts.stickyHeader !== false;
        if (this._showHead) {
            if (this._stickyHead) _hgrAddClass(this._table, "hgr-sticky-head");
            this._thead = document.createElement("thead");
            this._headerRow = document.createElement("tr");
            this._thead.appendChild(this._headerRow);
            this._table.appendChild(this._thead);
        } else {
            this._thead = null; this._headerRow = null;
        }
        this._tbody = document.createElement("tbody");
        this._table.appendChild(this._tbody);
        this._container.appendChild(this._table);
        this._slots = [];    // [i][j] → td (the CellSlot hosts)
    }

    /**
     * (Re)build the structure for a view shape: { headers: string[], rows: n }.
     * Slots are minted fresh; CELL CONTENT IS NOT TOUCHED — any content still
     * inside an old slot simply rides the discarded subtree until the facade
     * re-places it into the new slots (a single appendChild per cell).
     */
    render(shape) {
        var headers = (shape && shape.headers) || [];
        var rows = (shape && shape.rows) || 0;

        if (this._headerRow)
            while (this._headerRow.firstChild) this._headerRow.removeChild(this._headerRow.firstChild);
        while (this._colgroup.firstChild) this._colgroup.removeChild(this._colgroup.firstChild);
        for (var h = 0; h < headers.length; h++) {
            this._colgroup.appendChild(document.createElement("col"));   // widths need cols regardless
            if (!this._headerRow) continue;
            var th = document.createElement("th");
            th.className = "hgr-th";
            th.textContent = headers[h];
            if (this._drag) this._drag.wire(th, h);
            this._headerRow.appendChild(th);
        }

        var oldBody = this._tbody;
        this._tbody = document.createElement("tbody");
        this._slots = [];
        this._painted = { cursorTd: null, selTds: [] };   // old tds are gone with the body
        var self = this;
        var wireClick = function (td, i, j) {
            td.addEventListener("click", function (e) {
                if (self._onCellClick) {
                    self._onCellClick(i, j, { shift: !!(e && e.shiftKey),
                                              ctrl:  !!(e && (e.ctrlKey || e.metaKey)) });
                }
            });
        };
        for (var i = 0; i < rows; i++) {
            var tr = document.createElement("tr");
            var rowSlots = [];
            for (var j = 0; j < headers.length; j++) {
                var td = document.createElement("td");
                td.className = "hgr-td";
                wireClick(td, i, j);
                tr.appendChild(td);
                rowSlots.push(td);
            }
            this._tbody.appendChild(tr);
            this._slots.push(rowSlots);
        }
        this._table.replaceChild(this._tbody, oldBody);
        return this;
    }

    /**
     * Reconcile the selection visuals to the resolved answer: remove the
     * classes from the previously painted tds, add them to the new set (an
     * explicit diff against the paint memo — never a full-table sweep).
     * resolved: { cursorIJ: {i, j} | null, cells: [{i, j}] }.
     */
    paintSelection(resolved) {
        var self = this;
        this._painted.selTds.forEach(function (td) { _hgrRemoveClass(td, "hgr-sel"); });
        if (this._painted.cursorTd) _hgrRemoveClass(this._painted.cursorTd, "hgr-cursor");
        var selTds = [];
        (resolved.cells || []).forEach(function (c) {
            var td = self.slotAt(c.i, c.j);
            if (td) { _hgrAddClass(td, "hgr-sel"); selTds.push(td); }
        });
        var cursorTd = resolved.cursorIJ
                     ? this.slotAt(resolved.cursorIJ.i, resolved.cursorIJ.j) : null;
        if (cursorTd) _hgrAddClass(cursorTd, "hgr-cursor");
        this._painted = { cursorTd: cursorTd, selTds: selTds };
        // The viewport follows the intent's OWN target — the cursor for a
        // move, the range's focus for an extend, nothing for a remap — and
        // only while the keyboard is actually here.
        if (resolved.revealIJ && this._hasKeyboard())
            this.revealSlot(resolved.revealIJ.i, resolved.revealIJ.j);
        return this;
    }

    /** Error paint for the virtual session: hgr-invalid on the given view
     *  cells, diffed against the previous set; null clears. */
    paintInvalid(cellsIJ) {
        var self = this;
        (this._invalidTds || []).forEach(function (td) { _hgrRemoveClass(td, "hgr-invalid"); });
        this._invalidTds = [];
        (cellsIJ || []).forEach(function (c) {
            var td = self.slotAt(c.i, c.j);
            if (td) { _hgrAddClass(td, "hgr-invalid"); self._invalidTds.push(td); }
        });
        return this;
    }

    /** ext2 — apply per-visible-column widths (px | null), via the custom-
     *  property hatch; hgr-fixed engages once any explicit width exists. */
    setColWidths(widths) {
        var any = false, cols = this._colgroup.children;
        for (var j = 0; j < cols.length; j++) {
            var w = widths ? widths[j] : null, st = cols[j].style;
            if (w != null) { any = true; if (st && st.setProperty) st.setProperty("--hgr-col-w", w + "px"); }
            else if (st && st.removeProperty) st.removeProperty("--hgr-col-w");
        }
        if (any) _hgrAddClass(this._table, "hgr-fixed");
        else _hgrRemoveClass(this._table, "hgr-fixed");
        return this;
    }
    /**
     * Bring a slot into view, moving as little as possible — the viewport's
     * half of a keyboard move. Every scrollable ancestor takes its turn,
     * innermost outwards, and then the window. The sticky header's height
     * comes off the top edge of the FIRST scrollport only, since that is the
     * one position:sticky sticks to; further out, the band scrolls away with
     * the table and occludes nothing.
     *
     * Public and unguarded, so a downstream that moves the cursor
     * programmatically can demand the follow.
     */
    revealSlot(i, j) {
        var el = this.slotAt(i, j);
        if (!el || !el.getBoundingClientRect) return this;
        // HEIGHT only, deliberately. position:sticky sits on the th CELLS, not
        // on the thead, so the thead's rect reports its un-stuck origin — its
        // top is wrong the moment the body scrolls, while its height is the
        // band's height either way. Never reach for .bottom here.
        var inset = 0;
        if (this._stickyHead && this._thead && this._thead.getBoundingClientRect)
            inset = this._thead.getBoundingClientRect().height || 0;
        var node = el.parentNode;
        while (node && node !== document.body && node !== document.documentElement) {
            if (node.getBoundingClientRect && _hgrScrolls(node)) {
                _hgrRevealIn(node, el, inset);
                inset = 0;                        // claimed by the innermost port
            }
            node = node.parentNode;
        }
        _hgrRevealInWindow(el, inset);
        return this;
    }

    /** Is the keyboard actually here? The follow exists to serve it, and a
     *  grid nobody is typing into has no business moving the page under
     *  someone. An open editor counts — its input lives in a slot. */
    _hasKeyboard() {
        var a = document.activeElement;
        if (!a || a === document.body) return false;
        return a === this._table
            || !!(this._table.contains && this._table.contains(a));
    }

    /** The focusable table element — the keyboard attaches here. */
    el() { return this._table; }

    /** The CellSlot host at a view position, or null. */
    slotAt(i, j) {
        var row = this._slots[i];
        return (row && row[j]) ? row[j] : null;
    }

    rows() { return this._slots.length; }
    cols() { return this._slots.length ? this._slots[0].length : 0; }

    destroy() {
        if (this._table.parentNode) this._table.parentNode.removeChild(this._table);
        this._slots = [];
    }
}
