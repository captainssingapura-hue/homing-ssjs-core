package hue.captains.singapura.js.homing.workspace;

import java.util.Objects;

/**
 * Typed element in a workspace's Footer. Sealed for exhaustive switches
 * in JS code-gen. Workspaces declare their footer contents by
 * overriding {@link WorkspaceShell#footerItems()} — an empty list
 * suppresses the footer entirely (the DOM slot is not rendered).
 *
 * <p>Distinct from {@link RibbonItem} because footers and ribbons
 * have different conventions — footers carry passive status text +
 * separators, ribbons carry active controls. Two sealed hierarchies
 * keep the JS dispatch simple even if a variant happens to look the
 * same in V0.</p>
 *
 * @since RFC 0025 Ext1b b.2e — workspace chrome (Ribbon + Footer + fullscreen)
 */
public sealed interface FooterItem permits FooterItem.Label, FooterItem.Separator, FooterItem.Button {

    /** Status text — passive, no interaction. */
    record Label(String text) implements FooterItem {
        public Label {
            Objects.requireNonNull(text, "FooterItem.Label.text");
        }
    }

    /** Visual divider between footer groups. */
    record Separator() implements FooterItem {}

    /**
     * A clickable footer button — same shape as {@link RibbonItem.Button}.
     * Footers occasionally carry small affordances (zoom in/out, mode
     * toggle); typed separately so the JS render branch is clear.
     */
    record Button(WidgetIcon icon, String tooltip, String actionId) implements FooterItem {
        public Button {
            Objects.requireNonNull(icon,     "FooterItem.Button.icon");
            Objects.requireNonNull(tooltip,  "FooterItem.Button.tooltip");
            Objects.requireNonNull(actionId, "FooterItem.Button.actionId");
            if (actionId.isBlank()) {
                throw new IllegalArgumentException(
                        "FooterItem.Button.actionId must not be blank — chrome dispatches by id");
            }
        }
    }
}
