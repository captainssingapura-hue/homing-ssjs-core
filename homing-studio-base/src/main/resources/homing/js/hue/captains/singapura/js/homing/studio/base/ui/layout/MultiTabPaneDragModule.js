// =============================================================================
// MultiTabPaneDragModule — per-instance drag controller for MultiTabPane.
// Owns the mousedown → mousemove → mouseup flow: ghost, drop indicator,
// capacity-gated drop, detach-to-Modal, re-dock from Modal.
//
// Constructed once per MultiTabPane in its constructor:
//
//     this._drag = new TabDragController(this);
//
// On chip mousedown, MultiTabPane calls:
//
//     this._drag.beginDrag(event, slotId, tabId);
//
// TabDragController reads/mutates MultiTabPane's internal state via the
// back-reference. They're tightly coupled by design — this split exists
// only for file-size hygiene, not for general extensibility.
// =============================================================================

var DRAG_THRESHOLD   = 4;     // px of movement before drag activates (vs click)
var DETACH_DISTANCE  = 40;    // omnidirectional distance from drag start, outside any
                              // strip, after which the drag detaches to a Modal

var _STYLE_ID = "homing-mtp-drag-style";
// Theme-aware drag visuals — accent color tracks the active theme so the
// indicator + droptarget highlight match the rest of the chrome. The
// blocked variant stays a fixed red — the "this would fail" signal needs
// to read as warning regardless of theme palette.
var _STYLE_CSS = [
    ".hmtp-drag-ghost{position:fixed;z-index:9999;pointer-events:none;",
    "  padding:3px 8px;background:var(--color-surface);",
    "  border:1px solid var(--color-border);border-radius:3px;",
    "  font:13px sans-serif;color:var(--color-text-primary);",
    "  box-shadow:0 4px 12px rgba(0,0,0,0.18);opacity:0.95;}",
    // Drop-position indicator — a thin accent-coloured bar.",
    ".hmtp-drag-indicator{position:fixed;width:3px;",
    "  background:var(--color-accent);z-index:9998;pointer-events:none;",
    "  border-radius:1px;",
    "  box-shadow:0 0 6px color-mix(in srgb, var(--color-accent) 60%, transparent);}",
    ".hmtp-drag-indicator-blocked{background:#dc2626;",
    "  box-shadow:0 0 6px rgba(220,38,38,0.6);}",
    // Strip-level drop-target highlight — soft vertical gradient using the",
    // theme accent. Pure inner colour change; no border / box-shadow ring.",
    // Gradient fades top→bottom so the strip glows rather than blocks.",
    ".hmtp-strip-droptarget{",
    "  background:linear-gradient(180deg,",
    "    color-mix(in srgb, var(--color-accent) 40%, transparent) 0%,",
    "    color-mix(in srgb, var(--color-accent) 22%, transparent) 60%,",
    "    color-mix(in srgb, var(--color-accent) 10%, transparent) 100%)!important;}",
    ".hmtp-strip-droptarget-blocked{",
    "  background:linear-gradient(180deg,",
    "    rgba(220,38,38,0.40) 0%,",
    "    rgba(220,38,38,0.22) 60%,",
    "    rgba(220,38,38,0.10) 100%)!important;}",
    "body.hmtp-dragging-tab{cursor:grabbing!important;user-select:none;}"
].join("\n");

function _ensureStyles() {
    if (document.getElementById(_STYLE_ID)) return;
    var s = document.createElement("style");
    s.id = _STYLE_ID;
    s.textContent = _STYLE_CSS;
    document.head.appendChild(s);
}

class TabDragController {

    constructor(mt) {
        this._mt = mt;
        this._modalRedockHandlers = [];   // [titleBar, fn] for cleanup
        this._reset();
        _ensureStyles();
    }

    _reset() {
        this._srcSlot   = null;
        this._srcTabId  = null;
        this._active    = false;
        this._detached  = false;
        this._modal     = null;
        this._modalTab  = null;          // tab data while detached (waiting to re-dock)
        this._ghost     = null;
        this._indicator = null;
        this._litStrip  = null;          // currently-highlighted strip element
        this._startX    = 0;
        this._startY    = 0;
        this._offsetX   = 0;
        this._moveFn    = null;
        this._upFn      = null;
    }

