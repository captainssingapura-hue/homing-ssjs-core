package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.Exportable;

/**
 * The {@code domOpsParty} singleton export marker for
 * {@link DomOpsPartyModule}. Lives as a sibling top-level record (not
 * nested inside {@code DomOpsPartyModule}) because nesting it next to
 * the {@code DomOpsParty} class export would case-collide on Windows
 * filesystems — both would compile to {@code DomOpsPartyModule$<Name>.class}
 * files differing only by case, and NTFS would overwrite one with the
 * other.
 *
 * <p>The unconventional lowercase Java record name is deliberate — it
 * matches the JS-side identifier the vendored {@code DomOpsParty.js}
 * exports as the pre-activated root singleton. The framework's
 * {@code ExportWriter} uses the record's simple name verbatim when
 * emitting the {@code export} statement, so this name must be
 * exactly {@code domOpsParty}.</p>
 *
 * @since RFC 0024 Phase P1a (extracted to break Windows case-collision)
 */
@SuppressWarnings("checkstyle:TypeName")
public record domOpsParty() implements Exportable._Constant<DomOpsPartyModule> {}
