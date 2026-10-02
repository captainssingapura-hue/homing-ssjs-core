package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.Objects;

/**
 * A page placed in a catalogue: a slug, what a listing shows for it, how it
 * opens, and the {@link Navigable} that is the page.
 *
 * <p>The leaf is the placement, not the page. The navigable knows nothing of
 * where it sits (that is the site contract), so the slug, name, summary,
 * badge and icon live here, on the placement, and a {@link CatalogueTree}
 * holds each navigable to one placement — which is what makes a page's path
 * a fact rather than a choice.</p>
 *
 * <p>How it opens is the app's to say, since the app knows what its page is: a
 * document is read in place, where the listing was; a workspace is a place of
 * work of its own, and opens beside it. The app's leaf factory states it; a
 * catalogue, and a page no app says otherwise of, opens in place. What a host
 * does with it - a site navigates, a workspace opens a tab - is the host's.</p>
 *
 * @param <C>     the host catalogue's type, so a leaf is typed to where it is declared
 * @param slug    the path segment under the host
 * @param name    the display name
 * @param summary one line under the name, or empty
 * @param badge   a short upper-case tag; {@code PAGE} by default
 * @param icon    an emoji or glyph, or empty
 * @param opens   how the page opens when asked for from a listing; {@link Opening#IN_PLACE} by default
 * @param page    the navigable this leaf places
 */
public record Leaf<C extends Catalogue<C>>(
        NodeName slug, String name, String summary, String badge, String icon, Opening opens, Navigable page) {

    /** How a page opens when a person asks for it from a listing: the app that makes the page says. */
    public enum Opening {
        /** Where the listing was: a document, a catalogue. */
        IN_PLACE,
        /** Beside it, the listing kept: a place of work of its own, such as a workspace. */
        NEW_TAB
    }

    public Leaf {
        Objects.requireNonNull(slug, "Leaf.slug");
        Objects.requireNonNull(page, "Leaf.page");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Leaf.name must not be blank");
        if (summary == null) summary = "";
        if (badge == null || badge.isBlank()) badge = "PAGE";
        if (icon == null) icon = "";
        if (opens == null) opens = Opening.IN_PLACE;
    }

    /**
     * A leaf in {@code host} with its slug derived from the name. The host is
     * named only to type the leaf to it — pass {@code this} from inside the
     * catalogue's {@code leaves()}.
     */
    public static <C extends Catalogue<C>> Leaf<C> of(C host, String name, String summary, Navigable page) {
        return new Leaf<>(NodeName.conciseSlug(name), name, summary, null, null, null, page);
    }

    /** A leaf in {@code host} with the slug stated. */
    public static <C extends Catalogue<C>> Leaf<C> of(C host, NodeName slug, String name, String summary,
                                                      Navigable page) {
        return new Leaf<>(slug, name, summary, null, null, null, page);
    }

    public Leaf<C> badge(String badge) { return new Leaf<>(slug, name, summary, badge, icon, opens, page); }
    public Leaf<C> icon(String icon)   { return new Leaf<>(slug, name, summary, badge, icon, opens, page); }

    /** This leaf, opening as its app says. */
    public Leaf<C> opens(Opening opens) { return new Leaf<>(slug, name, summary, badge, icon, opens, page); }
}
