package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorNode;

import java.util.List;
import java.util.Optional;

/**
 * What a module grafts onto a colour tree (RFC 0067, appendix Semantic Colours and Flavours): the branches and leaves
 * it needs, and, for the palettable ones, its sub-palettes - one for each palette it has something to say for, and a
 * base sub-palette that answers every palettable colour it grafts. Its nodes are in the tree if and only if the module
 * is on the classpath, and the tree's owner never learns of them. A style that needs a colour of its own grafts it the
 * same way.
 *
 * <p>Found on the classpath by {@link java.util.ServiceLoader}, as a taxonomy's extensions are: a module names its
 * extension in {@code META-INF/services/hue.captains.singapura.js.homing.design.semantic.ColorExtension}, and a public
 * record with no components is one the loader can make.</p>
 */
public interface ColorExtension {

    /** The colour tree this grafts onto, by its source's type. */
    Class<? extends ColorSource> extending();

    /** The branches and leaves it grafts, in the order it lists them. */
    List<ColorNode<?, ?, ?>> nodes();

    /** Its word on its own palettable colours under a palette, if it has one for that palette. */
    default Optional<SubPalette> subPalette(ColorPalette<?> palette) { return Optional.empty(); }

    /** Its last word on its own palettable colours, every one of them; needed once it grafts any. */
    default Optional<SubPalette> baseSubPalette() { return Optional.empty(); }
}
