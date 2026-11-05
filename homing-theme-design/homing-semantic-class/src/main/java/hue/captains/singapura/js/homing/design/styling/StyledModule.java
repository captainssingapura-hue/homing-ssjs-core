package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.core.MadeByDesign;

import java.util.List;

/**
 * A module whose script is made from the styling of the site serving it: one empty value, the address its importers
 * name, handed the styling by whatever renders it ({@link StylingRenderer}).
 */
public interface StyledModule extends MadeByDesign {

    /** The module's script, without its imports and exports, as this styling makes it. */
    List<String> selfContent(Styling styling);
}
