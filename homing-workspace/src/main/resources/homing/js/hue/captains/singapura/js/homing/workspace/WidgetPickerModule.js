// =============================================================================
// WidgetPickerModule — RFC 0025 Ext1b Mechanism 2.
//
// Ephemeral picker UI. One instance per "add widget" action; once the user
// picks (or cancels), the picker goes out of scope. The picker holds NO
// state, owns NO branches, knows NOTHING about lifecycle, and renders into
// a caller-supplied host element — caller decides the container (a tab
// content area, a modal body, a sidebar, …).
//
//   var picker = new WidgetPicker({
//       entries     : WidgetEntryJson[],            // already filtered by caller
//       disabledIds : { "MyWidget": true },         // optional — greys those tiles
//       onPick      : function(entry, params) {},   // fired exactly once on commit
//       onCancel    : function() {}                 // optional — fired on form Cancel
//   });
//   picker.mountInto(hostEl);
//
// The orchestration of branch creation, tab placement, dynamic import, and
// tab-close cleanup lives in the workspace's chrome widget. The picker is
// intentionally dumb.
// =============================================================================

// Picker styles live in WidgetPickerStyles (CssGroup) — imported by the
// Java module declaration so the css.* identifiers are in scope here.

class WidgetPicker {

    constructor(opts) {
        if (!opts || !Array.isArray(opts.entries)) {
            throw new Error("WidgetPicker: entries[] required");
        }
        if (typeof opts.onPick !== "function") {
            throw new Error("WidgetPicker: onPick(entry, params) required");
        }
        this._entries     = opts.entries;
        this._disabledIds = opts.disabledIds || {};
        this._onPick      = opts.onPick;
        this._onCancel    = opts.onCancel || null;
        this._host        = null;
        this._delivered   = false;     // ensures onPick fires at most once
    }

    /**
     * Render the tile grid into hostEl. Caller chooses the container —
     * empty tab body, modal body, sidebar, anywhere. Picker takes over
     * the host's content (replaceChildren) but doesn't own the host
     * lifecycle. On pick or cancel the picker's content is replaced by
     * the caller (typically with the widget root or by removing the
     * tab entirely).
     */
    mountInto(hostEl) {
        this._host = hostEl;
        hostEl.replaceChildren(this._buildGrid());
    }

    // ─── Grid + tiles ──────────────────────────────────────────────────────

    _buildGrid() {
        var root = document.createElement("div");
        css.setClass(root, hwp_grid);

        // Group entries by entry.group, preserving registration order.
        var groups = [];
        var idx = {};
        for (var i = 0; i < this._entries.length; i++) {
            var e = this._entries[i];
            if (!(e.group in idx)) { idx[e.group] = groups.length; groups.push({ name: e.group, items: [] }); }
            groups[idx[e.group]].items.push(e);
        }

        for (var g = 0; g < groups.length; g++) {
            var label = document.createElement("div");
            css.setClass(label, hwp_group_label);
            label.textContent = groups[g].name;
            root.appendChild(label);
            for (var k = 0; k < groups[g].items.length; k++) {
                root.appendChild(this._buildTile(groups[g].items[k]));
            }
        }
        return root;
    }

    _buildTile(entry) {
        var tile = document.createElement("div");
        css.setClass(tile, hwp_tile);
        var disabled = !!this._disabledIds[entry.simpleName];
        if (disabled) {
            css.addClass(tile, hwp_tile_disabled);
            tile.title = "Already open";
        }

        var icon = document.createElement("div");
        css.setClass(icon, hwp_tile_icon);
        icon.textContent = entry.icon && entry.icon.kind === "emoji" ? entry.icon.value : "📦";
        tile.appendChild(icon);

        var label = document.createElement("div");
        css.setClass(label, hwp_tile_label);
        label.textContent = entry.label;
        tile.appendChild(label);

        if (entry.description) {
            var desc = document.createElement("div");
            css.setClass(desc, hwp_tile_desc);
            desc.textContent = entry.description;
            tile.appendChild(desc);
        }

        var self = this;
        tile.addEventListener("click", function () {
            if (disabled) {
                // Disabled-tile click means "I want this one but it's open" —
                // signal the caller via params=null so it can focus the
                // existing instance.
                if (self._delivered) return;
                self._delivered = true;
                self._onPick(entry, null);
                return;
            }
            self._onTilePick(entry);
        });
        return tile;
    }

    // ─── Pick handling ─────────────────────────────────────────────────────

    _onTilePick(entry) {
        var hasParams = entry.paramsFields && entry.paramsFields.length > 0;
        if (!hasParams) {
            this._deliver(entry, {});
        } else {
            this._showForm(entry);
        }
    }

    _showForm(entry) {
        var self = this;
        var defs = entry.defaults || {};
        var form = document.createElement("div");
        css.setClass(form, hwp_form);
        var inputs = {};
        for (var i = 0; i < entry.paramsFields.length; i++) {
            var f = entry.paramsFields[i];
            var row = document.createElement("div");
            css.setClass(row, hwp_form_row);
            var lab = document.createElement("label");
            css.setClass(lab, hwp_form_label);
            lab.textContent = f.name + (f.type ? " (" + f.type + ")" : "");
            var inp = document.createElement("input");
            css.setClass(inp, hwp_form_input);
            inp.value = defs[f.name] != null ? defs[f.name] : "";
            row.appendChild(lab); row.appendChild(inp);
            form.appendChild(row);
            inputs[f.name] = inp;
        }
        var actions = document.createElement("div");
        css.setClass(actions, hwp_form_actions);
        var cancelBtn = document.createElement("button");
        css.setClass(cancelBtn, hwp_form_btn);
        cancelBtn.textContent = "Cancel";
        var okBtn = document.createElement("button");
        css.setClass(okBtn, hwp_form_btn);
        css.addClass(okBtn, hwp_form_btn_primary);
        okBtn.textContent = "Open";
        actions.appendChild(cancelBtn); actions.appendChild(okBtn);
        form.appendChild(actions);

        if (this._host) this._host.replaceChildren(form);

        cancelBtn.addEventListener("click", function () {
            if (self._delivered) return;
            self._delivered = true;
            if (self._onCancel) self._onCancel();
        });
        okBtn.addEventListener("click", function () {
            var params = {};
            for (var k in inputs) {
                if (inputs.hasOwnProperty(k)) params[k] = inputs[k].value;
            }
            self._deliver(entry, params);
        });
    }

    _deliver(entry, params) {
        if (this._delivered) return;
        this._delivered = true;
        this._onPick(entry, params);
    }
}
