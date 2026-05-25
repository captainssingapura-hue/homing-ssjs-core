// =============================================================================
// WorkspaceLayoutModule — RFC 0025 Ext1b b.2e.
//
// Reusable skeleton for every workspace's chrome: ribbon at top (title +
// custom items + fullscreen toggle), locked-size content area in the
// middle (where MultiTabPane mounts), optional footer at bottom (omitted
// when empty).
//
//   var layout = new WorkspaceLayout({
//       container    : host,                 // host element (branch.createElement)
//       title        : "Animals Playground", // workspace.title()
//       ribbonItems  : [...],                // JSON from WorkspaceShell.ribbonItemsJson()
//       footerItems  : [...],                // JSON from WorkspaceShell.footerItemsJson()
//       onAction     : function (actionId) { ... }   // ribbon/footer Button clicks
//   });
//   // layout.contentEl  ← where MultiTabPane mounts
//   // layout.setFullScreen(bool)
//
// Fullscreen mechanism: body class wl-fullscreen-active is the toggle.
// CSS rules (in WorkspaceLayoutStyles) push the workspace root to
// position:fixed inset:0 and hide marked studio chrome (.st-header).
// Escape exits.
// =============================================================================

// Styles imported via WorkspaceLayoutStyles (CssGroup) — css.* identifiers
// are in scope at module top level.

// Map JS-emitted CSS class identifiers for the body-level fullscreen marker
// to actual class names. css.className() returns the generated class name.
function _bodyFullscreenClassName() { return css.className(wl_fullscreen_active); }
function _chromeHiddenClassName()   { return css.className(wl_chrome_hidden); }
function _workspaceActiveClassName(){ return css.className(wl_workspace_active); }

class WorkspaceLayout {

    constructor(opts) {
        if (!opts || !opts.container) throw new Error("WorkspaceLayout: container required");
        this._container   = opts.container;
        this._title       = opts.title || "";
        this._ribbonItems = Array.isArray(opts.ribbonItems) ? opts.ribbonItems : [];
        this._footerItems = Array.isArray(opts.footerItems) ? opts.footerItems : [];
        this._onAction    = opts.onAction || null;
        this._escHandler  = null;
        this._buildParty();
        this._build();
        this._joinPartyActors();
    }

    // ─── RFC 0028 cycle 3 — LayoutParty setup ───────────────────────────────
    //
    // The fullscreen flow goes through a Party rather than direct method
    // calls. Three pieces:
    //
    //   1. The LayoutSecretary (the only Secretary in this Party) is the
    //      single authoritative owner of fullscreen state. Pure function:
    //      Requested → flip state, emit Changed.
    //   2. The fullscreenToggle Actor lives on the ribbon button. It sends
    //      FullscreenToggleRequested on click; reacts to FullscreenChanged
    //      by swapping its icon glyph.
    //   3. The body Actor reacts to FullscreenChanged by performing the
    //      DOM mutations (body classes, root class, .st-header hiding,
    //      Escape-key listener attach/detach).
    //
    // No piece owns authoritative state except the Secretary. No Actor
    // touches another Actor's DOM. The Secretary touches nothing.
    _buildParty() {
        // RFC 0028 cycle 6 phase 1 — Secretary extracted to LayoutSecretaryModule.js
        // for unit-testability in isolation. The chrome wires the imported
        // initial state and behavior into the Party; the Party's runtime is
        // unchanged. Behavior is pure; state is the only authoritative
        // record of fullscreen status.
        this._party = new Party({
            name: "layout",
            root: {
                path     : "layout",
                initial  : LayoutSecretary.initial,
                behavior : LayoutSecretary.behavior
            }
        });
    }

    _joinPartyActors() {
        var self = this;
        // Toggle Actor — sender on click, reactor for icon update.
        this._party.joinActor({
            id: "layout/fullscreenToggle",
            parentSecretary: "layout",
            reactors: {
                FullscreenChanged: function (msg) {
                    self._fsBtn.textContent = msg.on ? "⤢" : "⛶";
                }
            }
        });
        // Body Actor — sender on Escape (via _applyFullscreenDom's handler),
        // reactor for the actual DOM toggling.
        this._party.joinActor({
            id: "layout/body",
            parentSecretary: "layout",
            reactors: {
                FullscreenChanged: function (msg) {
                    self._applyFullscreenDom(msg.on);
                }
            }
        });
    }

