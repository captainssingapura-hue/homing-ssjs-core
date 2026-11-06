package hue.captains.singapura.js.homing.design.color;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Reads colours as they are written, and writes their numbers ({@link ConcreteColor}). */
public record ColorWriting() implements StatelessFunctionalObject {

    public static final ColorWriting INSTANCE = new ColorWriting();

    /** The channels and alpha of an sRGB colour as it is written. */
    double[] srgb(String written) {
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

    /** The lightness, chroma, hue and alpha of an OKLCH colour as it is written. */
    double[] oklch(String written) {
        List<String> parts = arguments(written, trimmed(written), "oklch");
        if (parts.size() != 3 && parts.size() != 4) throw unread(written, "oklch() takes a lightness, a chroma, a hue and an alpha");
        String l = parts.get(0), h = parts.get(2);
        return new double[]{
                l.endsWith("%") ? number(written, l.substring(0, l.length() - 1)) / 100 : number(written, l),
                number(written, parts.get(1)),
                number(written, h.endsWith("deg") ? h.substring(0, h.length() - 3) : h),
                parts.size() == 4 ? alphaOf(written, parts.get(3)) : 1};
    }

    private String trimmed(String written) {
        if (written == null) throw new IllegalArgumentException("a colour is written, not null");
        return written.trim().toLowerCase(Locale.ROOT);
    }

    /** {@code rgb(1, 2, 3)} → [1, 2, 3]; {@code rgb(1 2 3 / .5)} → [1, 2, 3, .5]. */
    private List<String> arguments(String written, String s, String... functions) {
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

    private double alphaOf(String written, String p) {
        return p.endsWith("%") ? number(written, p.substring(0, p.length() - 1)) / 100 : number(written, p);
    }

    private double number(String written, String p) {
        if (!p.matches("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)")) throw unread(written, "not a number: " + p);
        return Double.parseDouble(p);
    }

    /** An alpha from 0 to 1. */
    double anAlpha(double a) {
        if (!(a >= 0 && a <= 1)) throw new IllegalArgumentException("an alpha is from 0 to 1, not " + a);
        return a;
    }

    /** A number without its trailing zeros. */
    String plain(double d) {
        return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
    }

    private IllegalArgumentException unread(String written, String why) {
        return new IllegalArgumentException("'" + written + "' is no colour: " + why);
    }
}
