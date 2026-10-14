package hue.captains.singapura.js.homing.design.semantic;

import java.util.List;

/**
 * What a Maven module imports for the designs it gives in styles it does not own (RFC 0067, appendix Two Classes per
 * Leaf, D32): for each style it gives files to, the colours each component drawn in them imports. A style's own
 * imports ({@link DesignStyle#colorImports()}) are for its own files; a module's, for its files alone - so each is held
 * to the files beside it, on any classpath.
 *
 * <p>Found on the classpath by {@link java.util.ServiceLoader}: a module names it in {@code
 * META-INF/services/hue.captains.singapura.js.homing.design.semantic.ColorImportsExtension}, a public record with no
 * components the loader can make.</p>
 */
public interface ColorImportsExtension {

    /** The module, as its files name it: {@code panes}, for {@code selector.panes.css}. */
    String module();

    /** Its designs' imports, a style at a time. */
    List<StyleImports<?>> imports();
}
