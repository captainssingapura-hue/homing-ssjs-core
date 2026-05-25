package hue.captains.singapura.js.homing.workspace;

import java.util.List;
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
public sealed interface RibbonItem
        permits RibbonItem.Button, RibbonItem.Separator, RibbonItem.Label, RibbonItem.Choice {

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

    /**
     * A labelled dropdown selector — label text plus a list of options.
     * Change events emit {@code onAction(actionId, value)} where
     * {@code value} is the selected option's {@link ChoiceOption#value()}.
     *
     * <p>Typed extension to the sealed family for workspace-level controls
     * that need explicit-value selection (e.g. the AnimalsPlayground's
     * global Animal selector). Per the Composable Chrome doctrine: open
     * set of items in any workspace, closed set of shapes at the framework
     * — adding a new shape grows the framework's grammar deliberately, not
     * the individual workspace's escape hatches.</p>
     *
     * @since RFC 0028 cycle 4 — first downstream Party (AnimalsPlayground)
     */
    record Choice(String label, List<ChoiceOption> options, String actionId) implements RibbonItem {
        public Choice {
            Objects.requireNonNull(label,    "RibbonItem.Choice.label");
            Objects.requireNonNull(options,  "RibbonItem.Choice.options");
            Objects.requireNonNull(actionId, "RibbonItem.Choice.actionId");
            if (actionId.isBlank()) {
                throw new IllegalArgumentException(
                        "RibbonItem.Choice.actionId must not be blank — chrome dispatches by id");
            }
            if (options.isEmpty()) {
                throw new IllegalArgumentException(
                        "RibbonItem.Choice.options must not be empty — selector needs at least one choice");
            }
            options = List.copyOf(options);
        }
    }

    /**
     * One option in a {@link Choice} — display label plus the wire value
     * the chrome dispatches when the user picks it. The value is a String
     * because that's what the HTML {@code <select>} element carries; the
     * workspace's onAction handler interprets it.
     */
    record ChoiceOption(String label, String value) {
        public ChoiceOption {
            Objects.requireNonNull(label, "ChoiceOption.label");
            Objects.requireNonNull(value, "ChoiceOption.value");
            if (label.isBlank()) {
                throw new IllegalArgumentException("ChoiceOption.label must not be blank");
            }
        }
    }
}