    // ─── Entry from MultiTabPane on chip mousedown ───────────────────────

    beginDrag(ev, slotId, tabId) {
        if (ev.button !== 0) return;
        // If a drag is already in progress (shouldn't happen normally), bail.
        if (this._moveFn) return;
        ev.preventDefault();

        this._srcSlot  = slotId;
        this._srcTabId = tabId;
        this._startX   = ev.clientX;
        this._startY   = ev.clientY;
        this._active   = false;

        // Cursor offset within chip for ghost positioning.
        var chip = this._sourceChip();
        if (chip) {
            var r = chip.getBoundingClientRect();
            this._offsetX = ev.clientX - r.left;
        }

        var self = this;
        this._moveFn = function (e) { self._onMove(e); };
        this._upFn   = function (e) { self._onUp(e);   };
        document.addEventListener("mousemove", this._moveFn);
        document.addEventListener("mouseup",   this._upFn);
    }

    destroy() {
        for (var i = 0; i < this._modalRedockHandlers.length; i++) {
            var H = this._modalRedockHandlers[i];
            H[0].removeEventListener("mousedown", H[1]);
        }
        this._modalRedockHandlers = [];
        if (this._modal) { try { this._modal.destroy(); } catch (e) {} this._modal = null; }
        if (this._ghost) { this._ghost.remove(); this._ghost = null; }
        if (this._indicator) { this._indicator.remove(); this._indicator = null; }
        if (this._moveFn) document.removeEventListener("mousemove", this._moveFn);
        if (this._upFn)   document.removeEventListener("mouseup",   this._upFn);
        this._reset();
    }

    // ─── Pointer move ────────────────────────────────────────────────────

    _onMove(e) {
        if (!this._active) {
            var dx = e.clientX - this._startX;
            var dy = e.clientY - this._startY;
            if (Math.abs(dx) < DRAG_THRESHOLD && Math.abs(dy) < DRAG_THRESHOLD) return;
            this._active = true;
            this._beginVisuals();
        }

        // Detached mode — modal just follows cursor. Re-dock is decided on
        // mouseup (release over a strip), NOT during move; otherwise the
        // modal gets snatched by any strip the cursor passes over, making
        // it impossible to position the modal across the workspace.
        // Live drop-target feedback is fine though — strip highlight +
        // indicator preview the docking destination without committing.
        if (this._detached) {
            this._modal.moveTo(e.clientX - this._offsetX, e.clientY - 14);
            var dropTarget = this._findStripAt(e.clientX, e.clientY);
            if (dropTarget) this._showIndicator(dropTarget, e.clientX);
            else            this._hideIndicator();
            return;
        }

        // Normal drag — update ghost position.
        this._ghost.style.left = (e.clientX + 10) + "px";
        this._ghost.style.top  = (e.clientY + 10) + "px";

        var strip = this._findStripAt(e.clientX, e.clientY);
        if (strip) {
            this._showIndicator(strip, e.clientX);
        } else {
            this._hideIndicator();
            // Detach check — omnidirectional distance from start, outside any
            // strip. Works for upward drags (bottom-pane chips) and sideways
            // drags too, not just downward.
            var ddx = e.clientX - this._startX;
            var ddy = e.clientY - this._startY;
            if (Math.sqrt(ddx * ddx + ddy * ddy) > DETACH_DISTANCE) {
                this._detachToModal(e);
            }
        }
    }

    // ─── Pointer up ──────────────────────────────────────────────────────

