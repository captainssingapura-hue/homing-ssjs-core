// =============================================================================
// WorkspaceControlModal — V2 phases 16+17+18 combined.
//
// Single modal hosts the entire workspace-control surface:
//
//   ┌────────────────────────────────────────────────────────┐
//   │ Identity:    Animals Playground (V2) :: default        │
//   │              UUID: <hash>                              │
//   │                                                         │
//   │ Switcher:    • default            [default] [open]      │
//   │              • Mine                        [switch] [×] │
//   │              • Test                        [switch] [×] │
//   │              [ name_input ]                [Create]     │
//   │                                                         │
//   │ Actions:     [Reset state]   [Slow-motion replay]       │
//   └────────────────────────────────────────────────────────┘
//
// Explicit Substrate doctrine:
//   - Per-workspace instance (NOT INSTANCE — one modal per shell).
//   - No async sugar — naive Promise chains.
//   - ALL collaborators in constructor: _ModalCtor, _window, _document,
//     _catalogueStore, _eventLog, _checkpointStore. Per-call open()/close()
//     receive only data.
//
// Instance fields (graph-traversable):
//   _ModalCtor        : Modal class
//   _window           : Window
//   _document         : Document
//   _catalogueStore   : WorkspaceCatalogueStore | null
//   _eventLog         : WorkspaceEventLog | null
//   _checkpointStore  : CheckpointStore | null
// =============================================================================

class WorkspaceControlModal {

    constructor(opts) {
        if (!opts)                throw new Error('[WorkspaceControlModal] opts required');
        if (!opts.workspaceKind)  throw new Error('[WorkspaceControlModal] opts.workspaceKind required');
        if (!opts.identity)       throw new Error('[WorkspaceControlModal] opts.identity required');
        this._ModalCtor       = opts.ModalCtor       || Modal;
        this._window          = opts.window
                              || (typeof window !== 'undefined' ? window : null);
        this._document        = opts.document
                              || (typeof document !== 'undefined' ? document : null);
        this._catalogueStore  = opts.catalogueStore  || null;
        this._eventLog        = opts.eventLog        || null;
        this._checkpointStore = opts.checkpointStore || null;
        // Per-instance data — identity is a LIVE ref (orchestrator
        // mutates it in place when resolveIdentity completes), so reads
        // here always see the current value.
        this._workspaceKind   = opts.workspaceKind;
        this._identity        = opts.identity;
        this._workspaceTitle  = opts.workspaceTitle || opts.workspaceKind;
        this._modal           = null;
    }

    isOpen() { return !!(this._modal && this._modal.isOpen && this._modal.isOpen()); }

    open() {
        if (this.isOpen()) return;
        if (!this._document) return;
        const self = this;
        const body = this._document.createElement('div');
        // Inherit theme colors from the Modal frame (Modal sets
        // background/color via --color-surface / --color-text-primary on
        // its body wrapper, then we sit inside that). Using `inherit`
        // for both means switching themes mid-session restyles us
        // without rebuilding.
        body.style.cssText = 'padding:14px;font:13px/1.45 system-ui,sans-serif;'
                           + 'color:inherit;background:inherit;'
                           + 'height:100%;overflow:auto;box-sizing:border-box;';
        this._renderIdentity(body);
        this._renderSwitcher(body);
        this._renderActions(body);

        // Center on viewport.
        const ww = (this._window && this._window.innerWidth)  || 800;
        const wh = (this._window && this._window.innerHeight) || 600;
        const w  = 520;
        const h  = 520;
        this._modal = new this._ModalCtor({
            container: this._document.body,
            title:     'Workspace — ' + this._workspaceTitle,
            content:   body,
            x:         Math.max(20, (ww - w) / 2),
            y:         Math.max(20, (wh - h) / 2),
            width:     w,
            height:    h,
            onClose:   function () { self._modal = null; }
        });
    }

    close() {
        if (this._modal && this._modal.destroy) {
            try { this._modal.destroy(); } catch (e) {}
        }
        this._modal = null;
    }

    toggle() { if (this.isOpen()) this.close(); else this.open(); }

    destroy() { this.close(); }

    // ─── Sections ───────────────────────────────────────────────────────

