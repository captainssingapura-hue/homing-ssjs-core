// =============================================================================
// MpaChrome — the page chrome: a bar with the brand, the trail and the
// preferences button, over the slot the app is mounted in.
//
// mountChrome(root, chrome) → the slot element
//
// `chrome` is what the server stamped into the page, frozen:
//   { brand: { label, href }, crumbs: [ { text, href } ] }
// The crumbs are the trail the router wrote down; the last one is this
// page and carries no link. No crumbs means the address said nothing about
// position, and the bar shows the brand alone.
//
// Everything the bar draws is on one branch, owned by the chrome; the app
// gets its own slot and never touches the bar. `css` is injected with the
// styles import; `href` is aliased from the explicit HrefManager import.
// =============================================================================

var href = HrefManagerInstance;

const _chromeOwner = Object.freeze({ toString: () => "mpaChrome" });

function mountChrome(root, chrome) {
    var branch = domOpsParty.createBranch("mpaChrome");
    branch.activate(_chromeOwner);

    var column = branch.createElement("column", "div");
    css.addClass(column, mpa_root);

    var bar = branch.createElement("bar", "header");
    css.addClass(bar, mpa_header);
    bar.appendChild(_brand(branch, chrome && chrome.brand));
    bar.appendChild(_crumbs(branch, chrome && chrome.crumbs));
    mountPreferences(branch, bar);
    column.appendChild(bar);

    var main = branch.createElement("main", "main");
    css.addClass(main, mpa_main);
    column.appendChild(main);

    root.replaceChildren(column);
    return main;
}

function _brand(branch, brand) {
    var a = branch.createElement("brand", "a");
    css.addClass(a, mpa_brand);
    href.set(a, (brand && brand.href) || "/");
    var mark = branch.createElement("brandMark", "span");
    css.addClass(mark, mpa_brand_mark);
    var word = branch.createElement("brandWord", "span");
    css.addClass(word, mpa_brand_word);
    word.textContent = (brand && brand.label) || "site";
    a.appendChild(mark);
    a.appendChild(word);
    return a;
}

function _crumbs(branch, crumbs) {
    var nav = branch.createElement("crumbs", "nav");
    css.addClass(nav, mpa_crumbs);
    nav.setAttribute("aria-label", "Breadcrumb");
    if (!crumbs || !crumbs.length) return nav;
    for (var i = 0; i < crumbs.length; i++) {
        if (i > 0) {
            var sep = branch.createElement("sep-" + i, "span");
            css.addClass(sep, mpa_crumb_sep);
            sep.textContent = "/";
            sep.setAttribute("aria-hidden", "true");
            nav.appendChild(sep);
        }
        var last = i === crumbs.length - 1;
        var el = branch.createElement("crumb-" + i, last ? "span" : "a");
        css.addClass(el, mpa_crumb);
        el.textContent = crumbs[i].text;
        if (last) el.setAttribute("aria-current", "page");
        else href.set(el, crumbs[i].href);
        nav.appendChild(el);
    }
    return nav;
}
