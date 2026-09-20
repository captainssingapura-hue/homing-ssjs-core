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
//   Card — a BRANCH component: it takes a sub-branch the caller made for it
//   and mints its own tree on it. One card per branch.
//     new Card(branch, { title, text?, badge?, link? })    link: { to, label? }
//       .root            the element the caller appends
//
// A button's type is "button" unless the caller says otherwise afterwards,
// so a button inside a form does not submit it by accident. A card is a
// raised box: a heading with an optional badge, a line of text, and a link
// pushed to the bottom, its address set through the manager so a typed
// address stays typed. `css` is injected with the styles import; the
// manager comes in by its explicit HrefManager import.
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
    constructor(branch, props) {
        if (!branch) throw new Error("[Card] a branch of its own is required");
        var p = props || {};
        branch.activate(_cardOwner);
        var card = branch.createElement("card", "div");
        css.addClass(card, el_card);
        var title = branch.createElement("title", "h2");
        css.addClass(title, el_card_title);
        var titleText = branch.createElement("title-text", "span");
        titleText.textContent = p.title == null ? "" : String(p.title);
        title.appendChild(titleText);
        if (p.badge) {
            var badge = branch.createElement("badge", "span");
            css.addClass(badge, el_badge);
            badge.textContent = String(p.badge);
            title.appendChild(badge);
        }
        card.appendChild(title);
        if (p.text) {
            var text = branch.createElement("text", "p");
            css.addClass(text, el_card_text);
            text.textContent = String(p.text);
            card.appendChild(text);
        }
        if (p.link && p.link.to) {
            var a = branch.createElement("link", "a");
            css.addClass(a, el_card_link);
            HrefManagerInstance.set(a, p.link.to);
            a.textContent = p.link.label == null ? String(p.link.to) : String(p.link.label);
            card.appendChild(a);
        }
        this.root = card;
    }
}
