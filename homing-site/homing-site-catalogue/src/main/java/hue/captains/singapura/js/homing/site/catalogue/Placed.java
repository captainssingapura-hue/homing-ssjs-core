package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.Query;

/**
 * A {@link Navigable} that can be told where it is. The catalogue router
 * hands a placed page its {@link Trail} along with the query; every other
 * router, and the site contract itself, sees only {@link #html(Query)}, which
 * renders the page with no position — the same page, unplaced.
 *
 * <p>This is how a page draws a breadcrumb without knowing the tree: it does
 * not ask, it is told, and only by a router that read the answer off the
 * address. A plain navigable placed in a catalogue is served as it is; a
 * placed one additionally learns its trail. Nothing has to be wrapped to be
 * placed.</p>
 */
@FunctionalInterface
public interface Placed extends Navigable {

    /** The page, knowing its position; {@link Trail#NONE} when reached without one. */
    HtmlPageContent html(Trail trail, Query query);

    @Override
    default HtmlPageContent html(Query query) {
        return html(Trail.NONE, query);
    }
}
