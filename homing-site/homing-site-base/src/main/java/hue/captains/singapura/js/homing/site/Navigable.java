package hue.captains.singapura.js.homing.site;

import hue.captains.singapura.js.homing.server.HtmlPageContent;

/**
 * RFC 0066 Episode 3 — a destination. The whole public contract of one:
 * given the query it was reached with, possibly none, the page.
 *
 * <p>That is all a client can ask of a place on a site, and so it is all the
 * contract says. How the page comes to exist — a JS app started by the
 * server's scaffold, a template filled on the way out, a string held since
 * boot — is the navigable's own business, and a navigable that is a JS app
 * looks no different from here than one that is not. The studio's typed
 * {@code Navigable<P, M>}, an app bound to its params, is one way of being
 * this: the binding names what to render, and the scaffold renders it.</p>
 *
 * <p>Where a navigable sits is not its business either. A {@link Router}
 * hands one out for a path; the same navigable may be handed out for two
 * paths, or for none and reached some other way, and it cannot tell. Params
 * a router reads off the path are bound before the navigable is returned;
 * what arrives here is only the query, the part of the address the router
 * did not consume.</p>
 */
@FunctionalInterface
public interface Navigable {

    /** The page for this query. Never null: a navigable with nothing to show is not one. */
    HtmlPageContent html(Query query);
}
