package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;

/**
 * Workspace chrome state — small, framework-wide settings that aren't
 * owned by any single widget or pane. Today: active {@link ThemeName} +
 * fullscreen mode. The shape is expected to grow; additions are
 * schema-versioned at the {@link WorkspaceState} envelope layer.
 *
 * <p>Chrome state is captured and restored as a unit. RFC 0028 Party
 * snapshots are <b>not</b> stored here by default — Party state is
 * re-derived from widget state on restore (the {@code re-derive-by-default}
 * clause of RFC 0029). Parties that need explicit serialization opt in
 * via a future {@code partySnapshots} field on this record.</p>
 *
 * @param theme      active theme — typed registry key
 * @param fullscreen workspace-level fullscreen mode flag
 * @since RFC 0029 cycle 1
 */
public record ChromeState(ThemeName theme, boolean fullscreen) {

    public ChromeState {
        Objects.requireNonNull(theme, "ChromeState.theme");
    }

    /** Convenience — default theme, fullscreen off (the boot-time defaults). */
    public static ChromeState defaults() {
        return new ChromeState(ThemeName.DEFAULT, false);
    }
}
