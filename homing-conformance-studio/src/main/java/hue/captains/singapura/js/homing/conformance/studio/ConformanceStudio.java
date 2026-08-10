package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;

/**
 * RFC 0044 — the conformance studio's harness identity: home landing catalogue
 * + brand. Deliberately thin. The real UI is the dedicated conformance
 * {@code WorkspaceSpec}; the studio exists to satisfy Bootstrap and brand the
 * deploy. The module inventory is NOT modeled here (it rides {@code /module-tree}
 * in the workspace), so the studio carries no module knowledge — a downstream
 * reuses the workspace spec + fixtures over its own registry and supplies its
 * own studio identity.
 */
public record ConformanceStudio() implements Studio<ConformanceLandingCatalogue> {

    public static final ConformanceStudio INSTANCE = new ConformanceStudio();

    @Override
    public ConformanceLandingCatalogue home() { return ConformanceLandingCatalogue.INSTANCE; }

    @Override
    public StudioBrand standaloneBrand() {
        return new StudioBrand("Crate-Studio", ConformanceLandingCatalogue.class);
    }
}
