package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.Exportable;

/**
 * {@code viewParty()} — the party tree as data, and the <b>only</b> thing a
 * monitor imports.
 *
 * <p>RFC 0063's observation by construction. The temptation is to import
 * {@link domOpsParty} and call {@code snapshot()} on it — but then the widget's
 * closure holds the root handle, which can dissolve everything, and a frozen
 * snapshot protects nothing. This function closes over the root inside the
 * party module and hands out only the projection: deep-frozen, no function, no
 * live reference. A holder of it can inspect the tree and cannot touch it.</p>
 *
 * <p>A sibling top-level record for the same case-collision reason as
 * {@link domOpsParty}.</p>
 *
 * @since RFC 0063
 */
@SuppressWarnings("checkstyle:TypeName")
public record viewParty() implements Exportable._Constant<DomOpsPartyModule> {}
