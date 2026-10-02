package hue.captains.singapura.js.homing.site.demo.library;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Html;
import hue.captains.singapura.js.homing.site.Trail;

/** The demo's page shell and its crumb bar, for the pages that are not listings. */
final class Shell {

    private Shell() {}

    static HtmlPageContent page(String title, String body) {
        return new HtmlPageContent("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>%s · library</title>
                    <style>
                        body { font: 16px/1.5 Georgia, serif; max-width: 40rem; margin: 2.5rem auto; padding: 0 1rem; }
                        nav.trail a { text-decoration: none; }
                        nav.trail span { opacity: .55; margin: 0 .4em; }
                        .meta { opacity: .7; font-size: .9em; }
                    </style>
                </head>
                <body>
                %s</body>
                </html>
                """.formatted(Html.escape(title), body));
    }

    static String crumbs(Trail trail) {
        if (trail.isEmpty()) return "";
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
}
