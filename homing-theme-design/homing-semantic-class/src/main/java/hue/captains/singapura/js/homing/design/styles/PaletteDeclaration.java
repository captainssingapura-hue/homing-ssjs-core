package hue.captains.singapura.js.homing.design.styles;

import hue.captains.singapura.js.homing.design.semantic.ColorPalette;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * What a jar of palettes provides (RFC 0067, D45): every palette it holds, declared explicitly - not found on the
 * classpath - as a jar of styles declares its castings ({@link StyleDeclaration}). A jar may hold palettes of several
 * schemes, or a set of one scheme's; the instance registers each jar's declaration and holds every palette to its checks
 * as it is made (the styling): of a scheme a style requires, whole over that scheme's colours, sitting on palettes
 * registered too.
 */
public interface PaletteDeclaration extends StatelessFunctionalObject {

    /** Every palette the jar provides, in the order a picker offers them. */
    List<? extends ColorPalette<?>> palettes();
}
