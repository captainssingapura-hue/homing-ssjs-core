package hue.captains.singapura.js.homing.site.demo.hello;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Html;

/**
 * The demo's page shell: a document with a title, the same three links on
 * every page, and the body the page supplies. The base has no opinion on
 * what a page looks like — this is the site's own furniture, kept in one
 * place so the three pages are only their bodies.
 */
final class Shell {

    private Shell() {}

    static HtmlPageContent page(String title, String body) {
        return new HtmlPageContent("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>%s · hello</title>
                    <style>
                        body { font: 16px/1.5 Georgia, serif; max-width: 40rem; margin: 3rem auto; padding: 0 1rem; }
                        nav a { margin-right: 1rem; }
                        code { font: 0.9em ui-monospace, monospace; }
                    </style>
                </head>
                <body>
                    <nav><a href="/">home</a><a href="/about">about</a><a href="/greet?name=Ada">greet</a></nav>
                    %s
                </body>
                </html>
                """.formatted(Html.escape(title), body));
    }
}
