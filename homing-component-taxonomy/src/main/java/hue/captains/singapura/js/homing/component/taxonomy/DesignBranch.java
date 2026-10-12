package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch every leaf of which is virtual (RFC 0067, appendix Virtual Leaves and Designs). A few things
 * mean the same everywhere yet must look different side by side, in one theme - a grid's edge in a
 * document, on a game board, in the game's second flavour - and they sit under a design branch: a
 * {@link Design} targets it, and is judged complete over it. Mixed into a levelled branch:
 *
 * <pre>{@code
 * public record GridFrame() implements L2_ComponentBranch<Region>, DesignBranch { … }
 * }</pre>
 *
 * <p>Only virtual leaves sit under it, with no parts, and no branch under it: refused when the taxonomy
 * is read, since a design could never be complete over anything else.</p>
 */
public interface DesignBranch {}
