// WorkspaceSwitcherModule.js — the workspace switcher, rebuilt on SystemDialog.
//
// RFC 0057, Phase 3. A tree of KINDS on the left, the INSTANCES of the selected
// kind on the right, and three verbs underneath: Cancel, Open in new tab, Open.
// Nothing navigates until you confirm — browsing and opening are different acts.
//
//   new WorkspaceSwitcher({ workspaceKind, workspaceTitle, availableKinds,
//                           identity, catalogueStore, eventLog, checkpointStore })
//     .open() .close() .toggle() .isOpen() .destroy()
//
// Same constructor and surface as the WorkspaceControlModal it replaces, so the
// shell chrome swaps one symbol. Everything that made the old one a floating
// panel rather than a dialog — no scrim, no inert, no keyboard, mouse-only rows
// styled by cssText — is gone, because SystemDialog supplies the first three and
// typed sheets supply the rest.
//
// THREE REGIONS, AND TAB CYCLES THEM: kind tree → instance list → the create
// input → the action row → back to the tree. Arrows move WITHIN whichever has
// focus — up and down a tree or list, left and right along the actions. One key
// for which region and another for where in it, so the tree can never trap you.
//
// A CHILD BRANCH PER DETAIL RENDER. TreeRenderer clears its container on
// setData but numbers its rows from zero, and DomOpsParty refuses a duplicate
// name — so the pane for each selected kind is built on a fresh child branch
// and the previous one is dissolved. The dialog's own branch is the parent, so
// closing the dialog takes every pane with it.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java WorkspaceSwitcherModule declaration — do not add import/export
// lines here.

const _owner = Object.freeze({ toString: () => "workspaceSwitcher" });
var _seq = 0;

class WorkspaceSwitcher {

    constructor(opts) {
        opts = opts || {};
        this._kind     = opts.workspaceKind;
        this._title    = opts.workspaceTitle || opts.workspaceKind;
        this._kinds    = Array.isArray(opts.availableKinds) ? opts.availableKinds : [];
        this._base     = opts.switchBase || null;       // /goto?app=… — where a KIND change goes
        this._identity = opts.identity || {};          // LIVE ref — the orchestrator fills it in
        this._store    = opts.catalogueStore  || null;
        this._eventLog = opts.eventLog        || null;
        this._cp       = opts.checkpointStore || null;
        this._root = domOpsParty.createBranch("wsSwitcher" + (++_seq));
        this._root.activate(_owner);
        this._dlg = null;   // SystemDialog handle, while open
        this._c   = null;   // per-open state, while open
    }

    isOpen()  { return !!this._dlg; }
    toggle()  { if (this.isOpen()) this.close(); else this.open(); }
    close()   { if (this._dlg) this._dlg.close(); }
    destroy() { this.close(); try { this._root.dissolve(); } catch (e) {} }

    open() {
        if (this._dlg) return;
        var self = this;
        var c = this._c = { kind: this._kind, instance: this._identity.id || null, rows: [],
                            branch: null, pair: null, detail: null, list: null,
                            listEl: null, inputEl: null, delBtn: null, host: null };
        this._dlg = openSystemDialog({
            branch: this._root,
            title:  "Workspace — " + this._title,
            modal:  true,
            content: function (branch, bodyEl) {
                c.branch = branch;
                c.pair = mountMasterDetail({
                    branch: branch, host: bodyEl,
                    data: kindTreeData(self._kinds, self._kind),
                    expandDepth: 1, showBadge: true, showNote: false, showRoot: false,
                    onSelect:   function (detailEl, s) { var k = kindOfSelection(s); if (k) self._showKind(k, detailEl); },
                    onActivate: function (s) { if (kindOfSelection(s)) self._go(false); }
                });
                // selectPath is silent by contract, so the pane is drawn explicitly.
                var p = pathOfKind(self._kinds, self._kind);
                if (p) c.pair.renderer.selectPath(p, { reveal: true });
                self._showKind(self._kind, c.pair.bodyEl);
                return { onKeydown: function (ev) { return self._keys(ev); }, focusEl: c.pair.navEl };
            },
            actions: [
                { id: "cancel", label: "Cancel",                       onClick: function () { self.close(); } },
                { id: "newtab", label: "Open in new tab",              onClick: function () { self._go(true); } },
                { id: "open",   label: "Open",         primary: true,  onClick: function () { self._go(false); } }
            ],
            onClose: function () { self._dlg = null; self._c = null; }
        });
        this._refreshActions();
    }

