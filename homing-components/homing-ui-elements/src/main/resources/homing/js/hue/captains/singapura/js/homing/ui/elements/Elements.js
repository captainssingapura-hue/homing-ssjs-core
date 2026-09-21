// =============================================================================
// Elements — a button, its builder, and a card, as classes.
//
//   Button — an ELEMENT component, made through its builder:
//     var b = new ButtonBuilder();               b.tag → "button": what the caller mints
//     var el = branch.createElement(name, b.tag);
//     var button = b.label("Delete").colour("danger", 0.6).onClick(fn).build(el);
//
//   ButtonBuilder — the properties, set progressively, each returning the builder:
//     .label(text)                  the text
//     .colour(word, extent?)        the colour word: "primary" (the default), "secondary",
//                                   "danger", "warning", "success", "plain"; and the extent
//                                   of it, −1 … 1, default 1
//     .extent(t)                    the extent alone
//     .size(s)                      the size: −1 the smallest, 0 regular (the default), 1 the
//                                   biggest — every length the design gives the button grows
//                                   by its own ratio to the power of it
//     .plain()                      colour("plain")
//     .onClick(fn)
//     .build(el)                    → the Button, dressing the element it was given
//
//   Button — the instance, adjusted live:
//     .el                           the element
//     .colour(word, extent?)        another word; the extent kept unless given
//     .extent(t)                    the number that scales the word: 1 the word as
//                                   the design binds it, 0 the design's neutral, −1
//                                   the meaning turned the other way — safety for
//                                   danger, calm for warning, failure for success
//     .size(s)                      the size, live
//     .label(text?)                 read, or set
//     .setOn(on)                    on lifts and presses as the design has it; off is inert and says so
//
//   A colour word is a semantic surface, complete: its surface, the ink on it
//   and its edge move together with the extent, each along the anchors the
//   design gives that target. Plain is not semantic — raised, in body ink —
//   and does not scale.
//
//   Card — a BRANCH component, made through its builder: it takes a sub-branch
//   the caller made for it and mints its own tree on it. One card per branch.
//     var card = new CardBuilder().title("Grid").badge("new").text("…").link(to, label?)
//                                 .size(0).aspect(0).onClick(fn?).keyboard(steward, id?).build(branch.createBranch("card"));
//       .root            the element the caller appends
//       .body            the bounded region inside; a caller with more than text mints there
//       .size(s)         the size, live, on the card and its parts
//       .aspect(a)       the aspect, live: −1 the tallest the design allows, 0 square, 1 the widest
//       .title(text?)    read, or set
//       .key(ev)         a keydown from whoever holds the keys for it: Enter or Space on a card
//                        with an action is the action; true when taken. A card handed the page's
//                        steward (.keyboard) is a member of the keyboard party and claims by the
//                        convention; one inside a pane or a dialog is built without, and its
//                        holder hands it the keys. No keydown listener of its own, ever
//       .dispose()       dissolves the branch
//   The card is a hard frame: its inline size is the design's, grown by its
//   size; its block size follows its aspect — the design's widest to the power
//   of it, square at 0; a host may only cap it; and the body scrolls beyond
//   what the head and the foot leave. It lifts, presses and rings as the design has an
//   interactive thing do; with onClick it is a button to the keyboard too
//   (role, tabindex, Enter and Space); without, nothing happens on a press.
//
// A button's type is "button" unless the caller says otherwise afterwards,
// so a button inside a form does not submit it by accident. A card's link
// has its address set through the manager so a typed address stays typed.
// `css` is injected with the styles import; the manager comes in by its
// explicit HrefManager import.
// =============================================================================

const _cardOwner = Object.freeze({ toString: () => "card" });

var _COLOURS = Object.freeze({
    primary:   Object.freeze({ cls: function () { return el_button_primary; },   scales: true }),
    secondary: Object.freeze({ cls: function () { return el_button_secondary; }, scales: true }),
    danger:    Object.freeze({ cls: function () { return el_button_danger; },    scales: true }),
    warning:   Object.freeze({ cls: function () { return el_button_warning; },   scales: true }),
    success:   Object.freeze({ cls: function () { return el_button_success; },   scales: true }),
    plain:     Object.freeze({ cls: function () { return el_button_plain; },     scales: false })
});

