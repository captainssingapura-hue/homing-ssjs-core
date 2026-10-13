package hue.captains.singapura.js.homing.design.color;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A colour as a value, in one of two spaces: sRGB or OKLCH. What a palette gives for a palettable colour and what a
 * fixed colour declares - a curated colour, never a mixture: how a palette arrives at its colours is a concern of its
 * own, kept out of the stack. Checked when made, from its numbers or from its written form; equal by its numbers, and
 * written one way whatever it was read from.
 */
public sealed interface ConcreteColor permits ConcreteColor.Srgb, ConcreteColor.Oklch {

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
            this.alpha = anAlpha(alpha);
        }

        public Srgb(String written) { this(read(written)); }

        private Srgb(double[] v) { this((int) v[0], (int) v[1], (int) v[2], v[3]); }

        private static double[] read(String written) {
            String s = trimmed(written);
            if (s.equals("transparent")) return new double[]{0, 0, 0, 0};
            if (s.startsWith("#")) {
                String h = s.substring(1);
                if (!h.matches("[0-9a-f]{3,4}|[0-9a-f]{6}|[0-9a-f]{8}")) throw unread(written, "a hex colour has 3, 4, 6 or 8 hex digits");
                if (h.length() <= 4) h = h.replaceAll("(.)", "$1$1");
                return new double[]{Integer.parseInt(h.substring(0, 2), 16), Integer.parseInt(h.substring(2, 4), 16), Integer.parseInt(h.substring(4, 6), 16),
                        h.length() == 8 ? Math.round(Integer.parseInt(h.substring(6, 8), 16) / 255.0 * 1000) / 1000.0 : 1};
            }
            List<String> parts = arguments(written, s, "rgb", "rgba");
            if (parts.size() != 3 && parts.size() != 4) throw unread(written, "rgb() takes three channels and an alpha");
            double[] v = new double[4];
            for (int i = 0; i < 3; i++) {
                String p = parts.get(i);
                if (!p.matches("\\d{1,3}")) throw unread(written, "a channel is a whole number from 0 to 255, not " + p);
                v[i] = Integer.parseInt(p);
            }
            v[3] = parts.size() == 4 ? alphaOf(written, parts.get(3)) : 1;
            return v;
        }

        private static int channel(String name, int v) {
            if (v < 0 || v > 255) throw new IllegalArgumentException("an sRGB " + name + " channel is from 0 to 255, not " + v);
            return v;
        }

        public int red() { return red; }
        public int green() { return green; }
        public int blue() { return blue; }
        public double alpha() { return alpha; }

        @Override public String css() {
            if (alpha == 1) return String.format(Locale.ROOT, "#%02x%02x%02x", red, green, blue);
            if (alpha == 0 && red == 0 && green == 0 && blue == 0) return "transparent";
            return "rgb(" + red + " " + green + " " + blue + " / " + plain(alpha) + ")";
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
            this.alpha = anAlpha(alpha);
        }

        public Oklch(String written) { this(read(written)); }

        private Oklch(double[] v) { this(v[0], v[1], v[2], v[3]); }

        private static double[] read(String written) {
            List<String> parts = arguments(written, trimmed(written), "oklch");
            if (parts.size() != 3 && parts.size() != 4) throw unread(written, "oklch() takes a lightness, a chroma, a hue and an alpha");
            String l = parts.get(0), h = parts.get(2);
            return new double[]{
                    l.endsWith("%") ? number(written, l.substring(0, l.length() - 1)) / 100 : number(written, l),
                    number(written, parts.get(1)),
                    number(written, h.endsWith("deg") ? h.substring(0, h.length() - 3) : h),
                    parts.size() == 4 ? alphaOf(written, parts.get(3)) : 1};
        }

        public double lightness() { return lightness; }
        public double chroma() { return chroma; }
        public double hue() { return hue; }
        public double alpha() { return alpha; }

        @Override public String css() {
            String lch = plain(lightness) + " " + plain(chroma) + " " + plain(hue);
            return "oklch(" + lch + (alpha == 1 ? "" : " / " + plain(alpha)) + ")";
        }

        @Override public boolean equals(Object o) {
            return o instanceof Oklch x && Double.compare(x.lightness, lightness) == 0 && Double.compare(x.chroma, chroma) == 0
                    && Double.compare(x.hue, hue) == 0 && Double.compare(x.alpha, alpha) == 0;
        }

        @Override public int hashCode() { return Objects.hash(lightness, chroma, hue, alpha); }

        @Override public String toString() { return css(); }
    }

    // ── reading the written forms ──────────────────────────────────────────

    private static String trimmed(String written) {
        if (written == null) throw new IllegalArgumentException("a colour is written, not null");
        return written.trim().toLowerCase(Locale.ROOT);
    }

    /** {@code rgb(1, 2, 3)} → [1, 2, 3]; {@code rgb(1 2 3 / .5)} → [1, 2, 3, .5]. */
    private static List<String> arguments(String written, String s, String... functions) {
        int open = s.indexOf('(');
        if (open < 0 || !s.endsWith(")") || !List.of(functions).contains(s.substring(0, open).trim()))
            throw unread(written, "not " + String.join("() or ", functions) + "()");
        String inner = s.substring(open + 1, s.length() - 1).trim();
        String alpha = null;
        int slash = inner.indexOf('/');
        if (slash >= 0) {
            alpha = inner.substring(slash + 1).trim();
            inner = inner.substring(0, slash).trim();
            if (alpha.isEmpty() || alpha.contains("/")) throw unread(written, "one alpha after the slash");
            if (inner.contains(",")) throw unread(written, "commas or a slash, not both");
        }
        var out = new ArrayList<String>();
        for (String p : inner.split(inner.contains(",") ? "\\s*,\\s*" : "\\s+")) if (!p.isEmpty()) out.add(p);
        if (alpha != null) out.add(alpha);
        return out;
    }

    private static double alphaOf(String written, String p) {
        return p.endsWith("%") ? number(written, p.substring(0, p.length() - 1)) / 100 : number(written, p);
    }

    private static double number(String written, String p) {
        if (!p.matches("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)")) throw unread(written, "not a number: " + p);
        return Double.parseDouble(p);
    }

    private static double anAlpha(double a) {
        if (!(a >= 0 && a <= 1)) throw new IllegalArgumentException("an alpha is from 0 to 1, not " + a);
        return a;
    }

    private static String plain(double d) {
        return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
    }

    private static IllegalArgumentException unread(String written, String why) {
        return new IllegalArgumentException("'" + written + "' is no colour: " + why);
    }
}
