package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A component that is only ever a part of another, never one on its own at the top: a submenu, shown beside the row of
 * the menu that holds it (owner, 23 Oct). Said by the component:
 * {@code record L1Menu() implements Composite<Menu>, PartOnly}. Its implementation refuses to be made without its
 * owner's placement, and a demo shows it only inside its owner. A simple marker for now: the reader does not yet check
 * that some component holds it.
 */
public interface PartOnly {}
