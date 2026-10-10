package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.List;
import java.util.Set;

/**
 * A colour palette (RFC 0067, D7): a typed identity, as a style is, answering colour and nothing
 * else - typed so, on the colour targets alone. It is dumb: its own CSS or a redirect, and what it
 * leaves unanswered falls back along the tree. It has no base, and answers the six colour roots.
 *
 * <p>A palette says which styles it suits. A style is worn in its own palette unless another is
 * asked for, and a palette that suits it is offered beside its own: when the style is changed, the
 * palette worn is kept if it suits the new style, and the new style's own is taken if not. The
 * palette names the styles, never the other way round, so a palette added later suits styles that
 * never heard of it. It is advice, not a fence: any style may be worn in any palette by asking for
 * the pair.</p>
 */
public interface ColorPalette {
    /** The palette's token, from its type: {@code harbour}. */
    default String token() { return NodeName.ofType(getClass(), "").value(); }
    /** The colour classes this palette answers. */
    List<Implementation<? extends Target.Color>> implementations();
    /** The styles this palette suits besides any whose own it is - by their types. None unless it says so. */
    default Set<Class<? extends DesignStyle>> suits() { return Set.of(); }
}