class Button {
    static TAG = "button";
    static COLOURS = Object.freeze(Object.keys(_COLOURS));

    /** The builder's; a caller makes a button through ButtonBuilder. */
    constructor(el, props) {
        if (!el) throw new Error("[Button] the element is required: mint it with the builder's tag on your branch");
        var p = props || {};
        this.el = el;
        this._colour = null;
        this._extent = 1;
        this._size = 0;
        el.type = "button";
        css.addClass(el, el_button);
        css.addClass(el, el_button_on);
        this.label(p.label == null ? "" : p.label);
        this.colour(p.colour || "primary", p.extent == null ? 1 : p.extent);
        this.size(p.size == null ? 0 : p.size);
        if (typeof p.onClick === "function") el.addEventListener("click", p.onClick);
    }

    colour(word, extent) {
        var c = _COLOURS[word];
        if (!c) throw new Error("[Button] no colour word '" + word + "'; one of " + Button.COLOURS.join(", "));
        if (this._colour) css.removeClass(this.el, _COLOURS[this._colour].cls());
        this._colour = word;
        css.addClass(this.el, c.cls());
        return this.extent(extent == null ? this._extent : extent);
    }

    extent(t) {
        var n = Math.max(-1, Math.min(1, Number(t)));
        this._extent = Number.isFinite(n) ? n : 1;
        css.extent(this.el, _COLOURS[this._colour].scales ? this._extent : null);
        return this;
    }

    size(s) {
        var n = Math.max(-1, Math.min(1, Number(s)));
        this._size = Number.isFinite(n) ? n : 0;
        css.size(this.el, this._size === 0 ? null : this._size);
        return this;
    }

    label(text) {
        if (text !== undefined) this.el.textContent = String(text);
        return this.el.textContent;
    }

    setOn(on) {
        this.el.disabled = !on;
        css.toggleClass(this.el, el_button_on, on);
        css.toggleClass(this.el, el_button_off, !on);
        return this;
    }
}

class ButtonBuilder {
    constructor() {
        this.tag = Button.TAG;
        this._props = { colour: "primary", extent: 1, size: 0 };
    }
    label(text)          { this._props.label = text; return this; }
    colour(word, extent) { if (!_COLOURS[word]) throw new Error("[ButtonBuilder] no colour word '" + word + "'"); this._props.colour = word; if (extent != null) this._props.extent = extent; return this; }
    extent(t)            { this._props.extent = t; return this; }
    size(s)              { this._props.size = s; return this; }
    plain()              { return this.colour("plain"); }
    onClick(fn)          { this._props.onClick = fn; return this; }
    build(el) {
        if (!el || String(el.tagName).toLowerCase() !== this.tag) throw new Error("[ButtonBuilder] build wants the element minted with its tag, <" + this.tag + ">");
        return new Button(el, this._props);
    }
}

