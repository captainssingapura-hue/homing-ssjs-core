package hue.captains.singapura.js.homing.design;

/**
 * The user-agent conditions a design may bind differently under. A mode is
 * orthogonal to a design and to a state: a binding is per (class, property,
 * state, mode), and {@link #LIGHT} is the unconditional base every other
 * mode falls back to. Modes are a closed set because each is a media
 * query the framework emits; a design cannot invent one.
 */
public enum Mode {
    /** The base; no media query. */
    LIGHT(""),
    DARK("(prefers-color-scheme: dark)"),
    HIGH_CONTRAST("(prefers-contrast: more)"),
    REDUCED_MOTION("(prefers-reduced-motion: reduce)");

    private final String media;

    Mode(String media) { this.media = media; }

    /** The media query the mode's bindings are wrapped in; empty for {@link #LIGHT}. */
    public String media() { return media; }
}
