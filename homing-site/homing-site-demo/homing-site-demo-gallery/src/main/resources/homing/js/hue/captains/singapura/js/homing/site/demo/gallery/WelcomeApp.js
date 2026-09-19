// =============================================================================
// WelcomeApp — the gallery's front page. appMain(el) draws it into the slot
// the chrome handed over; the theme line is re-read whenever the theme
// changes, which is how a page sees a pick made on the bar.
// =============================================================================

var href = HrefManagerInstance;

const _owner = Object.freeze({ toString: () => "welcome" });

var PAGES = [
    { title: "Counter", text: "A JS app with typed params. The site's router binds the start off the path and tells the page where it is.", link: "/counter/7" },
    { title: "Counter, from zero", text: "The same app, bound with nothing: the address /counter and a start of 0.", link: "/counter" },
    { title: "A plain page", text: "Not under the chrome at all: the router serves a string, and the MPA is nowhere in it.", link: "/plain" },
    { title: "The flat address", text: "The framework's permalink for a JS app, /app?app=welcome, served by the MPA's own route.", link: "/app?app=welcome" }
];

function appMain(el) {
    var branch = domOpsParty.createBranch("welcome");
    branch.activate(_owner);

    var kicker = branch.createElement("kicker", "div");
    css.addClass(kicker, ga_kicker);
    kicker.textContent = "homing-site-mpa";
    el.appendChild(kicker);

    var title = branch.createElement("title", "h1");
    css.addClass(title, ga_title);
    title.textContent = "Gallery";
    el.appendChild(title);

    var lede = branch.createElement("lede", "p");
    css.addClass(lede, ga_lede);
    el.appendChild(lede);
    function wearing() {
        lede.textContent = "Two JS apps as pages under the standard chrome, wearing "
            + (css.theme() || "the default") + ". The bar, the trail and the theme menu are the MPA's; "
            + "this page is an AppModule mounted in the slot it was given. Pick another theme on the bar and "
            + "every sheet on the page follows.";
    }
    wearing();
    css.onThemeApplied(wearing);

    var cards = branch.createElement("cards", "div");
    css.addClass(cards, ga_cards);
    for (var i = 0; i < PAGES.length; i++) {
        var card = branch.createElement("card-" + i, "div");
        css.addClass(card, ga_card);
        var h = branch.createElement("cardTitle-" + i, "h2");
        css.addClass(h, ga_card_title);
        h.textContent = PAGES[i].title;
        var p = branch.createElement("cardText-" + i, "p");
        css.addClass(p, ga_card_text);
        p.textContent = PAGES[i].text;
        var a = branch.createElement("cardLink-" + i, "a");
        css.addClass(a, ga_link);
        href.set(a, PAGES[i].link);
        a.textContent = PAGES[i].link;
        card.appendChild(h);
        card.appendChild(p);
        card.appendChild(a);
        cards.appendChild(card);
    }
    el.appendChild(cards);
}
