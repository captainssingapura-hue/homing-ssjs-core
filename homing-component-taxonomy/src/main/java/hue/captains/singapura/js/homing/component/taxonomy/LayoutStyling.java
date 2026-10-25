package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * The layouts' styling (owner, 21 Oct): a component the design alone sees. A layout is no component - strictly
 * style-agnostic, one of a flat list of its own ({@link Layout}), apart from this taxonomy - yet what it places is
 * designed: the splitter, the umbrella container and the sub-containers. So one component owns them, for design only:
 * its parts give them their castings, and so their looks, as any owner's parts do. It has no casting and no look of its
 * own, no module implements it, and it plays no part ({@code LAYOUT_STYLING_PLAYS_A_PART}).
 *
 * @param <B> the branch it sits under
 */
public interface LayoutStyling<B extends ComponentBranch> extends Composite<B> {}
