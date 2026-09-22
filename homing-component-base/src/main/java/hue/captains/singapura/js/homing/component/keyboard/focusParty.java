package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.Exportable;

/**
 * The page's logical-focus tree, one per document: the root branch of
 * {@link FocusPartyModule}. A top-level record, as {@code domOpsParty} is,
 * since a nested one would share a class-file name with {@code FocusParty}
 * on a case-insensitive filesystem.
 */
@SuppressWarnings("checkstyle:TypeName")
public record focusParty() implements Exportable._Constant<FocusPartyModule> {}
