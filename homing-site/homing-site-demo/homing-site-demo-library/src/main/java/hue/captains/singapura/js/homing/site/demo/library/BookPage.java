package hue.captains.singapura.js.homing.site.demo.library;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Html;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.site.catalogue.Placed;
import hue.captains.singapura.js.homing.site.catalogue.Trail;

/**
 * A book: a {@link Placed} page, so the router tells it where it is and it
 * draws the trail. Reached without a router it draws none — the page is
 * the same, it just has not been told.
 *
 * <p>The query is the page's own: {@code ?format=} picks how the details
 * are shown, and the router neither reads nor consumes it.</p>
 */
public record BookPage(String title, String author, int year, String blurb) implements Placed {

    @Override
    public HtmlPageContent html(Trail trail, Query query) {
        boolean brief = query.first("format").map("brief"::equals).orElse(false);
        var body = new StringBuilder();
        body.append(Shell.crumbs(trail));
        body.append("<h1>").append(Html.escape(title)).append("</h1>\n");
        body.append("<p class=\"meta\">").append(Html.escape(author)).append(", ").append(year).append("</p>\n");
        if (!brief) body.append("<p>").append(Html.escape(blurb)).append("</p>\n");
        body.append("<p class=\"meta\">")
            .append(trail.isEmpty() ? "Reached with no position: nobody told this page where it is."
                                    : "Position read off the address, " + trail.depth() + " crumbs deep.")
            .append(" <a href=\"?format=").append(brief ? "full" : "brief").append("\">")
            .append(brief ? "full" : "brief").append("</a></p>\n");
        return Shell.page(title, body.toString());
    }
}