    // ── the detail pane: everything about the selected kind ─────────────────
    _showKind(kind, host) {
        var self = this, c = this._c;
        c.kind = kind; c.host = host; c.rows = [];
        c.instance = (kind === this._kind) ? (this._identity.id || null) : null;
        if (c.detail) { try { c.detail.dissolve(); } catch (e) {} }
        var b = c.detail = c.branch.createBranch("detail" + Date.now() + "_" + (++_seq));
        b.activate(_owner);

        var k = null;
        for (var i = 0; i < this._kinds.length; i++) if (this._kinds[i].kind === kind) k = this._kinds[i];

        var wrap = b.createElement("wrap", "div"); css.addClass(wrap, ws_detail); host.appendChild(wrap);
        var head = b.createElement("head", "div"); css.addClass(head, ws_head);
        head.textContent = (k && k.title) || kind; wrap.appendChild(head);
        var sub = b.createElement("sub", "div"); css.addClass(sub, ws_sub);
        sub.textContent = kind + (kind === this._kind ? "  ·  current" : ""); wrap.appendChild(sub);

        var listEl = c.listEl = b.createElement("list", "div");
        css.addClass(listEl, ws_list); css.addClass(listEl, ws_list_focus);
        listEl.setAttribute("tabindex", "0"); wrap.appendChild(listEl);
        var note = b.createElement("note", "div"); css.addClass(note, ws_note);
        note.textContent = "Loading…"; wrap.appendChild(note);

        var row = b.createElement("create", "div"); css.addClass(row, ws_row);
        var input = c.inputEl = b.createElement("name", "input");
        input.type = "text"; input.placeholder = "New workspace name"; css.addClass(input, ws_input);
        input.addEventListener("keydown", function (ev) {
            if (ev.key === "Enter") { ev.preventDefault(); self._create(input.value); }
        });
        row.appendChild(input);
        row.appendChild(this._button(b, "mk", "Create", function () { self._create(input.value); }));
        var del = c.delBtn = this._button(b, "del", "Delete", function () { self._delete(); });
        css.addClass(del, ws_btn_danger); row.appendChild(del);
        wrap.appendChild(row);

        // Maintenance acts on what is OPEN, not on what is chosen — so only here.
        if (kind === this._kind) {
            var m = b.createElement("maint", "div"); css.addClass(m, ws_row); css.addClass(m, ws_maint);
            m.appendChild(this._button(b, "reset", "Reset state", function () { self._reset(); }));
            m.appendChild(this._button(b, "slow", "Slow-motion replay", function () {
                HrefManagerInstance.navigate(HrefManagerInstance.withParam("slowmo", "500"));
            }));
            wrap.appendChild(m);
        }

        c.list = new TreeRenderer({
            branch: b, container: listEl, expandDepth: 0, showBadge: true, showNote: false, showRoot: false,
            onSelect:   function (s) { c.instance = instanceOfSelection(s); self._refreshActions(); },
            onActivate: function (s) { if (instanceOfSelection(s)) self._go(false); }
        });
        if (!this._store) { note.textContent = "(catalogue not attached)"; this._refreshActions(); return; }
        this._store.listByKind(kind).then(function (rows) {
            if (self._c !== c || c.detail !== b) return;     // superseded by a later selection
            c.rows = rows || [];
            c.list.setData(instanceListData(c.rows, self._identity.id));
            note.textContent = c.rows.length ? c.rows.length + " saved" : "No saved workspaces — Open starts the default.";
            var p = pathOfInstance(c.rows, c.instance);
            if (p) c.list.selectPath(p, { reveal: true });
            self._refreshActions();
        }).catch(function () { note.textContent = "Could not read the catalogue."; });
    }