    _onUp(e) {
        document.removeEventListener("mousemove", this._moveFn);
        document.removeEventListener("mouseup",   this._upFn);
        this._moveFn = null;
        this._upFn   = null;

        if (!this._active) {
            // Click — no drag past threshold. Switch tab.
            this._mt.switchTab(this._srcSlot, this._srcTabId);
            this._reset();
            return;
        }

        if (this._detached) {
            // Mouseup with modal floating: release over a strip = dock there;
            // release in empty space = leave floating, arm title-bar drag for
            // future re-dock.
            var modal = this._modal;
            var tab   = this._modalTab;
            var dropStrip = this._findStripAt(e.clientX, e.clientY);
            if (dropStrip && this._canDropOn(dropStrip.slotId)) {
                var dockIdx = this._findInsertIndex(dropStrip.slotId, e.clientX);
                try { modal.destroy(); } catch (err) {}
                // RFC 0032 P4 — public attach publishes the typed onTabAttached
                // callback so the chrome's recorder can emit a TabMoved event.
                this._mt.attachTab(dropStrip.slotId, tab, dockIdx);
            } else {
                this._armModalRedock(modal, tab);
            }
            this._endVisuals();
            this._modal    = null;
            this._modalTab = null;
            this._reset();
            return;
        }

        var strip = this._findStripAt(e.clientX, e.clientY);
        if (strip && this._canDropOn(strip.slotId)) {
            var idx = this._findInsertIndex(strip.slotId, e.clientX);
            this._mt.moveTab(this._srcSlot, this._srcTabId, strip.slotId, idx);
        }
        // else: snap back (no state changes, just remove ghost / indicator).

        this._endVisuals();
        this._reset();
    }

    // ─── Visuals ─────────────────────────────────────────────────────────

    _beginVisuals() {
        document.body.classList.add("hmtp-dragging-tab");
        var chip = this._sourceChip();
        if (chip) chip.style.opacity = "0.4";

        var tab = this._sourceTab();
        if (!tab) return;
        var ghost = document.createElement("div");
        ghost.className = "hmtp-drag-ghost";
        ghost.textContent = tab.title;
        document.body.appendChild(ghost);
        this._ghost = ghost;
    }

    _endVisuals() {
        document.body.classList.remove("hmtp-dragging-tab");
        var chip = this._sourceChip();
        if (chip) chip.style.opacity = "";
        if (this._ghost) { this._ghost.remove(); this._ghost = null; }
        this._hideIndicator();
    }

    _showIndicator(target, cx) {
        var canDrop = this._canDropOn(target.slotId);

        // Strip-level highlight (gradient — primary signal). Clear previous
        // target if different.
        if (this._litStrip && this._litStrip !== target.el) {
            this._litStrip.classList.remove("hmtp-strip-droptarget",
                                            "hmtp-strip-droptarget-blocked");
        }
        target.el.classList.toggle("hmtp-strip-droptarget",         canDrop);
        target.el.classList.toggle("hmtp-strip-droptarget-blocked", !canDrop);
        this._litStrip = target.el;

        // Precise-position indicator (line — secondary, only between chips).
        // On an EMPTY strip the gradient alone is unambiguous; an indicator
        // line at the strip's left edge reads as a chunky left border, not
        // a position cue. Suppress.
        var chips = target.el.querySelectorAll(".hmtp-chip");
        if (chips.length === 0) {
            if (this._indicator) { this._indicator.remove(); this._indicator = null; }
            return;
        }

        if (!this._indicator) {
            this._indicator = document.createElement("div");
            this._indicator.className = "hmtp-drag-indicator";
            document.body.appendChild(this._indicator);
        }
        this._indicator.classList.toggle("hmtp-drag-indicator-blocked", !canDrop);

        var rect = target.el.getBoundingClientRect();
        var idx = this._findInsertIndex(target.slotId, cx);
        var x;
        if (idx >= chips.length) {
            var last = chips[chips.length - 1].getBoundingClientRect();
            x = last.right;
        } else {
            x = chips[idx].getBoundingClientRect().left - 1;
        }
        this._indicator.style.left   = x + "px";
        this._indicator.style.top    = rect.top + "px";
        this._indicator.style.height = rect.height + "px";
    }

    _hideIndicator() {
        if (this._indicator) { this._indicator.remove(); this._indicator = null; }
        if (this._litStrip) {
            this._litStrip.classList.remove("hmtp-strip-droptarget",
                                            "hmtp-strip-droptarget-blocked");
            this._litStrip = null;
        }
    }

    // ─── Detach + re-dock ────────────────────────────────────────────────

