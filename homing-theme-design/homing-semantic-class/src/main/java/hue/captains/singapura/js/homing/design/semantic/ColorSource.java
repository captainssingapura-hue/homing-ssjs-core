package hue.captains.singapura.js.homing.design.semantic;

/** Where a colour tree is declared: its owner's nodes, and every extension on the classpath grafting onto it ({@link ColorExtensions}). */
public interface ColorSource {

    /** The tree, read and refused nothing ({@link ReadColors}). */
    ColorTree read();
}
