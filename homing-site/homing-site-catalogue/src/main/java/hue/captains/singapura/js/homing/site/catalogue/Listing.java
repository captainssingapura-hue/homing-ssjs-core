package hue.captains.singapura.js.homing.site.catalogue;

/**
 * How a vertex is shown: the page for a catalogue, given the router that
 * knows its children and their addresses.
 *
 * <p>The tree says what is under a vertex; the listing says what a visitor
 * sees there. {@link HtmlListing} is the one the router uses unless told
 * otherwise — a document with the trail, the name, the sub-catalogues and
 * the pages, each a link — and a site that wants its vertices shown some
 * other way, a JS app behind the server's scaffold say, supplies its own.</p>
 */
@FunctionalInterface
public interface Listing {

    /** The page for {@code catalogue}. It is placed, so the router can hand it its trail. */
    Placed pageFor(Catalogue<?> catalogue, CatalogueRouter router);
}
