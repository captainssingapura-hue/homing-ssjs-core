package hue.captains.singapura.js.homing.design.color;

import hue.captains.singapura.tao.ontology.Scalar;

import java.util.Locale;
import java.util.Objects;

/**
 * A colour as a value, in one of two spaces: sRGB or OKLCH. What a palette gives for a palettable colour and what a
 * fixed colour declares - a curated colour, never a mixture: how a palette arrives at its colours is a concern of its
 * own, kept out of the stack. Checked when made, from its numbers or from its written form; equal by its numbers, and
 * written one way whatever it was read from.
 */
public sealed interface ConcreteColor extends Scalar permits ConcreteColor.Srgb, ConcreteColor.Oklch {

    /** As a sheet writes it. */
    String css();

    /**
     * An sRGB colour: three channels from 0 to 255 and an alpha from 0 to 1. Read from {@code #rgb}, {@code #rgba},
     * {@code #rrggbb}, {@code #rrggbbaa}, {@code rgb()} or {@code rgba()} - commas or spaces, the alpha after a slash
     * or a fourth comma, a number or a percentage - or {@code transparent}. Written {@code #rrggbb} when opaque,
     * {@code transparent} when it is, and {@code rgb(r g b / a)} otherwise.
     */
    final class Srgb implements ConcreteColor {

        private final int red, green, blue;
        private final double alpha;

        public Srgb(int red, int green, int blue) { this(red, green, blue, 1); }

        public Srgb(int red, int green, int blue, double alpha) {
            this.red = channel("red", red);
            this.green = channel("green", green);
            this.blue = channel("blue", blue);
            this.alpha = ColorWriting.INSTANCE.anAlpha(alpha);
        }

        public Srgb(String written) { this(ColorWriting.INSTANCE.srgb(written)); }

        private Srgb(double[] v) { this((int) v[0], (int) v[1], (int) v[2], v[3]); }

        private int channel(String name, int v) {
            if (v < 0 || v > 255) throw new IllegalArgumentException("an sRGB " + name + " channel is from 0 to 255, not " + v);
            return v;
        }

        /** The red channel, from 0 to 255. */
        public int red() { return red; }
        /** The green channel, from 0 to 255. */
        public int green() { return green; }
        /** The blue channel, from 0 to 255. */
        public int blue() { return blue; }
        /** The opacity, from 0 to 1. */
        public double alpha() { return alpha; }

        @Override public String css() {
            if (alpha == 1) return String.format(Locale.ROOT, "#%02x%02x%02x", red, green, blue);
            if (alpha == 0 && red == 0 && green == 0 && blue == 0) return "transparent";
            return "rgb(" + red + " " + green + " " + blue + " / " + ColorWriting.INSTANCE.plain(alpha) + ")";
        }

        @Override public boolean equals(Object o) {
            return o instanceof Srgb x && x.red == red && x.green == green && x.blue == blue && Double.compare(x.alpha, alpha) == 0;
        }

        @Override public int hashCode() { return Objects.hash(red, green, blue, alpha); }

        @Override public String toString() { return css(); }
    }

    /**
     * An OKLCH colour: a lightness from 0 to 1, a chroma from 0, a hue in degrees and an alpha from 0 to 1. Read from
     * {@code oklch(L C H)} or {@code oklch(L C H / A)} - the lightness a number or a percentage, the hue a number with
     * or without {@code deg}, turned into [0, 360). Written {@code oklch(L C H)}, the alpha after a slash when it is
     * not 1.
     */
    final class Oklch implements ConcreteColor {

        private final double lightness, chroma, hue, alpha;

        public Oklch(double lightness, double chroma, double hue) { this(lightness, chroma, hue, 1); }

        public Oklch(double lightness, double chroma, double hue, double alpha) {
            if (!(lightness >= 0 && lightness <= 1)) throw new IllegalArgumentException("an OKLCH lightness is from 0 to 1, not " + lightness);
            if (!(chroma >= 0) || Double.isInfinite(chroma)) throw new IllegalArgumentException("an OKLCH chroma is a number from 0, not " + chroma);
            if (!Double.isFinite(hue)) throw new IllegalArgumentException("an OKLCH hue is a number of degrees, not " + hue);
            this.lightness = lightness;
            this.chroma = chroma;
            double h = hue % 360;
            this.hue = h < 0 ? h + 360 : h;
            this.alpha = ColorWriting.INSTANCE.anAlpha(alpha);
        }

        public Oklch(String written) { this(ColorWriting.INSTANCE.oklch(written)); }

        private Oklch(double[] v) { this(v[0], v[1], v[2], v[3]); }

        /** The lightness, from 0 to 1. */
        public double lightness() { return lightness; }
        /** The chroma, from 0 up. */
        public double chroma() { return chroma; }
        /** The hue in degrees, from 0 up to but not including 360. */
        public double hue() { return hue; }
        /** The opacity, from 0 to 1. */
        public double alpha() { return alpha; }

        @Override public String css() {
            String lch = ColorWriting.INSTANCE.plain(lightness) + " " + ColorWriting.INSTANCE.plain(chroma) + " " + ColorWriting.INSTANCE.plain(hue);
            return "oklch(" + lch + (alpha == 1 ? "" : " / " + ColorWriting.INSTANCE.plain(alpha)) + ")";
        }

        @Override public boolean equals(Object o) {
            return o instanceof Oklch x && Double.compare(x.lightness, lightness) == 0 && Double.compare(x.chroma, chroma) == 0
                    && Double.compare(x.hue, hue) == 0 && Double.compare(x.alpha, alpha) == 0;
        }

        @Override public int hashCode() { return Objects.hash(lightness, chroma, hue, alpha); }

        @Override public String toString() { return css(); }
    }
}
