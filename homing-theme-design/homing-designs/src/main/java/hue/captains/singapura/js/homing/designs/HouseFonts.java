package hue.captains.singapura.js.homing.designs;

/**
 * The house stacks for the three faces: what {@link EditorialDesign} sets its
 * type in, and what every design with no typographic identity of its own
 * falls back to through it. Plain font stacks, owned by the designs; the
 * studio's legacy type palette provides the same stacks by these constants.
 */
public final class HouseFonts {
    public static final String BODY    = "\"Calibri\", \"Segoe UI\", system-ui, sans-serif";
    public static final String DISPLAY = "\"Georgia\", serif";
    public static final String MONO    = "\"Consolas\", \"Courier New\", monospace";

    private HouseFonts() {}
}
