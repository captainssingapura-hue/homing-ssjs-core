package hue.captains.singapura.js.homing.site.catalogue;

import java.util.Objects;

/**
 * A whole tree placed in a catalogue: another tree's root, grafted, so that
 * its vertices and its pages take their positions under the host - one
 * position each, in the composed tree, as any vertex and page has.
 *
 * <p>The graft is the placement, not the tree, as a {@link Leaf} is the
 * placement and not the page. Only the host declares the edge: the grafted
 * root is a root - it names no parent - and so knows nothing of where it
 * sits, which is what lets one tree be written on its own, by a module that
 * knows no site, and grafted wherever a site wants it. The root keeps its
 * own slug; what a listing shows for it at the graft - name, summary, badge,
 * icon - is its own unless the host restates it here.</p>
 *
 * @param <C>     the host catalogue's type, so a graft is typed to where it is declared
 * @param root    the root of the tree grafted
 * @param name    the name shown for it here; its own when not restated
 * @param summary one line under the name; its own when not restated
 * @param badge   a short upper-case tag; its own when not restated
 * @param icon    an emoji or glyph; its own when not restated
 */
public record Graft<C extends Catalogue<C>>(L0_Catalogue<?> root, String name, String summary, String badge, String icon) {

    public Graft {
        Objects.requireNonNull(root, "Graft.root");
        if (name == null || name.isBlank()) name = root.name();
        if (summary == null) summary = root.summary();
        if (badge == null || badge.isBlank()) badge = root.badge();
        if (icon == null) icon = root.icon();
    }

    /**
     * The tree under {@code root}, grafted into {@code host}, shown as it shows
     * itself. The host is named only to type the graft to it - pass
     * {@code this} from inside the catalogue's {@code grafts()}.
     */
    public static <C extends Catalogue<C>> Graft<C> of(C host, L0_Catalogue<?> root) {
        return new Graft<>(root, null, null, null, null);
    }

    /** This graft, shown here under another name and summary. */
    public Graft<C> shownAs(String name, String summary) { return new Graft<>(root, name, summary, badge, icon); }

    public Graft<C> badge(String badge) { return new Graft<>(root, name, summary, badge, icon); }
    public Graft<C> icon(String icon)   { return new Graft<>(root, name, summary, badge, icon); }
}
