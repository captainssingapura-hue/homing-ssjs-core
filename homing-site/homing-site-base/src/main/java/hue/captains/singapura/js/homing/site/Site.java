package hue.captains.singapura.js.homing.site;

/**
 * RFC 0066 Episode 3 — a site: a {@link Router} with a name.
 *
 * <p>This is the unit a {@link SiteHost} serves, and it is deliberately no
 * more than that. A site that serves JS modules, wears a design or offers a
 * theme picker declares those through the contracts that arrive for them;
 * the base does not presuppose any of it, so the smallest site is a router
 * over a few pages of HTML and needs nothing else.</p>
 */
public interface Site {

    /** The site's name, for the host's log line and whatever else wants one. */
    String name();

    /** The site's one answer to every path. */
    Router router();
}
