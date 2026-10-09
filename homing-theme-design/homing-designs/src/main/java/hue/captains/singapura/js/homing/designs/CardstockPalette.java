package hue.captains.singapura.js.homing.designs;

/**
 * Cardstock — warm off-white paper: a ground, a lighter sheet lying on it,
 * and a sunk face for what is cut into it. Cut for flat-morphism, which cast
 * a hard shadow in a chosen colour; that design is retired, a concept more
 * than a style, and the paper is kept as colours alone.
 *
 * <p>The thirteen seeds are {@link SeedPalette#CARDSTOCK}; these are the
 * values it seeds them from.</p>
 */
final class CardstockPalette {

    private CardstockPalette() {}

    /** The ground: warm off-white, the colour of the table. */
    static final String GROUND   = "#EDEAE4";
    static final String GROUND_D = "#22242A";

    /** A sheet lying on the ground. */
    static final String SHEET    = "#FBFAF7";
    static final String SHEET_D  = "#2E3138";

    /** What is cut INTO the ground: a hole casts nothing, it is only darker. */
    static final String SUNK     = "#E2DED5";
    static final String SUNK_D   = "#1B1D22";

    static final String INK      = "#17181A";
    static final String INK_D    = "#D3D6D1";
    static final String INK_2    = "#63665F";
    static final String INK_2_D  = "#8E928D";
    static final String HAIR     = "#DBD6CB";
    static final String HAIR_D   = "#3D414A";
    static final String ACCENT   = "#1F46C8";
    static final String ACCENT_D = "#7E9FE8";
    static final String ACCENT_PRESS   = "#173AA6";
    static final String ACCENT_PRESS_D = "#94B0EC";
    static final String ON_ACCENT   = "#FBFAF7";
    static final String ON_ACCENT_D = "#1A1C21";
}
