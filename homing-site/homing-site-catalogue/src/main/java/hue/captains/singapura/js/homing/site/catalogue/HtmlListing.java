package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.Placed;
import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Html;
import hue.captains.singapura.js.homing.site.Query;

/**
 * The listing a vertex gets unless the site says otherwise: one document,
 * server-rendered, with the trail, the catalogue's name and summary, its
 * sub-catalogues and its pages, every one a link the router minted.
 *
 * <p>Plain by design. It is the proof that a catalogue site needs no JS and
 * no theme to be a site, and it is what a visitor sees at every vertex until
 * a site gives its vertices a look of their own.</p>
 */
public final class HtmlListing implements Listing {

    public static final HtmlListing INSTANCE = new HtmlListing();

    private HtmlListing() {}

    @Override
    public Placed pageFor(Catalogue<?> c, CatalogueRouter router) {
        return (trail, query) -> render(c, router, trail);
    }

    private static HtmlPageContent render(Catalogue<?> c, CatalogueRouter router, Trail trail) {
        var tree = router.tree();
        var shown = tree.shownAs(c);
        var body = new StringBuilder();
        body.append(crumbs(trail));
        body.append("<h1>").append(iconAndName(shown.icon(), shown.name()))
            .append(" <small>").append(Html.escape(shown.badge())).append("</small></h1>\n");
        if (!shown.summary().isBlank()) body.append("<p class=\"summary\">").append(Html.escape(shown.summary())).append("</p>\n");

        // sub-catalogues, then grafted trees - each as it is shown here
        var subs = tree.childrenOf(c);
        if (!subs.isEmpty()) {
            body.append("<h2>Catalogues</h2>\n<ul class=\"entries\">\n");
            for (Catalogue<?> sub : subs) {
                var s = tree.shownAs(sub);
                body.append(entry(router.hrefOf(sub), s.icon(), s.name(), s.badge(), s.summary()));
            }
            body.append("</ul>\n");
        }
        var leaves = c.leaves();
        if (!leaves.isEmpty()) {
            body.append("<h2>Pages</h2>\n<ul class=\"entries\">\n");
            for (Leaf<?> leaf : leaves) {
                String href = router.hrefOf(leaf.page()).orElse("#");
                body.append(entry(href, leaf.icon(), leaf.name(), leaf.badge(), leaf.summary()));
            }
            body.append("</ul>\n");
        }
        if (subs.isEmpty() && leaves.isEmpty()) body.append("<p class=\"summary\">Nothing here yet.</p>\n");

        return new HtmlPageContent("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                    <style>
                        body { font: 16px/1.5 system-ui, sans-serif; max-width: 44rem; margin: 2.5rem auto; padding: 0 1rem; }
                        nav.trail a { text-decoration: none; }
                        nav.trail span { opacity: .55; margin: 0 .4em; }
                        h1 small { font-size: .5em; font-weight: 400; letter-spacing: .08em; opacity: .55; vertical-align: middle; }
                        .summary { opacity: .75; }
                        ul.entries { list-style: none; padding: 0; }
                        ul.entries li { padding: .5rem 0; border-top: 1px solid rgba(127,127,127,.25); }
                        ul.entries .badge { font-size: .7em; letter-spacing: .08em; opacity: .55; margin-left: .5em; }
                        ul.entries .sum { display: block; opacity: .75; font-size: .9em; }
                    </style>
                </head>
                <body>
                %s</body>
                </html>
                """.formatted(Html.escape(shown.name()), body));
    }

    private static String crumbs(Trail trail) {
        if (trail.depth() < 2) return "";
        var sb = new StringBuilder("<nav class=\"trail\">");
        var crumbs = trail.crumbs();
        for (int i = 0; i < crumbs.size(); i++) {
            var crumb = crumbs.get(i);
            if (i > 0) sb.append("<span>/</span>");
            if (i < crumbs.size() - 1) {
                sb.append("<a href=\"").append(Html.escape(crumb.href())).append("\">")
                  .append(Html.escape(crumb.text())).append("</a>");
            } else {
                sb.append(Html.escape(crumb.text()));
            }
        }
        return sb.append("</nav>\n").toString();
    }

    private static String iconAndName(String icon, String name) {
        return (icon.isBlank() ? "" : Html.escape(icon) + " ") + Html.escape(name);
    }

    private static String entry(String href, String icon, String name, String badge, String summary) {
        return "<li><a href=\"" + Html.escape(href) + "\">" + iconAndName(icon, name) + "</a>"
             + "<span class=\"badge\">" + Html.escape(badge) + "</span>"
             + (summary.isBlank() ? "" : "<span class=\"sum\">" + Html.escape(summary) + "</span>")
             + "</li>\n";
    }
}