    _detachToModal(e) {
        var tab = this._sourceTab();
        if (!tab) return;
        var srcSlot = this._srcSlot;

        // ─── b.2i contract: tab.render(el) is one-shot. The tab's persistent
        //     _contentEl already holds the widget DOM. Moving it into the
        //     modal body via a single sync appendChild between two attached
        //     parents preserves the widget's scroll/focus/animation/audio
        //     state — the widget doesn't notice the detach-to-modal at all.
        //
        //     Wrap the existing _contentEl in a modal-body div so the modal's
        //     padding contract is preserved. The wrapper is the actual
        //     content given to the Modal; the _contentEl lives inside it.
        var contentEl = document.createElement("div");
        contentEl.style.cssText = "padding:8px;height:100%;box-sizing:border-box;position:relative;";
        if (tab._contentEl) {
            // tab._contentEl is currently a child of the source slot's content
            // wrapper. appendChild here is a single move between two attached
            // parents (slot content → modal body, both in document at this
            // instant since we haven't removed/refreshed the slot yet).
            contentEl.appendChild(tab._contentEl);
            // tab._contentEl uses position:absolute inset:0 by default
            // (.hmtp-tab-content) — overrides for modal context.
            tab._contentEl.style.position = "relative";
            tab._contentEl.style.inset    = "auto";
            tab._contentEl.style.display  = "block";
            tab._contentEl.style.height   = "100%";
        }

        // Modal size: ~25% of viewport, with sensible floors so it's still
        // usable on small screens.
        var vw = window.innerWidth  || document.documentElement.clientWidth;
        var vh = window.innerHeight || document.documentElement.clientHeight;
        var modalW = Math.max(380, Math.round(vw * 0.5));
        var modalH = Math.max(260, Math.round(vh * 0.5));

        var modal = new Modal({
            container: document.body,
            title: tab.title,
            content: contentEl,
            x: Math.max(0, Math.round((vw - modalW) / 2)),
            y: Math.max(0, Math.round((vh - modalH) / 2)),
            width: modalW, height: modalH
        });

        // Remove tab from source state AFTER the move (so MultiTabPane's
        // slot re-render doesn't try to prune the _contentEl while it's
        // mid-flight — we've already moved it out of the slot's content
        // wrapper). _renderSlotLocal will see tab._contentEl.parentNode !==
        // slot's content and skip touching it.
        this._mt._removeTabFromState(srcSlot, this._srcTabId);
        this._mt._renderSlotLocal(srcSlot);

        this._modal    = modal;
        this._modalTab = tab;
        this._detached = true;

        // Ghost + indicator no longer needed (modal is the visual now).
        if (this._ghost) { this._ghost.remove(); this._ghost = null; }
        this._hideIndicator();
    }

    /**
     * Public — attach an externally-created Modal as a "floating tab
     * waiting to dock." Used by the WidgetPicker (Ext1b Mechanism 2):
     * once the user picks a widget tile, the picker modal stays open
     * with the freshly-constructed widget as its body, and this method
     * makes the modal's title bar a drag handle that — on release over
     * a pane strip — docks the widget as a new tab.
     *
     * The {@code tab} argument is a tab descriptor matching what
     * MultiTabPane consumes via {@code addTab()}: {@code {id, title,
     * render(contentEl)}}. The picker passes a {@code render} closure
     * that re-parents the already-mounted widget root into the new tab's
     * content element (the widget keeps its branch + state across the
     * modal → tab transition).
     *
     * Mirror of the detach flow's {@code _armModalRedock}: same gesture,
     * opposite direction. Modal close = cancel = consumer destroys the
     * widget branch via the modal's {@code onClose}.
     */
    attachFloatingTabModal(modal, tab) {
        this._armModalRedock(modal, tab);
    }

