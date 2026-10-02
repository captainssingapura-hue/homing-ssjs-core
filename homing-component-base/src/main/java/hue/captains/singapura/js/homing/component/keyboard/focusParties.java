package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.Exportable;

/**
 * The {@code focusParties} singleton export marker for {@link FocusPartyModule}:
 * the page's party of parties - its stationed focus party, and every mobile one
 * made on the page (RFC 0066 E3, grafting). A top-level record, as
 * {@link focusParty} is, so that it does not case-collide on Windows with the
 * {@code FocusParties} class export nested in the module.
 */
@SuppressWarnings("checkstyle:TypeName")
public record focusParties() implements Exportable._Constant<FocusPartyModule> {}
