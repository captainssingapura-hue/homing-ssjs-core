// =============================================================================
// Panel — a head, a body, and whatever is mounted in it. The workspace's unit
// of work: a named region that fills what holds it — a cell of a grid, a
// pane's tab, a page — and brings nothing else. It measures nothing: what
// holds it decides how big it is, and it fills that.
//
//   PanelBuilder — the properties, set progressively, each returning the builder:
//     .title(text)          the name in the head; without one there is no head
//     .fills(on)            what is mounted fills the body: no air, and no
//                           scroll of its own (a dock, a grid). Off, the body
//                           has the design's air and scrolls — for content.
//     .size(s)              -1 to 1: every length the design gives the panel
//                           grown by its own ratio to the power of it
//     .host(el)             appended there on build
//     .build(branch)        the Panel, on a sub-branch the caller made
//
//   Panel — the instance:
//     .root                 the element, appended to the host when one was given
//     .head                 the bar; null when the panel has no title
//     .controls             the slot at the end of the head: the caller appends
//                           what acts on the panel — buttons, a menu mark
//     .body                 where the caller mounts what the panel shows
//     .title(text?)         read, or set
//     .size(s)              the size, live, on the panel and its parts
//     .dispose()            the panel and everything on its branch
//
// It takes no keys and listens to nothing: a panel is furniture. What is
// mounted in it may be a member of the keyboard party; the panel neither
// knows nor interferes.
// =============================================================================

const _panelOwner = Object.freeze({ toString: () => "panel" });

class Panel {
    /** The builder's; a caller makes a panel through PanelBuilder. */
    constructor(branch, props) {
        if (!branch) throw new Error("[Panel] a branch of its own is required");
        var p = props || {};
        branch.activate(_panelOwner);
        this.branch = branch;
        this._size = 0;

        var root = branch.createElement("panel", "section");
        css.addClass(root, el_panel);
        this._parts = [root];

        this.head = null;
        this.controls = null;
        this._title = null;
        if (p.title != null) {
            var head = branch.createElement("head", "header");
            css.addClass(head, el_panel_head);
            var title = branch.createElement("title", "h2");
            css.addClass(title, el_panel_title);
            title.textContent = String(p.title);
            head.appendChild(title);
            var slot = branch.createElement("controls", "div");
            css.addClass(slot, el_panel_slot);
            head.appendChild(slot);
            root.appendChild(head);
            root.setAttribute("aria-label", String(p.title));
            this.head = head;
            this.controls = slot;
            this._title = title;
            this._parts.push(head, title, slot);
        }

        var body = branch.createElement("body", "div");
        css.addClass(body, el_panel_body);
        if (!p.fills) { css.addClass(body, el_panel_body_air); this._parts.push(body); }
        root.appendChild(body);
        this.body = body;

        this.root = root;
        this.size(p.size == null ? 0 : p.size);
        if (p.host) p.host.appendChild(root);
    }

    /** The name in the head, read or set; a panel built without one has no head to name. */
    title(text) {
        if (!this._title) return null;
        if (text !== undefined) { this._title.textContent = String(text); this.root.setAttribute("aria-label", String(text)); }
        return this._title.textContent;
    }

    /** The size, on the panel and on every part it minted: the size is an element's, not inherited. */
    size(s) {
        var n = Math.max(-1, Math.min(1, Number(s)));
        this._size = Number.isFinite(n) ? n : 0;
        var v = this._size === 0 ? null : this._size;
        this._parts.forEach(function (el) { css.size(el, v); });
        return this;
    }

    /** The panel and everything on its branch go together; what was mounted in the body is the mounter's to dispose. */
    dispose() {
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        try { this.branch.dissolve(); } catch (e) {}
    }
}

class PanelBuilder {
    constructor() { this._props = {}; }
    title(text)  { this._props.title = text; return this; }
    fills(on)    { this._props.fills = on === undefined ? true : !!on; return this; }
    size(s)      { this._props.size = s; return this; }
    host(el)     { this._props.host = el; return this; }
    build(branch) {
        if (!branch) throw new Error("[PanelBuilder] build wants the sub-branch the caller made for the panel");
        return new Panel(branch, this._props);
    }
}
