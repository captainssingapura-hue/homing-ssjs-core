// =============================================================================
// WelcomeApp — the gallery's front page. appMain(el) draws it into the slot
// the chrome handed over; the theme line is re-read whenever the theme
// changes, which is how a page sees a pick made on the bar. The cards are
// the shared Card builder; this page owns the grid they sit in and nothing
// of what a card looks like.
// =============================================================================

const _owner = Object.freeze({ toString: () => "welcome" });

var PAGES = [
    { title: "Counter", badge: "JS", text: "A JS app with typed params. The site's router binds the start off the path and tells the page where it is.", link: "/counter/7" },
    { title: "Counter, from zero", badge: "JS", text: "The same app, bound with nothing: the address /counter and a start of 0.", link: "/counter" },
    { title: "A plain page", badge: "HTML", text: "Not under the chrome at all: the router serves a string, and the MPA is nowhere in it.", link: "/plain" },
    { title: "The flat address", badge: "PERMALINK", text: "The framework's permalink for a JS app, /app?app=welcome, served by the MPA's own route.", link: "/app?app=welcome" }
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
        cards.appendChild(Card(branch, "card-" + i, {
            title: PAGES[i].title,
            text:  PAGES[i].text,
            badge: PAGES[i].badge,
            link:  { href: PAGES[i].link }
        }));
    }
    el.appendChild(cards);
}
