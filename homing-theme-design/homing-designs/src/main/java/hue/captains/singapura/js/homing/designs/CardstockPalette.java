package hue.captains.singapura.js.homing.designs;

/**
 * Cardstock — the paper flat-morphism is cut from. A warm off-white ground,
 * a lighter sheet lying on it, a sunk face for what is cut into it, and the
 * one colour that matters here beside those: the CAST, which is what the
 * ground looks like in the shade of a sheet. No blur is ever used, so the
 * cast is a flat colour and has to be chosen rather than computed — a
 * touch darker than the ground and a touch warmer, the way paper shades
 * paper.
 *
 * <p>At night the room is dim and a cast can barely be seen, so the cast
 * goes nearly black and the sheet lightens instead: the depth crosses over
 * from what is beneath a sheet to the sheet itself. Same mechanic, same
 * numbers, different room.</p>
 *
 * <p>The thirteen seeds are {@link SeedPalette#CARDSTOCK}; these are the
 * three this design needs that a seed palette has no slot for, because no
 * other design casts a hard shadow in a chosen colour.</p>
 */
final class CardstockPalette {

    private CardstockPalette() {}

    /** The ground: warm off-white, the colour of the table. */
    static final String GROUND   = "#EDEAE4";
    static final String GROUND_D = "#22242A";

    /** A sheet lying on the ground. */
    static final String SHEET    = "#FBFAF7";
    static final String SHEET_D  = "#2E3138";

    /** A second sheet, for what rests on a sheet. */
    static final String SHEET_2  = "#F1EEE7";
    static final String SHEET_2_D = "#383B43";

    /** What is cut INTO the ground: a hole casts nothing, it is only darker. */
    static final String SUNK     = "#E2DED5";
    static final String SUNK_D   = "#1B1D22";

    /** The ground in the shade of a sheet: the cast. */
    static final String CAST     = "#C5C0B4";
    static final String CAST_D   = "#15171B";

    /** A sheet in the shade of another sheet, for stacking. */
    static final String CAST_SHEET   = "#D6D1C6";
    static final String CAST_SHEET_D = "#1F2127";

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
