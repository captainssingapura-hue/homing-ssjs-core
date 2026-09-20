// =============================================================================
// Elements — a button and a card, as classes.
//
//   Button — an ELEMENT component: it tells the caller which element to mint
//   and takes it. The caller creates `branch.createElement(name, Button.TAG)`
//   on a branch it owns and hands the element in; the button dresses it.
//     new Button(el, { label, kind?, onClick? })
//       .el              the element it was given
//       .setOn(on)       a button switched off is inert and says so
//       .label(text)     the label, read or set
//
//   Card — a BRANCH component: it takes a sub-branch the caller made for it
//   and mints its own tree on it. One card per branch; two cards on a page
//   are two sub-branches.
//     new Card(branch, { title, text?, badge?, link? })    link: { to, label? }
//       .root            the element the caller appends
//
// A button is a control that is primary (kind "primary", the default) or
// plain. Its type is "button" unless the caller says otherwise afterwards,
// so a button inside a form does not submit it by accident. A card is a
// raised box: a heading with an optional badge, a line of text, and a link
// pushed to the bottom, its address set through the manager so a typed
// address stays typed.
//
// `css` is injected with the styles import; the manager comes in by its
// explicit HrefManager import.
// =============================================================================

const _cardOwner = Object.freeze({ toString: () => "card" });

class Button {
    static TAG = "button";

    constructor(el, props) {
        if (!el) throw new Error("[Button] the element is required: mint it with Button.TAG on your branch");
        var p = props || {};
        this.el = el;
        el.type = "button";
        css.addClass(el, el_button);
        css.addClass(el, p.kind === "plain" ? el_button_plain : el_button_primary);
        el.textContent = p.label == null ? "" : String(p.label);
        if (typeof p.onClick === "function") el.addEventListener("click", p.onClick);
    }

    setOn(on) {
        this.el.disabled = !on;
        css.toggleClass(this.el, el_button_off, !on);
    }

    label(text) {
        if (text !== undefined) this.el.textContent = String(text);
        return this.el.textContent;
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
