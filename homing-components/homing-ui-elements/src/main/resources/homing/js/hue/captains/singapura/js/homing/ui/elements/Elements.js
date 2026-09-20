// =============================================================================
// Elements — a button and a card, as builders.
//
//   Button(branch, name, { label, kind?, onClick? })            → <button>
//   setButtonOn(button, on)                                      a button switched off is inert and says so
//   Card(branch, name, { title, text?, badge?, link? })         → <div>
//       link: { to, label? }
//
// `branch` is the DomOpsParty branch that will own the element; `name` is
// the element's name on it, and the card's parts are named under it
// (name-title, name-text, …), so two cards on one branch need two names.
// Each builder returns one node and places nothing: the caller appends it.
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

function Button(branch, name, props) {
    var p = props || {};
    var btn = branch.createElement(name, "button");
    btn.type = "button";
    css.addClass(btn, el_button);
    css.addClass(btn, p.kind === "plain" ? el_button_plain : el_button_primary);
    btn.textContent = p.label == null ? "" : String(p.label);
    if (typeof p.onClick === "function") btn.addEventListener("click", p.onClick);
    return btn;
}

function setButtonOn(btn, on) {
    btn.disabled = !on;
    css.toggleClass(btn, el_button_off, !on);
}

function Card(branch, name, props) {
    var p = props || {};
    var card = branch.createElement(name, "div");
    css.addClass(card, el_card);

    var title = branch.createElement(name + "-title", "h2");
    css.addClass(title, el_card_title);
    var titleText = branch.createElement(name + "-title-text", "span");
    titleText.textContent = p.title == null ? "" : String(p.title);
    title.appendChild(titleText);
    if (p.badge) {
        var badge = branch.createElement(name + "-badge", "span");
        css.addClass(badge, el_badge);
        badge.textContent = String(p.badge);
        title.appendChild(badge);
    }
    card.appendChild(title);

    if (p.text) {
        var text = branch.createElement(name + "-text", "p");
        css.addClass(text, el_card_text);
        text.textContent = String(p.text);
        card.appendChild(text);
    }

    if (p.link && p.link.to) {
        var a = branch.createElement(name + "-link", "a");
        css.addClass(a, el_card_link);
        HrefManagerInstance.set(a, p.link.to);
        a.textContent = p.link.label == null ? String(p.link.to) : String(p.link.label);
        card.appendChild(a);
    }
    return card;
}
