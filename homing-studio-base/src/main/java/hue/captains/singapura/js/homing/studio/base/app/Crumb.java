package hue.captains.singapura.js.homing.studio.base.app;

/**
 * One element of a breadcrumb trail — pre-formatted text and href, ready
 * for inclusion in a JSON response or a Header() crumbs array.
 *
 * <p>Used by RFC 0016's tree-leaf doc breadcrumbs, where the trail mixes
 * catalogue crumbs (e.g., "Demo Studio", URL = {@code /app?app=...&id=...})
 * with tree-internal crumbs (e.g., "🌳 Animals & Halloween", URL =
 * {@code /app?app=tree&id=animals}). A single homogeneous record covers
 * both kinds so downstream serializers don't need to dispatch on origin.</p>
 *
 * @param text the visible crumb label (may include an icon prefix)
 * @param href the navigation target; {@code ""} for the current page
 * @since RFC 0016 → tree-breadcrumb bridge
 */
public record Crumb(String text, String href) {}
