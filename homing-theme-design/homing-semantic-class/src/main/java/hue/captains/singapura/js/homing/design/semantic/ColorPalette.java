package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.List;

/**
 * A colour palette (RFC 0067, D7): a typed identity, as a style is, answering colour and nothing
 * else - typed so, on the colour targets alone. It is dumb: its own CSS or a redirect, and what it
 * leaves unanswered falls back along the tree. It has no base, and answers the six colour roots.
 */
public interface ColorPalette {

    /** The palette's token, from its type: {@code harbour}. */
    default String token() { return NodeName.ofType(getClass(), "").value(); }

    /** The colour classes this palette answers. */
    List<Implementation<? extends Target.Color>> implementations();
}
