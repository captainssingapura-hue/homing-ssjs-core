package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch that heads a family of components (RFC 0067, appendix Two Classes per Leaf): every component under it, at
 * any depth, is of its family, and a style's CSS for them is written in the family's file -
 * {@code editorial/button.css} - each casting's rule whole in it, a casting in a role beside its component. Said by the
 * branch: {@code record Button() implements L2_ComponentBranch<Control>, FamilyHead}.
 *
 * <p>One level of heads: the reader refuses a head with another head between it and the root, and a component that
 * says it heads a family - only a branch does. A component with no head above it has no file, which the style check
 * refuses where files are asked for.</p>
 */
public interface FamilyHead {}
