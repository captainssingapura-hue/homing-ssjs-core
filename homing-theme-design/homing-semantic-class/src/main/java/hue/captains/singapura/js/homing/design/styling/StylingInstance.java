package hue.captains.singapura.js.homing.design.styling;

/**
 * An instance's styling, as the jar that assembles it names it (RFC 0067, appendix The Style Jars): what a build writes
 * the family files of ({@link WriteStyleFamilies}), the jar's class with a static {@code INSTANCE} implementing this.
 */
public interface StylingInstance {

    /** The instance's styling: its styles registered in their two stages, its palettes. */
    Styling styling();
}