    /**
     * After mouseup with modal still floating: arm a title-bar mousedown
     * handler that re-docks the tab if mouseup happens over a target
     * strip. Modal's own drag handles the move; we only listen for the
     * release. Re-docking DURING move would let the modal get snatched by
     * any strip the cursor passes over — wrong UX. Decision-on-release
     * lets the user position the modal anywhere they want.
     */
    _armModalRedock(modal, tab) {
        var self = this;
        var titleBar = modal.el.querySelector(".hmd-titlebar");
        if (!titleBar) return;

        var onDown = function (downEv) {
            if (downEv.button !== 0) return;
            // Don't preventDefault / stopPropagation — Modal's own drag
            // handler needs to fire so the modal follows the cursor.

            // Live drop-target feedback during the drag — strip highlight +
            // indicator line. Decision still made on mouseup, but the user
            // can now see exactly where the modal will land before they
            // commit.
            var onMove = function (mv) {
                var strip = self._findStripAt(mv.clientX, mv.clientY);
                if (strip) self._showIndicator(strip, mv.clientX);
                else       self._hideIndicator();
            };
            var onUp = function (upEv) {
                document.removeEventListener("mousemove", onMove);
                document.removeEventListener("mouseup",   onUp);
                self._hideIndicator();
                var strip = self._findStripAt(upEv.clientX, upEv.clientY);
                if (!strip || !self._canDropOn(strip.slotId)) return;
                // Release was over a target strip → re-dock.
                titleBar.removeEventListener("mousedown", onDown);
                self._modalRedockHandlers = self._modalRedockHandlers.filter(
                        function (H) { return H[1] !== onDown; });
                var idx = self._findInsertIndex(strip.slotId, upEv.clientX);

                // ─── b.2i discipline: move tab._contentEl from the modal body
                //     INTO the dest slot's content wrapper BEFORE destroying
                //     the modal. Single sync appendChild between two attached
                //     parents (modal body → slot content) preserves the
                //     widget's UI state. Then restore the .hmtp-tab-content
                //     position styles overridden in _detachToModal.
                var destWrappers = self._mt._wrappersBySlot.get(strip.slotId);
                if (tab._contentEl && destWrappers) {
                    destWrappers.content.appendChild(tab._contentEl);
                    tab._contentEl.style.position = "";
                    tab._contentEl.style.inset    = "";
                    tab._contentEl.style.height   = "";
                    // display will be set by _renderContentContents based on
                    // activeTabId — switchTab below makes this tab active.
                }

                try { modal.destroy(); } catch (e) {}
                // RFC 0032 P4 — public attach publishes onTabAttached so the
                // chrome's recorder sees this redock as a typed event.
                self._mt.attachTab(strip.slotId, tab, idx);

                // Clean up detach state on the controller so the next drag
                // starts fresh.
                self._detached = false;
                self._modal    = null;
                self._modalTab = null;
            };
            document.addEventListener("mousemove", onMove);
            document.addEventListener("mouseup",   onUp);
        };
        titleBar.addEventListener("mousedown", onDown);
        this._modalRedockHandlers.push([titleBar, onDown]);
    }

    // ─── Hit-testing ─────────────────────────────────────────────────────

    _findStripAt(cx, cy) {
        var found = null;
        this._mt._stripEls.forEach(function (el, slotId) {
            if (found) return;
            var r = el.getBoundingClientRect();
            if (cx >= r.left && cx <= r.right && cy >= r.top && cy <= r.bottom) {
                found = { slotId: slotId, el: el };
            }
        });
        return found;
    }

    _findInsertIndex(slotId, cx) {
        var el = this._mt._stripEls.get(slotId);
        if (!el) return 0;
        var chips = el.querySelectorAll(".hmtp-chip");
        for (var i = 0; i < chips.length; i++) {
            var r = chips[i].getBoundingClientRect();
            if (cx < r.left + r.width / 2) return i;
        }
        return chips.length;
    }

    _canDropOn(destSlot) {
        if (destSlot === this._srcSlot && !this._detached) return true; // same-pane reorder
        var cap = this._mt.capacityOf(destSlot);
        var state = this._mt._tabsBySlot.get(destSlot);
        return state.tabs.length < cap;
    }

    // ─── Lookups ─────────────────────────────────────────────────────────

    _sourceChip() {
        var el = this._mt._stripEls.get(this._srcSlot);
        if (!el) return null;
        return el.querySelector('[data-tab-id="' + this._srcTabId + '"]');
    }

    _sourceTab() {
        var state = this._mt._tabsBySlot.get(this._srcSlot);
        if (!state) return null;
        for (var i = 0; i < state.tabs.length; i++) {
            if (state.tabs[i].id === this._srcTabId) return state.tabs[i];
        }
        return null;
    }
}
