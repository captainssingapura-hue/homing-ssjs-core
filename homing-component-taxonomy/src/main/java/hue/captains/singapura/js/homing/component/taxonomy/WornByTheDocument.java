package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch whose leaves the document itself wears (RFC 0071 port, owner 24 Oct: "go with :root, keep it out of runtime"):
 * a leaf under it is a style's CSS and nothing else - its rule written on {@code :root}, the document's own element, by
 * the sheet writer; no module implements it, no class is placed for it, no element is made for it. The page's ground is
 * one: its colour, its ink, its face, its scrollbar. Said by the branch: {@code record Document() implements
 * L1_ComponentBranch<Root>, WornByTheDocument, FamilyHead}.
 *
 * <p>What such a leaf cannot have, the reader refuses: parts, states, rooms, values, axes, a role set, or a part to play -
 * each of these is an element's. And a component that says it: only a branch does. A module that implements such a leaf
 * the implementations gate refuses.</p>
 */
public interface WornByTheDocument {}
