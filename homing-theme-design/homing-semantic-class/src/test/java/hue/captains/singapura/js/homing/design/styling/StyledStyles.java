package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentLeaf;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.Derives;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.Implementation;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.semantic.StylingRes;

import java.util.List;

/**
 * The styles the checks are run over, each a function of a leaf, their files under
 * {@code styles/hue/captains/singapura/js/homing/design/styling/}: {@link Plain}, a base layer; {@link Ink} and
 * {@link Loudly} over it; and two that are refused - {@link Shabby}, a base layer with holes, and {@link Stray}, with a
 * file it does not answer.
 */
public record StyledStyles() {

    private static final StylingRes CONTENT = StylingRes.CssContent.INSTANCE;
    private static final StylingRes ABSENT = StylingRes.Absence.INSTANCE;

    /** The base layer: its own content for every leaf, the extension's badge included. */
    public record Plain() implements DesignStyle<ColorScheme.Base> {
        public static final Plain INSTANCE = new Plain();
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public StylingRes style(ComponentLeaf leaf) { return CONTENT; }
    }

    /** Over plain: a tab of its own, every other leaf plain's. */
    public record Ink() implements Derives<ColorScheme.Base, Plain> {
        public static final Ink INSTANCE = new Ink();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public StylingRes style(ComponentLeaf leaf) { return leaf instanceof StatedTaxonomy.Tab ? CONTENT : ABSENT; }
    }

    /** Of the loud scheme, over plain: a meter in the glare only its scheme sees. */
    public record Loudly() implements Derives<ResolvingColours.Loud, Plain> {
        public static final Loudly INSTANCE = new Loudly();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public StylingRes style(ComponentLeaf leaf) { return leaf instanceof StyledTaxonomy.Meter ? CONTENT : ABSENT; }
    }

    /** A base layer with holes: absent for the meter, content for the badge with no file for it, and a file for no leaf. */
    public record Shabby() implements DesignStyle<ColorScheme.Base> {
        public static final Shabby INSTANCE = new Shabby();
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public StylingRes style(ComponentLeaf leaf) { return leaf instanceof StyledTaxonomy.Meter ? ABSENT : CONTENT; }
    }

    /** Over plain, absent for every leaf, with a file for the tab all the same. */
    public record Stray() implements Derives<ColorScheme.Base, Plain> {
        public static final Stray INSTANCE = new Stray();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }
}