    _renderIdentity(parent) {
        const section = this._section(parent, 'Identity');
        const row = this._document.createElement('div');
        row.style.cssText = 'display:flex;align-items:baseline;gap:10px;'
                          + 'flex-wrap:wrap;margin-bottom:4px;';
        const big = this._document.createElement('span');
        big.style.cssText = 'font-size:16px;font-weight:600;'
                          + 'color:'+COLOR_TEXT_PRIMARY+';';
        big.textContent = this._workspaceTitle + ' :: ' + (this._identity.name || '?');
        row.appendChild(big);
        if (this._identity.isDefault) {
            const tag = this._document.createElement('span');
            tag.style.cssText = 'font-size:11px;padding:1px 6px;border-radius:8px;'
                              + 'background:'+COLOR_SURFACE_RAISED+';'
                              + 'color:'+COLOR_ACCENT_EMPHASIS+';';
            tag.textContent = 'default';
            row.appendChild(tag);
        }
        section.appendChild(row);
        const sub = this._document.createElement('div');
        sub.style.cssText = 'font:11px/1.4 monospace;color:'+COLOR_TEXT_MUTED+';';
        sub.textContent = 'UUID: ' + (this._identity.id || '?');
        section.appendChild(sub);
    }

    _renderSwitcher(parent) {
        const section = this._section(parent, 'Workspaces');
        const list = this._document.createElement('div');
        list.style.cssText = 'border:1px solid '+COLOR_BORDER+';border-radius:4px;'
                           + 'background:'+COLOR_SURFACE_RECESSED+';'
                           + 'padding:4px 0;margin-bottom:8px;'
                           + 'max-height:200px;overflow:auto;';
        section.appendChild(list);
        const status = this._document.createElement('div');
        status.style.cssText = 'font-size:11px;color:'+COLOR_TEXT_MUTED+';margin-bottom:8px;';
        status.textContent = 'Loading…';
        section.appendChild(status);

        // Create form.
        const form = this._document.createElement('div');
        form.style.cssText = 'display:flex;gap:6px;';
        const input = this._document.createElement('input');
        input.type = 'text';
        input.placeholder = 'New workspace name';
        input.style.cssText = 'flex:1;padding:5px 8px;'
                            + 'background:'+COLOR_SURFACE_RECESSED+';'
                            + 'color:'+COLOR_TEXT_PRIMARY+';'
                            + 'border:1px solid '+COLOR_BORDER+';'
                            + 'border-radius:3px;font:13px system-ui;';
        const createBtn = this._button('Create', function () {
            const name = (input.value || '').trim();
            if (!name) return;
            // ?name=<n> route — WorkspaceDirectory mints a fresh UUID on miss.
            self._navigateWithParam('name', name);
        });
        form.appendChild(input);
        form.appendChild(createBtn);
        section.appendChild(form);

        const self = this;
        // Populate list from catalogue.
        if (!this._catalogueStore) {
            status.textContent = '(catalogue not attached)';
            return;
        }
        this._catalogueStore.listByKind(this._workspaceKind)
            .then(function (rows) {
                if (!rows || rows.length === 0) {
                    status.textContent = '(no workspaces yet for kind '
                                       + self._workspaceKind + ')';
                    return;
                }
                status.textContent = rows.length + ' workspace(s) catalogued';
                rows.sort(function (a, b) {
                    if (a.isDefault && !b.isDefault) return -1;
                    if (!a.isDefault && b.isDefault) return 1;
                    return (a.name || '').localeCompare(b.name || '');
                });
                for (const entry of rows) {
                    list.appendChild(self._renderSwitcherRow(entry));
                }
            })
            .catch(function (e) {
                status.textContent = 'Failed to list: '
                                   + (e && e.message ? e.message : e);
            });
    }

    _renderSwitcherRow(entry) {
        const self = this;
        const row = this._document.createElement('div');
        row.style.cssText = 'display:flex;align-items:center;gap:8px;'
                          + 'padding:6px 10px;'
                          + 'border-bottom:1px solid '+COLOR_BORDER+';';
        const isCurrent = (entry.id === this._identity.id);

        const dot = this._document.createElement('span');
        dot.textContent = isCurrent ? '●' : '○';
        dot.style.cssText = 'width:10px;color:'
                          + (isCurrent
                                ? COLOR_ACCENT_EMPHASIS
                                : COLOR_TEXT_MUTED) + ';';
        row.appendChild(dot);

        const name = this._document.createElement('span');
        name.textContent = entry.name || '(unnamed)';
        name.style.cssText = 'flex:1;color:'
                           + (isCurrent
                                ? COLOR_TEXT_PRIMARY
                                : COLOR_TEXT_MUTED) + ';';
        row.appendChild(name);

        if (entry.isDefault) {
            const tag = this._document.createElement('span');
            tag.style.cssText = 'font-size:10px;padding:1px 5px;border-radius:7px;'
                              + 'background:'+COLOR_SURFACE_RAISED+';'
                              + 'color:'+COLOR_ACCENT_EMPHASIS+';';
            tag.textContent = 'default';
            row.appendChild(tag);
        }

        if (isCurrent) {
            const open = this._document.createElement('span');
            open.style.cssText = 'font-size:11px;color:'+COLOR_TEXT_MUTED+';';
            open.textContent = '(open)';
            row.appendChild(open);
        } else {
            row.appendChild(this._button('Switch', function () {
                self._navigateWithParam('workspace', entry.id);
            }));
            if (!entry.isDefault) {
                row.appendChild(this._button('×', function () {
                    if (!self._window || !self._window.confirm
                        || !self._window.confirm('Delete workspace "' + entry.name
                            + '"? This cannot be undone.')) return;
                    self._catalogueStore.delete(self._workspaceKind, entry.id)
                        .then(function () {
                            row.parentNode && row.parentNode.removeChild(row);
                        })
                        .catch(function (e) {
                            console.warn('[WorkspaceControlModal] delete failed:',
                                e && e.message ? e.message : e);
                        });
                }, 'danger'));
            }
        }
        return row;
    }

