package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.Exportable;

/**
 * The root's owner, {@code partyChief}, exported so that it is <b>retained</b>.
 *
 * <p>RFC 0063 found, by building the monitor's snapshot, that the root read as
 * leaked on every workspace: {@code isOwnerAlive} was {@code false} for a
 * branch whose owner is a module-level {@code const} the source calls "never
 * GC'd". The comment was wrong. V8 does not keep a module-scope binding that is
 * neither exported nor captured by a closure — after evaluation it is
 * unreachable, and a {@code WeakRef} to it clears at the first collection.
 * Confirmed against a purpose-built module: an uncaptured top-level
 * {@code const} was collected, a captured one was not.</p>
 *
 * <p>Exporting it is the framework's own way of saying a module-level binding
 * is part of the surface, and an exported binding is retained. Nothing needs
 * to import it; its being importable is what keeps it alive. A sibling
 * top-level record for the same case-collision reason as {@link domOpsParty}.</p>
 *
 * @since RFC 0063
 */
@SuppressWarnings("checkstyle:TypeName")
public record partyChief() implements Exportable._Constant<DomOpsPartyModule> {}
