// =============================================================================
// MpaChrome — the page chrome: a bar with the brand, the trail and the
// preferences button, over the slot the app is mounted in. A branch
// component: the scaffold makes a branch for it and hands it in.
//
//   new MpaChrome(branch, root, chrome)
//     .main    the slot element the app is mounted in
//
// `chrome` is what the server stamped into the page, frozen:
//   { brand: { label, home }, crumbs: [ { text, to } ], preferences: { module } }
// The crumbs are the trail the router wrote down; the last one is this
// page and carries no link. No crumbs means the address said nothing about
// position, and the bar shows the brand alone.
//
// Everything the bar draws is on the chrome's branch; the app gets its own
// slot and never touches the bar. `css` is injected with the styles import;
// the href manager comes in by its explicit import.
// =============================================================================

const _chromeOwner = Object.freeze({ toString: () => "mpaChrome" });

class MpaChrome {
    constructor(branch, root, chrome) {
        if (!branch) throw new Error("[MpaChrome] a branch of its own is required");
        branch.activate(_chromeOwner);

        var column = branch.createElement("column", "div");
        css.addClass(column, mpa_root);

        var bar = branch.createElement("bar", "header");
        css.addClass(bar, mpa_header);
        bar.appendChild(MpaChrome._brand(branch, chrome && chrome.brand));
        bar.appendChild(MpaChrome._crumbs(branch, chrome && chrome.crumbs));
        this.preferences = new PreferencesButton(branch.createBranch("prefs"), bar, chrome);
        column.appendChild(bar);

        var main = branch.createElement("main", "main");
        css.addClass(main, mpa_main);
        column.appendChild(main);

        root.replaceChildren(column);
        this.el = column;
        this.main = main;
    }

    static _brand(branch, brand) {
        var a = branch.createElement("brand", "a");
        css.addClass(a, mpa_brand);
        HrefManagerInstance.set(a, (brand && brand.home) || "/");
        var mark = branch.createElement("brandMark", "span");
        css.addClass(mark, mpa_brand_mark);
        var word = branch.createElement("brandWord", "span");
        css.addClass(word, mpa_brand_word);
        word.textContent = (brand && brand.label) || "site";
        a.appendChild(mark);
        a.appendChild(word);
        return a;
    }

    static _crumbs(branch, crumbs) {
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
            else HrefManagerInstance.set(el, crumbs[i].to);
            nav.appendChild(el);
        }
        return nav;
    }
}
