package hue.captains.singapura.js.homing.workspace;

import java.util.Objects;

/**
 * Typed element in a workspace's Ribbon (the row of action affordances
 * across the top of a {@link WorkspaceShell}). Sealed for exhaustive
 * switches in the JS code-gen — every variant earns its own render
 * branch.
 *
 * <p>Workspaces declare their ribbon contents by overriding {@link
 * WorkspaceShell#ribbonItems()}. The framework automatically prepends
 * the workspace's title (left edge) and appends a fullscreen toggle
 * (right edge); {@code ribbonItems()} supplies the workspace-specific
 * middle content.</p>
 *
 * <p>Forward-compatibility note: a future "multi-tabbed ribbon"
 * extension (Excel-style Home / Insert / Layout tabs) would compose
 * {@code RibbonItem} into a {@code RibbonTab} or {@code RibbonGroup}
 * containing a list. The current flat-list V0 is a strict subset of
 * that shape.</p>
 *
 * @since RFC 0025 Ext1b b.2e — workspace chrome (Ribbon + Footer + fullscreen)
 */
public sealed interface RibbonItem permits RibbonItem.Button, RibbonItem.Separator, RibbonItem.Label {

    /**
     * A clickable button — icon plus tooltip plus the action identifier
     * the workspace's chrome JS dispatches on click. Action handling is
     * workspace-defined in JS; the {@code actionId} is the wire key.
     */
    record Button(WidgetIcon icon, String tooltip, String actionId) implements RibbonItem {
        public Button {
            Objects.requireNonNull(icon,     "RibbonItem.Button.icon");
            Objects.requireNonNull(tooltip,  "RibbonItem.Button.tooltip");
            Objects.requireNonNull(actionId, "RibbonItem.Button.actionId");
            if (actionId.isBlank()) {
                throw new IllegalArgumentException(
                        "RibbonItem.Button.actionId must not be blank — chrome dispatches by id");
            }
        }
    }

    /** Visual divider between groups of ribbon items. */
    record Separator() implements RibbonItem {}

    /** Inline text label — workspace-specific status / annotation. */
    record Label(String text) implements RibbonItem {
        public Label {
            Objects.requireNonNull(text, "RibbonItem.Label.text");
        }
    }
}
