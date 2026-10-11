package hue.captains.singapura.js.homing.design.semantic;

import java.util.List;

/**
 * What a Maven module writes in a style it does not own: the words it adds to a lexicon (a {@code
 * LexiconExtension}), written in the base style every style is written over so that every style draws
 * them, and in any other style where they should look otherwise. A module writes only its own words:
 * the lexicon's are the styles' own to write.
 *
 * <p>Found on the classpath by {@link java.util.ServiceLoader}: a module names it in {@code
 * META-INF/services/hue.captains.singapura.js.homing.design.semantic.WritingExtension}, one record for
 * each style it writes in.</p>
 */
public interface WritingExtension {

    /** The style it writes in, by type. */
    Class<? extends DesignStyle> writes();

    /** Its writings, in the order it lists them. */
    List<Writing> writings();
}
