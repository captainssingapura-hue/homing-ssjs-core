package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch every leaf of which is virtual (RFC 0067, appendix Virtual Leaves and Designs; renamed by D29, appendix
 * Local Styles). A few things mean the same everywhere yet must look different side by side, in one theme - a grid's
 * edge in a document, on a game board, in the game's second flavour - and they sit under a local-style branch: a local
 * style styles it, chosen where the thing is made, and is judged complete over it. Mixed into a levelled branch:
 *
 * <pre>{@code
 * public record GridFrame() implements L2_ComponentBranch<Region>, LocalStyleBranch { … }
 * }</pre>
 *
 * <p>Only virtual leaves sit under it, with no parts, and no branch under it: refused when the taxonomy
 * is read, since a local style could never be complete over anything else.</p>
 */
public interface LocalStyleBranch {}
