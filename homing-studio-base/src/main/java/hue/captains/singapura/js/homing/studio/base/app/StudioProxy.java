package hue.captains.singapura.js.homing.studio.base.app;
import hue.captains.singapura.js.homing.tree.NodeName;


/**
 * Typed proxy for re-attaching a source {@link L0_Catalogue} as a leaf in
 * an umbrella catalogue tree (RFC 0011).
 *
 * <p>The proxy is <b>data</b>, not a {@link Catalogue} — its presence in
 * the umbrella tree as an {@link Entry.OfStudio} leaf is the structural
 * fact; {@link CatalogueRegistry} then uses it to augment breadcrumbs for
 * all pages whose source L0 root is wrapped by this proxy. The source
 * studio's own catalogue records are untouched.</p>
 *
 * <p>Type parameter {@code S} carries the wrapped L0's exact type at the
 * construction site, so authoring is type-checked end-to-end:
 * {@code new StudioProxy<>(SkillsHome.INSTANCE, …)} resolves to
 * {@code StudioProxy<SkillsHome>}. No {@code Class<?>} juggling at the
 * authoring boundary.</p>
 *
 * <p>Display fields ({@code name}, {@code summary}, {@code badge},
 * {@code icon}) are how the proxy renders on the parent catalogue's tile
 * grid and in breadcrumb crumbs. They may differ from the source L0's own
 * labels — the proxy is the source studio's <i>identity as seen from the
 * umbrella</i>, not a copy.</p>
 *
 * @param <S>     the wrapped L0_Catalogue type
 * @param source  the wrapped L0's INSTANCE (not just its class — typed reference)
 * @param name    display name on the parent catalogue's tile grid
 * @param summary one-line summary shown on the tile
 * @param badge   text label for the card's badge CSS class (RFC 0009)
 * @param icon    short glyph prepended into breadcrumb crumbs (RFC 0009)
 *
 * @since RFC 0011
 */
public record StudioProxy<S extends L0_Catalogue<S>>(
        S source,
        String name,
        String summary,
        String badge,
        String icon) implements CatalogueLeaf {

    /**
     * RFC 0051 Law 2. Like {@code AppDoc}, every proxy shares one class, so
     * the segment comes from the wrapped source's own slug — a studio keeps
     * the same path segment whether reached directly or through an umbrella,
     * which is the parity this proxy exists to preserve.
     */
    @Override public NodeName slug() {
        return source.slug();
    }

    public StudioProxy {
        if (source == null)         throw new IllegalArgumentException("StudioProxy.source must not be null");
        if (name == null || name.isBlank())   throw new IllegalArgumentException("StudioProxy.name must not be blank");
        if (summary == null) summary = "";
        if (badge == null || badge.isBlank()) badge = "STUDIO";
        if (icon == null)   icon = "";
    }
}
