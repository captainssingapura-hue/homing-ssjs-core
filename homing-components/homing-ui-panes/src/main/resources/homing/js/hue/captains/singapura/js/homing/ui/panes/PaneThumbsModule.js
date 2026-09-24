// =============================================================================
// PaneThumbs — a picture of where the panes are, to pick one by. A button per
// pane, laid out in the proportions the panes really have on the screen, in a
// frame of the same shape as the room they fill. You choose a pane by pointing
// at the small one in the same place as the big one.
//
//   new PaneThumbs(branch, { host, panes, onPick?, labelOf?, enabledOf?, width? })
//     panes:     () → [pane], asked afresh on every refresh, because a
//                workspace is split and merged while the control stands there
//     onPick:    (pane) as one is chosen; the picture decides nothing else
//     labelOf:   (pane) → the word in the box; its slotId unless said
//     enabledOf: (pane) → whether it can be chosen at all; everything can
//                unless said. A pane that cannot is SHOWN, not hidden: where it
//                is is part of the picture, and a gap would be a lie about the room
//
//   thumbs.refresh()       measure again and lay out again, by hand
//   thumbs.picked()        the chosen pane, or null
//   thumbs.pick(pane)      choose one in code; null chooses none
//   thumbs.dispose()
//
// IT WATCHES rather than measures once. A page is built detached and appended
// afterwards, so everything is nought pixels wide at the moment a control is
// constructed; a window is resized; a splitter is dragged. All three are the
// same event — a pane changed size — so the panes themselves are what is
// observed, and the picture is redrawn a frame later. Nothing has to remember
// to nudge it.
//
// IT MEASURES RATHER THAN READS A LAYOUT. A grid could describe its own tree
// and this would draw that, but then it would work for a grid and nothing
// else — not a dock that floats, not two workspaces side by side, not a pane
// somebody simply put on the page. Rectangles are what every pane has in
// common, so rectangles are what this knows: the union of them is the room,
// and each one's share of it is where its thumbnail goes. The picture is
// therefore always of what is actually there.
// =============================================================================

const _thumbsOwner = Object.freeze({ toString: () => "paneThumbs" });

class PaneThumbs {
    constructor(branch, opts) {
        if (!branch) throw new Error("[PaneThumbs] a branch of its own is required");
        var o = opts || {}, self = this;
        if (typeof o.panes !== "function") throw new Error("[PaneThumbs] panes() is asked afresh every refresh: hand in the function, not the list");
        branch.activate(_thumbsOwner);
        this.branch = branch;
        this._panes = o.panes;
        this._onPick = typeof o.onPick === "function" ? o.onPick : null;
        this._onDrawn = typeof o.onDrawn === "function" ? o.onDrawn : null;
        this._labelOf = typeof o.labelOf === "function" ? o.labelOf : function (p) { return p.slotId; };
        this._enabledOf = typeof o.enabledOf === "function" ? o.enabledOf : function () { return true; };
        this._picked = null;
        this._at = {};          // slotId → { el, label, pane }
        this._watched = [];     // the pane elements under the observer, kept so they can be dropped again
        this._due = false;      // a redraw is already scheduled for the next frame
        this._disposed = false;
        this._sees = typeof ResizeObserver === "function" ? new ResizeObserver(function () { self._soon(); }) : null;

        var root = branch.createElement("thumbs", "div");
        css.addClass(root, mtp_thumbs);
        root.setAttribute("role", "group");
        if (o.width) root.style.setProperty("--thumbs-width", o.width);
        this.root = root;
        this.el = root;
        if (o.host) o.host.appendChild(root);
        this.refresh();
        this._soon();   // built detached, appended by the caller: the sizes are all nought until a frame has passed
    }

    /**
     * A redraw once, soon, however many times it is asked for — a resize
     * arrives per pane and per pass. The next FRAME is the right moment, since
     * that is when the layout the picture is of has settled; but a document
     * that is hidden never paints, and a control built in a background tab
     * must still be right when the tab is looked at. So a timer runs beside
     * the frame and whichever comes first does the work.
     */
    _soon() {
        if (this._due || this._disposed) return;
        this._due = true;
        var self = this, done = false;
        var go = function () {
            if (done || self._disposed) return;
            done = true;
            self._due = false;
            self.refresh();
        };
        if (typeof requestAnimationFrame === "function") requestAnimationFrame(go);
        setTimeout(go, 0);
    }

    /** The chosen pane, or null. */
    picked() { return this._picked; }

    /** Drawn — by a call, or by the panes changing size under it. What that means to whoever is holding it is theirs. */
    _drawn() { if (this._onDrawn) this._onDrawn(); }

    /** Choose one, or none. The same call a press makes, so a page can set the target without a click. */
    pick(pane) {
        var was = this._picked;
        this._picked = pane || null;
        if (was !== this._picked) this._dress();
        if (this._onPick && was !== this._picked) this._onPick(this._picked);
        return this;
    }

