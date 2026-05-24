package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.SvgRef;

import java.util.Objects;

/**
 * Typed icon for a {@link WidgetEntry}'s picker tile. Sealed for
 * exhaustive switch in JS code-gen: every variant must be emitted
 * by the framework into the workspace's bundled registry JSON.
 *
 * <p>Two variants today:</p>
 *
 * <ul>
 *   <li>{@link Emoji} — short glyph string, rendered directly as text.
 *       Lightweight; no extra resources; relies on the user's emoji
 *       font / vendor renderer. Default for casual studios.</li>
 *   <li>{@link Svg} — typed {@link SvgRef} to an SvgGroup entry. RFC
 *       0017 themed: the icon inherits the active theme's accent
 *       colour via {@code currentColor}, the rendering is sharp at
 *       any size, and the icon is addressable as its own SvgDoc. The
 *       right choice for studios with visual identity needs.</li>
 * </ul>
 *
 * @since RFC 0025 Ext1a — Mechanism 1 (Widget Type Registry)
 */
public sealed interface WidgetIcon permits WidgetIcon.Emoji, WidgetIcon.Svg {

    /** Short emoji glyph rendered as text. */
    record Emoji(String glyph) implements WidgetIcon {
        public Emoji {
            Objects.requireNonNull(glyph, "WidgetIcon.Emoji.glyph");
            if (glyph.isBlank()) {
                throw new IllegalArgumentException(
                        "WidgetIcon.Emoji.glyph must not be blank");
            }
        }

        /** Default tile icon when a studio doesn't specify one. */
        public static final Emoji DEFAULT = new Emoji("📦");
    }

    /** Typed SvgRef rendered as inline themed SVG. */
    record Svg(SvgRef<?> ref) implements WidgetIcon {
        public Svg {
            Objects.requireNonNull(ref, "WidgetIcon.Svg.ref");
        }
    }
}
