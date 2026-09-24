// =============================================================================
// AddTab — the standalone control for putting a new tab somewhere: WHERE,
// picked off a picture of the panes, and WHAT, picked off a list of the kinds
// a TabSource can make. Then one button, which does exactly what the strip's
// own plus would do, through the same source.
//
//   new AddTab(branch, { host, source, panes, onAdded?, width?, verb?, mode? })
//     source: a TabSource — the kinds, and the rule for minting one
//     panes:  () → [pane], asked afresh: a workspace is split and merged
//             while the control stands there
//     onAdded: (pane, tab, index) after it has landed
//     mode:   how the tab arrives; "front" unless said. NOT "focus": you are
//             standing in this control, and a control that answers you by
//             taking the keys out of your hand and putting them in another
//             room has answered a question you did not ask. The plus at the
//             end of a strip is the other case, and it is not this one
//
//   add.refresh()      measure the panes again, and say again what can be done
//   add.picked()       the pane the control is pointing at, or null
//   add.dispose()
//
// IT IS A CONTROL, NOT A POLICY. It asks the source whether a pane will take
// another and shows the answer; it never decides that a dock is full, never
// reorders, never chooses for you. The two questions are separate on purpose —
// a room and a thing — because they are answered by different parts of what
// you know, and a single list of "kind in pane" would be the product of the
// two and read as neither.
//
// The whole control is the NATIVE world: a button per thumbnail, a select, a
// button. Nothing here joins the keyboard party — the browser's own focus
// walks it, Enter and Space press, the select opens on its own keys — which is
// what a small control should be, and what leaves the party to the containers
// that need it.
// =============================================================================

const _addTabOwner = Object.freeze({ toString: () => "addTab" });

class AddTab {
    constructor(branch, opts) {
        if (!branch) throw new Error("[AddTab] a branch of its own is required");
        var o = opts || {};
        if (!o.source) throw new Error("[AddTab] a TabSource is what it adds from");
        if (typeof o.panes !== "function") throw new Error("[AddTab] panes() is asked afresh every refresh: hand in the function, not the list");
        branch.activate(_addTabOwner);
        var self = this;
        this.branch = branch;
        this._source = o.source;
        this._panes = o.panes;
        this._onAdded = typeof o.onAdded === "function" ? o.onAdded : null;
        this._mode = o.mode == null ? "front" : String(o.mode);
        this._first = true;
        this._disposed = false;

        var root = branch.createElement("addtab", "div");
        css.addClass(root, mtp_new);
        this.root = root;
        this.el = root;

        this._thumbs = new PaneThumbs(branch.createBranch("thumbs"), {
            host: root, width: o.width,
            panes: function () { return self._panes(); },
            enabledOf: function (p) { return self._source.canAdd(p); },
            onPick: function () { self._dress(); },
            onDrawn: function () { self._after(); }   // the picture redrew itself: say again what can be done
        });

        var pick = branch.createElement("kind", "select");
        css.addClass(pick, mtp_new_pick);
        pick.setAttribute("aria-label", "what to mount");
        this._source.kinds().forEach(function (k) {
            var opt = branch.createElement("kind-" + k.id, "option");
            opt.value = k.id;
            opt.textContent = k.label;
            pick.appendChild(opt);
        });
        root.appendChild(pick);
        this._pick = pick;

        var go = branch.createElement("go", "button");
        css.addClass(go, mtp_add);
        go.setAttribute("type", "button");
        go.textContent = o.verb == null ? "Add" : String(o.verb);
        go.addEventListener("click", function () { self.add(); });
        root.appendChild(go);
        this._go = go;

        if (o.host) o.host.appendChild(root);
        this.refresh();
    }

    /** The pane the control is pointing at, or null. */
    picked() { return this._thumbs.picked(); }

    /** Point it at one in code — the same thing a press on a thumbnail does. */
    pick(pane) { this._thumbs.pick(pane); this._dress(); return this; }

    /** The kind that would be mounted, by id. */
    kind() { return this._pick.value; }

    /**
     * The gesture, by call: the chosen kind into the chosen pane. Returns the
     * index it landed at, or −1 — no pane chosen, or one that would not take
     * it — and in neither case was anything made.
     */
    add() {
        var pane = this.picked();
        if (!pane || !this._source.canAdd(pane)) return -1;
        var tab = this._source.mint(pane, this._pick.value);
        var at = pane.addTab(tab);
        this._source.show(pane, tab, this._mode);   // in front, and the keys stay in the hand that is using this
        this.refresh();
        if (this._onAdded) this._onAdded(pane, tab, at);
        return at;
    }

    /**
     * Measure the panes again and say again what can be done. Called after
     * every add, and by the page whenever the room changes under it — a split,
     * a merge, a tab closed somewhere else — since a picture of where the
     * panes are is only true until they move.
     */
    refresh() {
        if (this._disposed) return this;
        this._thumbs.refresh();   // which calls back through onDrawn, so _after runs exactly once
        return this;
    }

    /**
     * What follows a drawing of the picture, whoever asked for it — this
     * control, or the picture itself a frame after the panes changed size.
     * The first pane is chosen for you ONCE, so the control does not open
     * looking broken, but only once there IS one to choose: a page is built
     * detached and appended afterwards, and until then every pane is nought
     * pixels wide and the picture is empty.
     */
    _after() {
        if (!this._thumbs) return;   // the picture draws once inside its own constructor, before this holds it
        if (this._first && !this._thumbs.picked()) {
            var open = this._panes().filter(this._source.canAdd, this._source);
            if (open.length > 0) { this._thumbs.pick(open[0]); this._first = false; }
        } else if (this._thumbs.picked()) {
            this._first = false;
        }
        this._dress();
    }

    /** Whether the button would do anything, said on the button: the source's answer about the chosen pane, and nothing of its own. */
    _dress() {
        var pane = this._thumbs.picked(), can = !!pane && this._source.canAdd(pane);
        css.toggleClass(this._go, mtp_add_off, !can);
        this._go.disabled = !can;
        this._go.setAttribute("title", !pane ? "pick a pane first"
                                  : can ? "a new tab in " + pane.slotId
                                        : pane.slotId + " is full: " + pane.count() + " of " + pane.budget());
    }

    dispose() {
        if (this._disposed) return;
        this._disposed = true;
        this._thumbs.dispose();
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        try { this.branch.dissolve(); } catch (e) {}
    }
}