class Card {
    /** The builder's; a caller makes a card through CardBuilder. */
    constructor(branch, props) {
        if (!branch) throw new Error("[Card] a branch of its own is required");
        var self = this;
        var p = props || {};
        branch.activate(_cardOwner);
        this.branch = branch;
        this._size = 0;
        this._aspect = 0;
        var card = branch.createElement("card", "article");
        css.addClass(card, el_card);

        var head = branch.createElement("head", "header");
        css.addClass(head, el_card_head);
        var title = branch.createElement("title", "h2");
        css.addClass(title, el_card_title);
        this._title = branch.createElement("title-text", "span");
        this._title.textContent = p.title == null ? "" : String(p.title);
        title.appendChild(this._title);
        head.appendChild(title);
        if (p.badge) {
            var badge = branch.createElement("badge", "span");
            css.addClass(badge, el_badge);
            badge.textContent = String(p.badge);
            head.appendChild(badge);
        }
        card.appendChild(head);

        var body = branch.createElement("body", "div");
        css.addClass(body, el_card_body);
        if (p.text) {
            var text = branch.createElement("text", "p");
            css.addClass(text, el_card_text);
            text.textContent = String(p.text);
            body.appendChild(text);
        }
        card.appendChild(body);

        if (p.link && p.link.to) {
            var foot = branch.createElement("foot", "footer");
            css.addClass(foot, el_card_foot);
            var a = branch.createElement("link", "a");
            css.addClass(a, el_card_link);
            HrefManagerInstance.set(a, p.link.to);
            a.textContent = p.link.label == null ? String(p.link.to) : String(p.link.label);
            foot.appendChild(a);
            card.appendChild(foot);
        }

        this._action = typeof p.onClick === "function";
        this._kb = null; this._kbId = null; this._offKeys = null;
        if (this._action) {   // an action: the card is a button to the keyboard too, through the party
            css.addClass(card, el_card_action);
            card.setAttribute("role", "button");
            card.tabIndex = 0;
            card.addEventListener("click", p.onClick);
            if (p.keyboard) {
                var self = this;
                this._kb = p.keyboard;
                this._kbId = this._kb.join(p.keyboardId != null ? String(p.keyboardId) : branch.name, { keyDown: function (ev) { return self.key(ev); } });
                this._offKeys = Keys.claimOn(card, this._kb, this._kbId);
            }
        }

        this.root = card;
        this.body = body;
        this._parts = [card, head].concat(badge ? [badge] : [], text ? [text] : [], a ? [a] : []);
        this._parts.push(title);
        this.size(p.size == null ? 0 : p.size);
        this.aspect(p.aspect == null ? 0 : p.aspect);
    }

    /** The aspect: on the card alone, since only its frame has a proportion. */
    aspect(a) {
        var n = Math.max(-1, Math.min(1, Number(a)));
        this._aspect = Number.isFinite(n) ? n : 0;
        css.aspect(this.root, this._aspect === 0 ? null : this._aspect);
        return this;
    }

    /** The size, on the card and on every part it minted: the size is an element's, not inherited, so the card carries it to its own. */
    size(s) {
        var n = Math.max(-1, Math.min(1, Number(s)));
        this._size = Number.isFinite(n) ? n : 0;
        var v = this._size === 0 ? null : this._size;
        this._parts.forEach(function (el) { css.size(el, v); });
        return this;
    }

    title(text) {
        if (text !== undefined) this._title.textContent = String(text);
        return this._title.textContent;
    }

    /** The card and everything on its branch go together. */
    /** A keydown from whoever holds the keys for the card: Enter or Space on the card is the action; true when taken. */
    key(ev) {
        if (!this._action || !ev || (ev.key !== "Enter" && ev.key !== " ") || ev.target !== this.root) return false;
        this.root.click();
        return true;
    }

    dispose() {
        if (this._offKeys) { this._offKeys(); this._offKeys = null; }
        if (this._kb) { this._kb.leave(this._kbId); this._kb = null; }
        try { this.branch.dissolve(); } catch (e) {}
    }
}

class CardBuilder {
    constructor() { this._props = { size: 0, aspect: 0 }; }
    title(text)      { this._props.title = text; return this; }
    badge(text)      { this._props.badge = text; return this; }
    text(text)       { this._props.text = text; return this; }
    link(to, label)  { this._props.link = { to: to, label: label }; return this; }
    size(s)          { this._props.size = s; return this; }
    aspect(a)        { this._props.aspect = a; return this; }
    onClick(fn)      { this._props.onClick = fn; return this; }
    keyboard(steward, id) { this._props.keyboard = steward; this._props.keyboardId = id; return this; }
    build(branch) {
        if (!branch) throw new Error("[CardBuilder] build wants the sub-branch the caller made for the card");
        return new Card(branch, this._props);
    }
}
