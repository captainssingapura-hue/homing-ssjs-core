package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.List;

/**
 * A design style (RFC 0067): a typed identity - a stateless record, its type its identity - and what it answers. It
 * requires one colour scheme, its type argument, and is worn only in a palette of that scheme. It says which colour each
 * thing uses, in semantic colours its scheme can see ({@link #colorings()}), and never what a colour is: no answer can
 * name a value. A style written over another says so by type, with {@link Derives}; one that derives from nothing
 * answers every root, colour's included, so that every class resolves. A style also writes the words of a lexicon -
 * the glyph it draws for each ({@link Writing}) - the words its base writes its own unless it writes them otherwise.
 *
 * @param <S> the colour scheme it requires
 */
public interface DesignStyle<S extends ColorScheme> {

    /** The style's token, from its type: {@code neo-brutalism}. */
    default String token() { return NodeName.ofType(getClass(), "").value(); }

    /** What a picker calls it: its token's words, capitalised - {@code Neo-Brutalism} - unless it says otherwise. */
    default String label() { return Labels.of(token()); }

    /** One line on what it is trying to be, shown beside its name in a picker; none unless it says. */
    default String inspiration() { return ""; }

    /** The family a picker files it under; {@code Styles} unless it says. */
    default String group() { return "Styles"; }

    /** The classes this style answers itself on every target that is not colour. */
    List<Implementation<?>> implementations();

    /** Which colour each class it answers itself uses; none by default, each as its base colours it. */
    default List<Coloring<S, ?>> colorings() { return List.of(); }

    /** The words this style writes itself; none by default, each written as its base writes it. */
    default List<Writing> writings() { return List.of(); }

    /**
     * The style's design for a casting - a component on its own or in a role, each a leaf for its class (RFC 0067, appendix
     * Two Classes per Leaf, D25): its own content, or absence, and the
     * base layer's content is used. Absence by default, the common case; the base layer - a style that derives from
     * nothing - answers content for every leaf, and the check refuses it any absence (D26).
     */
    default StylingRes style(Casting casting) { return StylingRes.Absence.INSTANCE; }
}