    _build() {
        // ─── Anchor html + body + .st-root to viewport height so wl_root's
        //     overflow:hidden can actually clip. Base .st-root is min-height:100vh
        //     — grows when tall content pushes it; same for body and html in their
        //     default flow. Without bounding all three the document scrollbar
        //     appears, defeating "workspace locked in size". All three classes
        //     added; removed on destroy.
        document.body.classList.add(_workspaceActiveClassName());
        css.addClass(document.documentElement, wl_body_locked);
        css.addClass(document.body,            wl_body_locked);
        var stRoot = document.querySelector(".st-root");
        if (stRoot) css.addClass(stRoot, wl_body_locked);

        var root = document.createElement("div");
        css.setClass(root, wl_root);
        this._rootEl = root;

        // ─── Ribbon ────────────────────────────────────────────────────────
        var ribbon = document.createElement("div");
        css.setClass(ribbon, wl_ribbon);

        var titleEl = document.createElement("span");
        css.setClass(titleEl, wl_ribbon_title);
        titleEl.textContent = this._title;
        ribbon.appendChild(titleEl);

        var itemsEl = document.createElement("span");
        css.setClass(itemsEl, wl_ribbon_items);
        for (var i = 0; i < this._ribbonItems.length; i++) {
            itemsEl.appendChild(this._renderRibbonItem(this._ribbonItems[i]));
        }
        ribbon.appendChild(itemsEl);

        // ─── Fullscreen toggle (always present, framework-provided) ────────
        var fsBtn = document.createElement("button");
        css.setClass(fsBtn, wl_ribbon_fs);
        fsBtn.title = "Toggle full-screen (Esc to exit)";
        fsBtn.textContent = "⛶";   // ⛶ — fullscreen glyph
        var self = this;
        // Click sends FullscreenToggleRequested into the LayoutParty rather
        // than calling toggleFullScreen directly — the Secretary owns the
        // authoritative state, the body Actor performs the DOM mutation,
        // and the toggle Actor itself updates its own icon via the
        // FullscreenChanged broadcast it receives back.
        fsBtn.addEventListener("click", function () {
            self._party.tellFrom("layout/fullscreenToggle",
                                  { kind: "FullscreenToggleRequested" });
        });
        ribbon.appendChild(fsBtn);
        this._fsBtn = fsBtn;
        root.appendChild(ribbon);

        // ─── Content (where MultiTabPane mounts) ───────────────────────────
        var content = document.createElement("div");
        css.setClass(content, wl_content);
        root.appendChild(content);
        this.contentEl = content;

        // ─── Footer (optional — only when footerItems non-empty) ───────────
        if (this._footerItems.length > 0) {
            var footer = document.createElement("div");
            css.setClass(footer, wl_footer);
            for (var j = 0; j < this._footerItems.length; j++) {
                footer.appendChild(this._renderFooterItem(this._footerItems[j]));
            }
            root.appendChild(footer);
            this.footerEl = footer;
        }

        this._container.appendChild(root);
    }

    // ─── Item rendering — sealed-style switch on .kind ─────────────────────

    _renderRibbonItem(item) {
        var self = this;
        switch (item.kind) {
            case "Button": {
                var btn = document.createElement("button");
                css.setClass(btn, wl_ribbon_button);
                btn.title = item.tooltip || "";
                btn.textContent = item.icon && item.icon.kind === "emoji" ? item.icon.value : "?";
                btn.addEventListener("mouseenter", function () { css.addClass(btn, wl_ribbon_button_hover); });
                btn.addEventListener("mouseleave", function () { css.removeClass(btn, wl_ribbon_button_hover); });
                btn.addEventListener("click", function () {
                    if (self._onAction) try { self._onAction(item.actionId); } catch (e) { console.error("[WorkspaceLayout] onAction threw:", e); }
                });
                return btn;
            }
            case "Separator": {
                var sep = document.createElement("span");
                css.setClass(sep, wl_ribbon_separator);
                return sep;
            }
            case "Label": {
                var lbl = document.createElement("span");
                css.setClass(lbl, wl_ribbon_label);
                lbl.textContent = item.text;
                return lbl;
            }
            case "Choice": {
                // Typed dropdown: label + <select>. Change event emits
                // onAction(actionId, value) — the value is the selected
                // option's wire value (the workspace's onAction
                // interprets it). Note this extends the onAction
                // signature from (actionId) to (actionId, value); existing
                // handlers ignore the second arg, new ones use it.
                var container = document.createElement("label");
                css.setClass(container, wl_ribbon_label);
                container.textContent = item.label + " ";
                var sel = document.createElement("select");
                for (var k = 0; k < item.options.length; k++) {
                    var opt = document.createElement("option");
                    opt.value = item.options[k].value;
                    opt.textContent = item.options[k].label;
                    sel.appendChild(opt);
                }
                sel.addEventListener("change", function () {
                    if (self._onAction) try { self._onAction(item.actionId, sel.value); }
                    catch (e) { console.error("[WorkspaceLayout] onAction threw:", e); }
                });
                container.appendChild(sel);
                return container;
            }
            default: {
                var unknown = document.createElement("span");
                unknown.textContent = "?";
                return unknown;
            }
        }
    }

