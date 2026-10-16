package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.Derives;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.Implementation;
import hue.captains.singapura.js.homing.design.semantic.BaseColours;

import java.util.List;

/**
 * The styles the checks are run over, each its record alone - its implementations classes of their own, in
 * {@code design.styles.<style>} (RFC 0067, appendix The Style Jars): {@link Plain}, a base layer; {@link Ink} and
 * {@link Loudly} over it; and {@link Shabby}, a jar of whose classes is refused.
 */
public record StyledStyles() {

    /** The base layer: an implementation for every casting, the extension's badge included. */
    public record Plain() implements DesignStyle<BaseScheme> {
        public static final Plain INSTANCE = new Plain();
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }

    /** Over plain: a tab of its own, every other casting plain's. */
    public record Ink() implements Derives<BaseScheme, Plain> {
        public static final Ink INSTANCE = new Ink();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }

    /** Of the loud scheme, over plain: a meter in the glare only its scheme sees. */
    public record Loudly() implements Derives<BaseColours.LoudScheme, Plain> {
        public static final Loudly INSTANCE = new Loudly();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }

    /** A base layer whose jar is wrong in every way a jar can be: the declaration's check refuses it. */
    public record Shabby() implements DesignStyle<BaseScheme> {
        public static final Shabby INSTANCE = new Shabby();
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }
}
