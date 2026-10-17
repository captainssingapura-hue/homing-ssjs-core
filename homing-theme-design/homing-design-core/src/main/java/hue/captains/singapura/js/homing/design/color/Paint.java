package hue.captains.singapura.js.homing.design.color;

import hue.captains.singapura.js.homing.design.Target.Color;

import java.util.Objects;

/**
 * What a colour target is painted with, in one state (RFC 0067, appendix Semantic Colours and Flavours): one of the
 * cases its target offers. A case names semantic colours, never a value, and is bounded by type to the targets that
 * offer it - so nothing lands where nobody means it: a scale in a shadow's glow, the typography colour in a ramp's
 * stop, an inherited border.
 *
 * <table>
 *   <caption>The cases each target offers</caption>
 *   <tr><th>Target</th><th>Its cases</th></tr>
 *   <tr><td>Text</td><td>{@link With} · {@link Inherited} · {@link Along}</td></tr>
 *   <tr><td>Fill</td><td>{@link With} · {@link Inherited} · {@link Nothing} · {@link Typography}</td></tr>
 *   <tr><td>Stroke</td><td>{@link With} · {@link Inherited} · {@link Nothing}</td></tr>
 *   <tr><td>Scrollbar</td><td>{@link ThumbAndTrack} · {@link Inherited}</td></tr>
 *   <tr><td>Background</td><td>{@link Drawn} · {@link Ground} · {@link Along}</td></tr>
 *   <tr><td>Border</td><td>{@link With} · {@link Sides} · {@link Typography}</td></tr>
 *   <tr><td>Outline, ColumnRule</td><td>{@link With} · {@link Typography}</td></tr>
 *   <tr><td>BoxShadow</td><td>{@link Shadow} · {@link Cast}</td></tr>
 * </table>
 *
 * @param <S> the scheme of what names it: the colours it names are of that scheme or one below
 * @param <T> the target painted
 */
public sealed interface Paint<S extends ColorScheme, T extends Color>
        permits Paint.With, Paint.Inherited, Paint.Nothing, Paint.Typography, Paint.Along,
                Paint.ThumbAndTrack, Paint.Ground, Paint.Drawn, Paint.Sides, Paint.Shadow, Paint.Cast {

    /** A semantic colour, for a target that writes one colour. */
    record With<S extends ColorScheme, T extends Color & Color.Single>(SemanticColor<? super S> color) implements Paint<S, T> {
        public With { Objects.requireNonNull(color, "With.color"); }
    }

    /** The colour of what the element stands on: CSS inherits it. */
    record Inherited<S extends ColorScheme, T extends Color & Color.Inheritable>() implements Paint<S, T> {}

    /** Nothing painted. */
    record Nothing<S extends ColorScheme, T extends Color & Color.Omissible>() implements Paint<S, T> {}

    /** The element's typography colour, whole and never mixed - CSS's {@code currentColor}. */
    record Typography<S extends ColorScheme, T extends Color & Color.Typographic>() implements Paint<S, T> {}

    /**
     * Along a scale, by the degree set on the element: the one case that is not a colour, worn only by a casting whose
     * node declares the COLOUR axis. A background moves its ground; the rest is clear.
     */
    record Along<S extends ColorScheme, T extends Color & Color.Scalable>(ColorScale<? super S, ?> scale) implements Paint<S, T> {
        public Along { Objects.requireNonNull(scale, "Along.scale"); }
    }

    /** A scrollbar's two colours. */
    record ThumbAndTrack<S extends ColorScheme>(SemanticColor<? super S> thumb, SemanticColor<? super S> track) implements Paint<S, Color.Scrollbar> {
        public ThumbAndTrack {
            Objects.requireNonNull(thumb, "ThumbAndTrack.thumb");
            Objects.requireNonNull(track, "ThumbAndTrack.track");
        }
    }

    /** A ground alone: nothing drawn on it, a ramp's stops and a pattern's marks clear. */
    record Ground<S extends ColorScheme>(SemanticColor<? super S> ground) implements Paint<S, Color.Background> {
        public Ground { Objects.requireNonNull(ground, "Ground.ground"); }
    }

    /** A ground and what is drawn on it: a ramp's three stops and a pattern's marks. */
    record Drawn<S extends ColorScheme>(SemanticColor<? super S> ground, SemanticColor<? super S> rampFrom, SemanticColor<? super S> rampVia,
                                        SemanticColor<? super S> rampTo, SemanticColor<? super S> mark) implements Paint<S, Color.Background> {
        public Drawn {
            Objects.requireNonNull(ground, "Drawn.ground");
            Objects.requireNonNull(rampFrom, "Drawn.rampFrom");
            Objects.requireNonNull(rampVia, "Drawn.rampVia");
            Objects.requireNonNull(rampTo, "Drawn.rampTo");
            Objects.requireNonNull(mark, "Drawn.mark");
        }
    }

    /** Each side of the border its own colour. */
    record Sides<S extends ColorScheme>(SemanticColor<? super S> top, SemanticColor<? super S> right,
                                        SemanticColor<? super S> bottom, SemanticColor<? super S> left) implements Paint<S, Color.Border> {
        public Sides {
            Objects.requireNonNull(top, "Sides.top");
            Objects.requireNonNull(right, "Sides.right");
            Objects.requireNonNull(bottom, "Sides.bottom");
            Objects.requireNonNull(left, "Sides.left");
        }
    }

    /** A box's shadow: the dark it casts, the light on its own lit side or rim, the light it gives off. */
    record Shadow<S extends ColorScheme>(SemanticColor<? super S> cast, SemanticColor<? super S> lit, SemanticColor<? super S> glow)
            implements Paint<S, Color.BoxShadow> {
        public Shadow {
            Objects.requireNonNull(cast, "Shadow.cast");
            Objects.requireNonNull(lit, "Shadow.lit");
            Objects.requireNonNull(glow, "Shadow.glow");
        }
    }

    /** A cast alone: no light on its side, none given off. */
    record Cast<S extends ColorScheme>(SemanticColor<? super S> cast) implements Paint<S, Color.BoxShadow> {
        public Cast { Objects.requireNonNull(cast, "Cast.cast"); }
    }
}
