package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.List;

/**
 * A design style (RFC 0067): a typed identity - a stateless record, its type its identity - and
 * what it answers. A style never answers colour: that is its palette's. A style written over another
 * says so by type, with {@link Derives}; one that derives from nothing answers every root that is
 * not colour, so that every class resolves. A style also writes the words of a lexicon - the glyph it
 * draws for each ({@link Writing}) - the words its base writes its own unless it writes them otherwise.
 */
public interface DesignStyle {

    /** The style's token, from its type: {@code neo-brutalism}. */
    default String token() { return NodeName.ofType(getClass(), "").value(); }

    /** The classes this style answers itself. */
    List<Implementation<?>> implementations();

    /** The words this style writes itself; none by default, each written as its base writes it. */
    default List<Writing> writings() { return List.of(); }
}