    /**
     * Measure and lay out: every pane that is on the screen gets a box in the
     * frame, in its own share of the union of them all. A pane that has gone
     * takes its thumbnail with it — and the choice, if it was the chosen one,
     * because a control pointing at a pane that no longer exists is worse than
     * one pointing at nothing.
     */
    refresh() {
        if (this._disposed) return this;
        var list = this._panes().filter(function (p) { return p && p.el; });
        var seen = {}, rects = [], self = this;
        list.forEach(function (p) {
            var r = p.el.getBoundingClientRect();
            if (r.width > 0 && r.height > 0) { seen[p.slotId] = true; rects.push({ pane: p, r: r }); }
        });
        Object.keys(this._at).forEach(function (id) { if (!seen[id]) self._forget(id); });
        this._watch(list);
        if (rects.length === 0) { this._picked = null; this._drawn(); return this; }
        var u = PaneThumbs.union(rects.map(function (x) { return x.r; }));
        this.root.style.setProperty("--thumbs-aspect", (u.w / u.h).toFixed(4));
        rects.forEach(function (x) { self._place(x.pane, x.r, u); });
        if (this._picked && !seen[this._picked.slotId]) this._picked = null;
        this._dress();
        this._drawn();
        return this;
    }

    /**
     * The panes under the observer, brought level with the list: one of them
     * changing size is the only thing that can make this picture wrong, and it
     * covers the three that look like different problems — a page appended
     * after it was built, a window resized, a splitter dragged.
     */
    _watch(list) {
        if (!this._sees) return;
        var want = list.map(function (p) { return p.el; }), self = this;
        this._watched.forEach(function (el) { if (want.indexOf(el) < 0) self._sees.unobserve(el); });
        want.forEach(function (el) { if (self._watched.indexOf(el) < 0) self._sees.observe(el); });
        this._watched = want;
    }

    /** The room every pane is in: the smallest rectangle holding all of them. */
    static union(rs) {
        var l = Infinity, t = Infinity, r = -Infinity, b = -Infinity;
        rs.forEach(function (x) { l = Math.min(l, x.left); t = Math.min(t, x.top); r = Math.max(r, x.right); b = Math.max(b, x.bottom); });
        return { l: l, t: t, w: Math.max(1, r - l), h: Math.max(1, b - t) };
    }

    _place(pane, r, u) {
        var at = this._at[pane.slotId] || this._mint(pane);
        at.pane = pane;
        var pc = function (n) { return (n * 100).toFixed(3) + "%"; };
        at.el.style.setProperty("--thumb-x", pc((r.left - u.l) / u.w));
        at.el.style.setProperty("--thumb-y", pc((r.top - u.t) / u.h));
        at.el.style.setProperty("--thumb-w", pc(r.width / u.w));
        at.el.style.setProperty("--thumb-h", pc(r.height / u.h));
        at.label.textContent = String(this._labelOf(pane));
        at.el.setAttribute("title", this._labelOf(pane) + " — " + pane.count() + " of " + pane.budget());
    }

    /**
     * One box, on a branch of its own named for the pane it stands for. If
     * anything in the building of it fails, the BRANCH GOES TOO: a name is
     * taken by createBranch and freed only by a dissolve, so a half-made
     * thumbnail left behind would refuse every attempt to make it again, and
     * the picture would be empty ever after with nothing to say why.
     */
    _mint(pane) {
        var self = this, own = this.branch.createBranch("thumb-" + pane.slotId);
        try {
            own.activate(_thumbsOwner);
            var el = own.createElement("thumb", "button");
            css.addClass(el, mtp_thumb);
            el.setAttribute("type", "button");
            var label = own.createElement("label", "span");
            css.addClass(label, mtp_thumb_label);
            el.appendChild(label);
            el.addEventListener("click", function () { var at = self._at[pane.slotId]; if (at && self._enabledOf(at.pane)) self.pick(at.pane); });
            this.root.appendChild(el);
            this._at[pane.slotId] = { el: el, label: label, pane: pane, branch: own };
            return this._at[pane.slotId];
        } catch (e) {
            try { this.branch.dissolveBranch("thumb-" + pane.slotId); } catch (e2) {}
            throw e;
        }
    }

    _forget(id) {
        var at = this._at[id];
        if (!at) return;
        if (at.el.parentNode === this.root) this.root.removeChild(at.el);
        delete this._at[id];
        try { this.branch.dissolveBranch("thumb-" + id); } catch (e) {}
    }

    /** Which one is chosen and which can be, said on the boxes: colour and a cursor, and nothing that moves. */
    _dress() {
        var self = this;
        Object.keys(this._at).forEach(function (id) {
            var at = self._at[id], can = self._enabledOf(at.pane);
            css.toggleClass(at.el, mtp_thumb_on, at.pane === self._picked);
            css.toggleClass(at.el, mtp_thumb_off, !can);
            at.el.disabled = !can;
            at.el.setAttribute("aria-pressed", at.pane === self._picked ? "true" : "false");
        });
    }

    dispose() {
        if (this._disposed) return;
        this._disposed = true;
        if (this._sees) { this._sees.disconnect(); this._sees = null; }
        this._watched = [];
        this._at = {};
        this._picked = null;
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        try { this.branch.dissolve(); } catch (e) {}
    }
}
