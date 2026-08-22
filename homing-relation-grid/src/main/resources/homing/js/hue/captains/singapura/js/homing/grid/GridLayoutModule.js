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
    "  font:13px sans-serif;}",
    ".hgr-th{position:sticky;top:0;text-align:left;padding:6px 10px;",
    "  background:var(--color-surface-raised);color:var(--color-text-muted);",
    "  border-bottom:2px solid var(--color-border);white-space:nowrap;}",
    ".hgr-td{padding:0;border-bottom:1px solid var(--color-border);",
    "  border-right:1px solid color-mix(in srgb, var(--color-border) 50%, transparent);",
    "  vertical-align:middle;}",
    // Selection visuals land here in Phase 4 (cursor / range / editing classes
    // painted on the td by the layout at GridSelection's direction).
    ""
].join("\n");

function _hgrEnsureStyles() {
    if (document.getElementById(_HGR_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _HGR_STYLE_ID;
    s.textContent = _HGR_STYLE_CSS;
    document.head.appendChild(s);
}

class GridLayout {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[GridLayout] opts.container is required");
        _hgrEnsureStyles();
        this._container = opts.container;
        this._table = document.createElement("table");
        this._table.className = "hgr-table";
        this._thead = document.createElement("thead");
        this._headerRow = document.createElement("tr");
        this._thead.appendChild(this._headerRow);
        this._tbody = document.createElement("tbody");
        this._table.appendChild(this._thead);
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

        while (this._headerRow.firstChild) this._headerRow.removeChild(this._headerRow.firstChild);
        for (var h = 0; h < headers.length; h++) {
            var th = document.createElement("th");
            th.className = "hgr-th";
            th.textContent = headers[h];
            this._headerRow.appendChild(th);
        }

        var oldBody = this._tbody;
        this._tbody = document.createElement("tbody");
        this._slots = [];
        for (var i = 0; i < rows; i++) {
            var tr = document.createElement("tr");
            var rowSlots = [];
            for (var j = 0; j < headers.length; j++) {
                var td = document.createElement("td");
                td.className = "hgr-td";
                tr.appendChild(td);
                rowSlots.push(td);
            }
            this._tbody.appendChild(tr);
            this._slots.push(rowSlots);
        }
        this._table.replaceChild(this._tbody, oldBody);
        return this;
    }

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
