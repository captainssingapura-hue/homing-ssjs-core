package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.List;

/**
 * A design style (RFC 0067): a typed identity - a stateless record, its type its identity - and
 * what it answers. A style never answers colour: that is its palette's. A style written over another
 * says so by type, with {@link Derives}; one that derives from nothing answers every root that is
 * not colour, so that every class resolves.
 */
public interface DesignStyle {

    /** The style's token, from its type: {@code neo-brutalism}. */
    default String token() { return NodeName.ofType(getClass(), "").value(); }

    /** The classes this style answers itself. */
    List<Implementation<?>> implementations();
}
