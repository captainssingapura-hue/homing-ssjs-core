package hue.captains.singapura.js.homing.color.semantic;

/**
 * The <strong>degree</strong> of a semantic meaning — how intense the feeling is
 * (0 = faintest, approaching 1 = full intensity). A <em>proper rational</em>
 * number in {@code [0, 1)}: {@code numerator < denominator}, both stored in
 * lowest terms.
 *
 * <p>Rational (not floating-point) so degrees are exact and value-equal —
 * {@code of(2, 4)} equals {@code of(1, 2)}. Full intensity ({@code = 1}) is
 * deliberately excluded (a proper fraction); the peak is a limit, not a value.</p>
 */
public record Degree(int numerator, int denominator) {

    // -----------------------------------------------------------------------
    // Common scale points — the "nice" proper fractions, ready to use.
    // (All strictly below 1, since a Degree is a proper fraction.)
    // -----------------------------------------------------------------------

    /** The faintest degree, {@code 0}. */
    public static final Degree ZERO = new Degree(0, 1);
    /** {@code 1/4}. */
    public static final Degree QUARTER = new Degree(1, 4);
    /** {@code 1/3}. */
    public static final Degree ONE_THIRD = new Degree(1, 3);
    /** {@code 1/2}. */
    public static final Degree HALF = new Degree(1, 2);
    /** {@code 2/3}. */
    public static final Degree TWO_THIRDS = new Degree(2, 3);
    /** {@code 3/4}. */
    public static final Degree THREE_QUARTERS = new Degree(3, 4);

    public Degree {
        if (denominator <= 0) {
            throw new IllegalArgumentException("denominator must be positive: " + denominator);
        }
        if (numerator < 0) {
            throw new IllegalArgumentException("numerator must be non-negative: " + numerator);
        }
        if (numerator >= denominator) {
            throw new IllegalArgumentException(
                    "degree must be a proper fraction in [0, 1): " + numerator + "/" + denominator);
        }
        int g = gcd(numerator, denominator);
        numerator /= g;
        denominator /= g;
    }

    /** {@code numerator/denominator}, validated to be a proper fraction in {@code [0, 1)}. */
    public static Degree of(int numerator, int denominator) {
        return new Degree(numerator, denominator);
    }

    /** Quantised access: step {@code s} out of {@code n} — {@code s/n}. Requires {@code s < n}. */
    public static Degree level(int step, int outOf) {
        return new Degree(step, outOf);
    }

    /** Numeric projection for consumers that need one (e.g. an eventual resolver). */
    public double asDouble() {
        return (double) numerator / denominator;
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }
}
