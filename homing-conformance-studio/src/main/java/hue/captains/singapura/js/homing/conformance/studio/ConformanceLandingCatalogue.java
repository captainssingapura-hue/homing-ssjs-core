package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Navigable;
import hue.captains.singapura.js.homing.workspace.shell.GenericWorkspace;

import java.util.List;

/**
 * RFC 0044 — a minimal landing catalogue for the conformance studio: one tile
 * that opens the dedicated conformance workspace. It exists only to satisfy the
 * Bootstrap harness (a studio needs a home catalogue) and to give the deploy a
 * clean entry page + brand home; the module inventory itself lives entirely in
 * the workspace (fed by {@code /module-tree}), NOT modeled as catalogue content.
 */
public record ConformanceLandingCatalogue() implements L0_Catalogue<ConformanceLandingCatalogue> {

    public static final ConformanceLandingCatalogue INSTANCE = new ConformanceLandingCatalogue();

    @Override public String name()    { return "Conformance"; }
    @Override public String badge()   { return "STUDIO"; }
    @Override public String summary() { return "Browse and validate the JS module set."; }

    @Override
    public List<Entry<ConformanceLandingCatalogue>> leaves() {
        Navigable<GenericWorkspace.Params, GenericWorkspace> workspace =
                new Navigable<>(GenericWorkspace.INSTANCE,
                        new GenericWorkspace.Params("conformance"),
                        "Conformance Workspace",
                        "The module Navigator plus Summary, Full Content, and Conformance panes.");
        return List.of(Entry.of(this, workspace));
    }
}
