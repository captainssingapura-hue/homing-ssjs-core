package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.Exportable;

/**
 * The {@code domOpsParties} singleton export marker for {@link DomOpsPartyModule}:
 * the page's party of parties - its stationed party, and every mobile party
 * made on the page (RFC 0066 E3, grafting). A sibling top-level record, as
 * {@link domOpsParty} is, so that it does not case-collide on Windows with the
 * {@code DomOpsParties} class export nested in the module.
 */
@SuppressWarnings("checkstyle:TypeName")
public record domOpsParties() implements Exportable._Constant<DomOpsPartyModule> {}