    _button(b, name, label, onClick) {
        var btn = b.createElement(name, "button");
        btn.type = "button"; btn.textContent = label; css.addClass(btn, ws_btn);
        btn.addEventListener("click", function () { if (!btn.disabled) onClick(); });
        return btn;
    }

    _same() { var c = this._c; return c.kind === this._kind && c.instance === (this._identity.id || null); }

    _rowOf(id) { var c = this._c; for (var i = 0; i < c.rows.length; i++) if (c.rows[i].id === id) return c.rows[i]; return null; }

    // Opening what is already open would spend a page load to arrive here, so
    // the primary reads Close. Delete is offered only where it can succeed.
    _refreshActions() {
        var c = this._c; if (!c || !this._dlg) return;
        this._dlg.setAction("open", { label: this._same() ? "Close" : "Open" });
        var can = c.kind === this._kind && canDelete(this._rowOf(c.instance), this._identity.id);
        if (c.delBtn) { c.delBtn.disabled = !can; css.toggleClass(c.delBtn, ws_btn_off, !can); }
    }

    _go(newTab) {
        var c = this._c; if (!c) return;
        if (this._same() && !newTab) { this.close(); return; }
        var url = targetUrl(HrefManagerInstance.current(), { base: this._base, currentKind: this._kind, kind: c.kind, instanceId: c.instance });
        if (newTab) HrefManagerInstance.openNew(url); else HrefManagerInstance.navigate(url);
    }

    _create(name) {
        var c = this._c; name = (name || "").trim(); if (!c || !name) return;
        HrefManagerInstance.navigate(targetUrl(HrefManagerInstance.current(), { base: this._base, currentKind: this._kind, kind: c.kind, name: name }));
    }

    _delete() {
        var self = this, c = this._c; if (!c || !this._store) return;
        var row = this._rowOf(c.instance);
        if (!canDelete(row, this._identity.id)) return;
        if (!window.confirm('Delete workspace "' + row.name + '"? This cannot be undone.')) return;
        this._store.delete(c.kind, row.id)
            .then(function () { if (self._c === c) self._showKind(c.kind, c.host); })
            .catch(function (e) { console.warn("[WorkspaceSwitcher] delete failed:", e && e.message ? e.message : e); });
    }

    _reset() {
        if (!window.confirm('Clear all events + checkpoint for "' + (this._identity.name || "?")
                            + '" and reload? Current widgets will be lost.')) return;
        var tasks = [];
        if (this._eventLog && this._eventLog.clear) tasks.push(this._eventLog.clear().catch(function () {}));
        if (this._cp && this._cp.clear) tasks.push(this._cp.clear(this._kind, this._identity.id).catch(function () {}));
        Promise.all(tasks).then(function () { HrefManagerInstance.navigate(HrefManagerInstance.current()); });
    }

    // ── keyboard: Tab cycles the regions, arrows move within one ────────────
    _keys(ev) {
        var c = this._c; if (!c || !this._dlg) return false;
        var acts  = [this._dlg.actionEl("cancel"), this._dlg.actionEl("newtab"), this._dlg.actionEl("open")];
        var stops = [c.pair && c.pair.navEl, c.listEl, c.inputEl, acts[2]].filter(function (s) { return !!s; });
        var a = document.activeElement, idx = -1;
        for (var i = 0; i < stops.length; i++) if (stops[i] === a || stops[i].contains(a)) idx = i;
        if (acts.indexOf(a) >= 0) idx = stops.length - 1;          // any action button IS the actions region
        if (ev.key === "Tab") {
            var next = idx < 0 ? 0 : (idx + (ev.shiftKey ? stops.length - 1 : 1)) % stops.length;
            stops[next].focus(); return true;
        }
        if (stops[idx] === c.pair.navEl) return c.pair.renderer.handleKeydown(ev);
        if (stops[idx] === c.listEl)     return c.list ? c.list.handleKeydown(ev) : false;
        if (acts.indexOf(a) >= 0 && (ev.key === "ArrowLeft" || ev.key === "ArrowRight")) {
            acts[(acts.indexOf(a) + (ev.key === "ArrowRight" ? 1 : acts.length - 1)) % acts.length].focus();
            return true;
        }
        return false;   // the input and the buttons keep their native keys
    }
}
