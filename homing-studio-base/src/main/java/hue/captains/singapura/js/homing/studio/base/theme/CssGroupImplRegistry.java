package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssGroupImpl;

import java.util.List;

/**
 * Manual registry of every {@link CssGroupImpl} the studio-base ships.
 *
 * <p>The framework's {@code CssContentGetAction} consults this list (or a
 * downstream-provided equivalent) to resolve which impl to render for a given
 * {@code (CssGroup canonical name, Theme.slug)} pair. RFC 0002 §3.4 captures
 * the eternal-good rationale for keeping it manual: audit-able, build-deterministic,
 * reflection-free, downstream-friendly, fail-loud.</p>
 *
 * <p>RFC 0066: a theme's {@link GlobalColorPalette.Provision} is an impl too, but it
 * travels with the theme — {@link StudioThemeRegistry#palettes()} lists them and the
 * boot joins the two lists.</p>
 *
 * <p>Adding a new impl: instantiate it (or use its {@code INSTANCE}) and append
 * to {@link #ALL}. Concrete classes belong in this same package by convention.</p>
 */
public final class CssGroupImplRegistry {

    /** Every CssGroup × Theme implementation shipped by homing-studio-base. */
    public static final List<CssGroupImpl<?, ?>> ALL = List.of(
            UtilImpl.INSTANCE
    );

    private CssGroupImplRegistry() {}
}
