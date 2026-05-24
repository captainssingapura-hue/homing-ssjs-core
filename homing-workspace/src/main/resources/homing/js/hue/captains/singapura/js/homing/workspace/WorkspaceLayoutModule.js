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
        this._fullScreen  = false;
        this._escHandler  = null;
        this._build();
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
        fsBtn.addEventListener("click", function () { self.toggleFullScreen(); });
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
     * Toggle fullscreen state. Body gets wl-fullscreen-active class; the
     * workspace's own root gets wl-root-fullscreen; studio chrome surfaces
     * (.st-header) get wl-chrome-hidden. Escape exits.
     */
    toggleFullScreen() { this.setFullScreen(!this._fullScreen); }

    setFullScreen(on) {
        if (on === this._fullScreen) return;
        this._fullScreen = !!on;
        var self = this;
        if (this._fullScreen) {
            document.body.classList.add(_bodyFullscreenClassName());
            css.addClass(this._rootEl, wl_root_fullscreen);
            // Hide studio chrome — the studio's Header lives at .st-header.
            // Future studio surfaces (footer, sidebar) tag with the same
            // class via their own CSS or are hidden by extending this list.
            var header = document.querySelector(".st-header");
            if (header) css.addClass(header, wl_chrome_hidden);
            this._fsBtn.textContent = "⤢";   // ⤢ — exit-fullscreen glyph
            // Escape exits.
            this._escHandler = function (e) { if (e.key === "Escape") self.setFullScreen(false); };
            document.addEventListener("keydown", this._escHandler);
        } else {
            document.body.classList.remove(_bodyFullscreenClassName());
            css.removeClass(this._rootEl, wl_root_fullscreen);
            var header2 = document.querySelector(".st-header");
            if (header2) css.removeClass(header2, wl_chrome_hidden);
            this._fsBtn.textContent = "⛶";   // ⛶
            if (this._escHandler) {
                document.removeEventListener("keydown", this._escHandler);
                this._escHandler = null;
            }
        }
    }

    isFullScreen() { return this._fullScreen; }

    destroy() {
        if (this._escHandler) document.removeEventListener("keydown", this._escHandler);
        this._escHandler = null;
        if (this._fullScreen) this.setFullScreen(false);
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