    _renderActions(parent) {
        const section = this._section(parent, 'Actions');
        const row = this._document.createElement('div');
        row.style.cssText = 'display:flex;gap:8px;flex-wrap:wrap;';
        const self = this;
        row.appendChild(this._button('Reset state', function () {
            if (!self._window || !self._window.confirm
                || !self._window.confirm(
                    'Clear all events + checkpoint for "' + self._identity.name
                  + '" and reload? Current widgets will be lost.')) return;
            self._doReset();
        }, 'danger'));
        row.appendChild(this._button('Slow-motion replay', function () {
            self._navigateWithParam('slowmo', '500');
        }));
        section.appendChild(row);
        const note = this._document.createElement('div');
        note.style.cssText = 'font-size:11px;color:'+COLOR_TEXT_MUTED+';margin-top:6px;';
        note.textContent = 'Reset clears the event log + checkpoint, then reloads. '
                         + 'Slow-motion replay re-walks the log at 500ms/step '
                         + '(observation-only — nothing is rewritten).';
        section.appendChild(note);
    }

    _doReset() {
        const self = this;
        const tasks = [];
        if (this._eventLog && typeof this._eventLog.clear === 'function') {
            tasks.push(this._eventLog.clear().catch(function (e) {
                console.warn('[WorkspaceControlModal] event log clear failed:', e);
            }));
        }
        if (this._checkpointStore && typeof this._checkpointStore.clear === 'function') {
            tasks.push(this._checkpointStore
                    .clear(this._workspaceKind, this._identity.id)
                    .catch(function (e) {
                        console.warn('[WorkspaceControlModal] checkpoint clear failed:', e);
                    }));
        }
        Promise.all(tasks).then(function () {
            if (self._window && self._window.location) self._window.location.reload();
        });
    }

    // ─── DOM helpers ────────────────────────────────────────────────────

    _section(parent, heading) {
        const block = this._document.createElement('div');
        block.style.cssText = 'margin-bottom:18px;';
        const h = this._document.createElement('div');
        h.textContent = heading;
        h.style.cssText = 'font-size:11px;text-transform:uppercase;'
                        + 'letter-spacing:1px;color:'+COLOR_TEXT_MUTED+';'
                        + 'margin-bottom:6px;padding-bottom:2px;'
                        + 'border-bottom:1px solid '+COLOR_BORDER+';';
        block.appendChild(h);
        parent.appendChild(block);
        return block;
    }

    _button(label, onClick, variant) {
        const btn = this._document.createElement('button');
        btn.textContent = label;
        const danger = (variant === 'danger');
        btn.style.cssText = 'padding:4px 12px;border:none;border-radius:3px;'
                          + 'cursor:pointer;font:11px system-ui;font-weight:600;'
                          + (danger
                                // Danger stays explicit (red) across themes —
                                // destructive actions deserve visual weight.
                                ? 'background:#7a3010;color:#fff;'
                                : 'background:'+COLOR_SURFACE_RAISED+';'
                                + 'color:'+COLOR_TEXT_PRIMARY+';');
        btn.addEventListener('click', function (ev) {
            ev.stopPropagation();
            try { onClick(); }
            catch (e) { console.error('[WorkspaceControlModal] button threw:', e); }
        });
        return btn;
    }

    /**
     * Update one search-param and reload. Preserves the framework's
     * ?app=… / ?ws_kind=… routing parameters; only the named param is
     * touched. Mutually-exclusive params (workspace vs name) are
     * cleared in the right combinations.
     */
    _navigateWithParam(key, value) {
        if (!this._window || !this._window.location) return;
        const loc = this._window.location;
        let params;
        try { params = new URLSearchParams(loc.search); }
        catch (e) { params = new URLSearchParams(''); }
        if (key === 'workspace') {
            params.delete('name');
            params.set('workspace', value);
            params.delete('slowmo');
        } else if (key === 'name') {
            params.delete('workspace');
            params.set('name', value);
            params.delete('slowmo');
        } else if (key === 'slowmo') {
            params.set('slowmo', value);
        } else {
            params.set(key, value);
        }
        loc.search = '?' + params.toString();
    }
}