    _renderFooterItem(item) {
        var self = this;
        switch (item.kind) {
            case "Label": {
                var lbl = document.createElement("span");
                lbl.textContent = item.text;
                return lbl;
            }
            case "Separator": {
                var sep = document.createElement("span");
                css.setClass(sep, wl_footer_separator);
                return sep;
            }
            case "Button": {
                var btn = document.createElement("button");
                css.setClass(btn, wl_footer_button);
                btn.title = item.tooltip || "";
                btn.textContent = item.icon && item.icon.kind === "emoji" ? item.icon.value : "?";
                btn.addEventListener("click", function () {
                    if (self._onAction) try { self._onAction(item.actionId); } catch (e) { console.error("[WorkspaceLayout] onAction threw:", e); }
                });
                return btn;
            }
            default: {
                var unknown = document.createElement("span");
                unknown.textContent = "?";
                return unknown;
            }
        }
    }

    // ─── Fullscreen ────────────────────────────────────────────────────────

    /**
     * Public toggle — re-published as a message into LayoutParty so the
     * Secretary stays the single source of truth. External callers (and
     * the ribbon button) all funnel through the same routing.
     */
    toggleFullScreen() {
        this._party.tellFrom("layout/fullscreenToggle",
                              { kind: "FullscreenToggleRequested" });
    }

    /**
     * Public set-to-specific-value — sends FullscreenSetRequested(on) so
     * the Secretary can decide whether to no-op (already at this value) or
     * broadcast the FullscreenChanged fact.
     */
    setFullScreen(on) {
        this._party.tellFrom("layout/fullscreenToggle",
                              { kind: "FullscreenSetRequested", on: !!on });
    }

    /**
     * Read-only accessor. The Party snapshot is the authoritative source;
     * we read the layout Secretary's state directly via inspect().
     */
    isFullScreen() {
        var snap = this._party.inspect();
        var root = snap.secretaries.find(function (s) { return s.path === "layout"; });
        return !!(root && root.state && root.state.fullscreen);
    }

    // ─── DOM-side effect, invoked from the body Actor's reactor ────────────
    //
    // This is the ONLY place that touches DOM for fullscreen. Secretary
    // never reaches here; toggle Actor never reaches here. The body Actor's
    // FullscreenChanged reactor calls this with the new state, and the
    // method performs both DOM class toggling and Escape-handler attach /
    // detach. The Escape handler itself bounces back into LayoutParty via
    // setFullScreen(false), keeping all state transitions routed through
    // the Secretary.
    _applyFullscreenDom(on) {
        var self = this;
        if (on) {
            document.body.classList.add(_bodyFullscreenClassName());
            css.addClass(this._rootEl, wl_root_fullscreen);
            var header = document.querySelector(".st-header");
            if (header) css.addClass(header, wl_chrome_hidden);
            this._escHandler = function (e) {
                if (e.key === "Escape") self.setFullScreen(false);
            };
            document.addEventListener("keydown", this._escHandler);
        } else {
            document.body.classList.remove(_bodyFullscreenClassName());
            css.removeClass(this._rootEl, wl_root_fullscreen);
            var header2 = document.querySelector(".st-header");
            if (header2) css.removeClass(header2, wl_chrome_hidden);
            if (this._escHandler) {
                document.removeEventListener("keydown", this._escHandler);
                this._escHandler = null;
            }
        }
    }

    destroy() {
        // Ensure fullscreen is exited (so global classes get removed),
        // routed through the Party as everything else is.
        if (this.isFullScreen()) this.setFullScreen(false);
        if (this._escHandler) document.removeEventListener("keydown", this._escHandler);
        this._escHandler = null;
        // Drop Party Actors. The Party itself is workspace-local; its
        // Secretary state dies with this layout instance.
        if (this._party) {
            try { this._party.leave("layout/fullscreenToggle"); } catch (e) {}
            try { this._party.leave("layout/body"); } catch (e) {}
            this._party = null;
        }
        // Restore html + body + .st-root to their base styles so non-workspace
        // pages that follow get normal page scroll behaviour back.
        document.body.classList.remove(_workspaceActiveClassName());
        css.removeClass(document.documentElement, wl_body_locked);
        css.removeClass(document.body,            wl_body_locked);
        var stRoot = document.querySelector(".st-root");
        if (stRoot) css.removeClass(stRoot, wl_body_locked);
        if (this._rootEl && this._rootEl.parentNode) this._rootEl.parentNode.removeChild(this._rootEl);
        this._rootEl = null;
    }
}
