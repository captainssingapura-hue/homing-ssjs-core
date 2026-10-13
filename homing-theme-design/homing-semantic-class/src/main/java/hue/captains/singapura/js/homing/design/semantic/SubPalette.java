package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.Palettable;

import java.util.Optional;

/**
 * An extension's word on the palettable colours it grafts (RFC 0067, appendix Semantic Colours and Flavours): a
 * stateless functional object scoped to its extension's branch - asked of those colours and no other. An extension
 * has one for each palette it has something to say for, and a base sub-palette, its last word.
 */
public interface SubPalette {

    /** The concrete colour it gives one of its extension's palettable colours, or none. */
    Optional<ConcreteColor> color(Palettable<?, ?> color);
}
