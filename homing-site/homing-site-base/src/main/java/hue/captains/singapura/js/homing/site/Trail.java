package hue.captains.singapura.js.homing.site;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Where a page is: the crumbs from the root to it, each a name and the
 * address it is served at. The last crumb is the page itself.
 *
 * <p>A trail is read off the path, never looked up: the router that resolved
 * the address already walked every step of it, and the trail is that walk
 * written down. So a trail cannot disagree with the address bar, and a page
 * reached some other way — a flat address, a link from outside — has
 * {@link #NONE}, which is the honest answer to "where am I" when the address
 * did not say.</p>
 *
 * @param crumbs root first, the page last; empty when the page has no known position
 */
public record Trail(List<Crumb> crumbs) {

    /** The address said nothing about position. */
    public static final Trail NONE = new Trail(List.of());

    /** One step of a trail. */
    public record Crumb(String text, String href) {
        public Crumb {
            if (text == null || text.isBlank()) throw new IllegalArgumentException("Crumb.text must not be blank");
            Objects.requireNonNull(href, "Crumb.href");
        }
    }

    public Trail {
        Objects.requireNonNull(crumbs, "Trail.crumbs");
        crumbs = List.copyOf(crumbs);
    }

    public boolean isEmpty() { return crumbs.isEmpty(); }
    public int     depth()   { return crumbs.size(); }

    /** This trail one crumb longer. */
    public Trail then(String text, String href) {
        var out = new ArrayList<>(crumbs);
        out.add(new Crumb(text, href));
        return new Trail(out);
    }

    /** The page's own crumb, when there is one. */
    public Crumb last() {
        if (crumbs.isEmpty()) throw new IllegalStateException("Trail.NONE has no last crumb");
        return crumbs.get(crumbs.size() - 1);
    }
}
