package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;

/**
 * L0 root catalogue for the standalone Color Studio deploy. Deliberately
 * minimal — the Color Studio's real surface is the {@code color} workspace
 * ({@link ColorWorkspaceSpec}), reached at
 * {@code ?app=genericWorkspace&ws_kind=color}, not a catalogue of docs. This
 * exists only to satisfy the framework's "every studio has one L0 home"
 * contract (it anchors the brand and the root redirect).
 */
public record ColorCatalogue() implements L0_Catalogue<ColorCatalogue> {

    public static final ColorCatalogue INSTANCE = new ColorCatalogue();

    @Override public String name()    { return "Homing · Colours"; }
    @Override public String summary() {
        return "Semantic-colour management. Open the Colours workspace "
             + "(?app=genericWorkspace&ws_kind=color) to browse the ColorGroup "
             + "taxonomy and the colours within each group.";
    }
    @Override public String badge() { return "STUDIO"; }
    @Override public String icon()  { return "🎨"; }
}
