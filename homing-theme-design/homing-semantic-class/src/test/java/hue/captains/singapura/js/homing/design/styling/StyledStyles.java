package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.ColorImports;
import hue.captains.singapura.js.homing.design.semantic.Derives;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.Implementation;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Alarm;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Copy;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Danger;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Glare;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Page;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Raised;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Risk;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Safe;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Warning;
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

    /**
     * The base layer: its own content for every leaf, the extension's badge included - each design's colours imported,
     * the meter's clear only in its own colours' values, the badge's by the extension, for its own file (D32).
     */
    public record Plain() implements DesignStyle<ColorScheme.Base> {
        public static final Plain INSTANCE = new Plain();
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public StylingRes style(Casting leaf) { return CONTENT; }
        @Override public List<ColorImports<ColorScheme.Base>> colorImports() { return IMPORTS; }
        static final List<ColorImports<ColorScheme.Base>> IMPORTS = List.of(
                new ColorImports<>(StatedTaxonomy.Tab.INSTANCE, new Copy(), new Page(), new Raised(), new Danger(), new Safe()),
                new ColorImports<>(StatedTaxonomy.Strip.INSTANCE, new Raised()),
                new ColorImports<>(StyledTaxonomy.Meter.INSTANCE, new Risk(), new Warning(), new Alarm(), new Safe()));
    }

    /** Over plain: a tab of its own, every other leaf plain's. */
    public record Ink() implements Derives<ColorScheme.Base, Plain> {
        public static final Ink INSTANCE = new Ink();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public StylingRes style(Casting leaf) { return leaf.equals(new Casting(StatedTaxonomy.Tab.INSTANCE)) ? CONTENT : ABSENT; }
        @Override public List<ColorImports<ColorScheme.Base>> colorImports() {
            return List.of(new ColorImports<>(StatedTaxonomy.Tab.INSTANCE, new Copy(), new Raised(), new Danger()));
        }
    }

    /** Of the loud scheme, over plain: a meter in the glare only its scheme sees. */
    public record Loudly() implements Derives<ResolvingColours.Loud, Plain> {
        public static final Loudly INSTANCE = new Loudly();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public StylingRes style(Casting leaf) { return leaf.equals(new Casting(StyledTaxonomy.Meter.INSTANCE)) ? CONTENT : ABSENT; }
        @Override public List<ColorImports<ResolvingColours.Loud>> colorImports() {
            return List.of(new ColorImports<>(StyledTaxonomy.Meter.INSTANCE, new Glare(), new Page()));
        }
    }

    /** A base layer with holes: absent for the meter, content for the badge with no file for it, and a file for no leaf. */
    public record Shabby() implements DesignStyle<ColorScheme.Base> {
        public static final Shabby INSTANCE = new Shabby();
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public StylingRes style(Casting leaf) { return leaf.equals(new Casting(StyledTaxonomy.Meter.INSTANCE)) ? ABSENT : CONTENT; }
        @Override public List<ColorImports<ColorScheme.Base>> colorImports() {
            return List.of(new ColorImports<>(StatedTaxonomy.Tab.INSTANCE, new Copy()), new ColorImports<>(StatedTaxonomy.Strip.INSTANCE, new Copy()));
        }
    }

    /** Over shabby, absent for every leaf: shabby's holes are its own. */
    public record OverShabby() implements Derives<ColorScheme.Base, Shabby> {
        public static final OverShabby INSTANCE = new OverShabby();
        @Override public Shabby base() { return Shabby.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }

    /** Over plain, absent for every leaf, with a file for the tab all the same. */
    public record Stray() implements Derives<ColorScheme.Base, Plain> {
        public static final Stray INSTANCE = new Stray();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }
}
