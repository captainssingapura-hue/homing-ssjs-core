package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.Map;

/**
 * RFC 0051 — the law every {@link ParamCodec} owes: writing params and reading
 * them back returns the same params.
 *
 * <p>Locking {@code from} and {@code to} into one object removes the chance
 * that two separate implementations drift apart; it does not make either one
 * correct. A codec can still forget a component, drop a repeated value, or
 * write a key it does not read. This is the check that catches that, and it
 * lives in main so every studio can run it over its own apps rather than each
 * repo re-deriving what the law says.</p>
 *
 * <p>Callers supply the sample values — a codec's author knows which params
 * are interesting (empty strings, a repeated key, a value with an {@code &}
 * in it) far better than any generator could guess.</p>
 */
public final class ParamCodecLaw {

    private ParamCodecLaw() {}

    /**
     * Assert {@code from(to(p)) == p} for each sample, and that the encoded
     * form survives a trip through a real query string.
     *
     * @throws AssertionError naming every sample that failed, and how
     */
    public static <P extends AppModule._Param> void assertRoundTrips(
            String codecName, ParamCodec<P> codec, List<P> samples) {

        if (samples == null || samples.isEmpty()) {
            throw new AssertionError(codecName + ": no samples — the law would pass vacuously");
        }
        var failures = new java.util.ArrayList<String>();
        for (P sample : samples) {
            Map<String, List<String>> written;
            try {
                written = codec.to(sample);
            } catch (RuntimeException e) {
                failures.add(sample + ": to() threw " + e);
                continue;
            }
            if (written == null) {
                failures.add(sample + ": to() returned null, but it is declared total");
                continue;
            }
            check(codecName, sample, codec.from(written), "via the map", failures);

            // And again through the wire, which is where a codec that forgot
            // to encode a separator or a space quietly stops round-tripping.
            String encoded = QueryString.encode(written);
            check(codecName, sample, codec.fromQueryString(encoded),
                  "via the query string '" + encoded + "'", failures);
        }
        if (!failures.isEmpty()) {
            throw new AssertionError(codecName + " breaks the round-trip law for "
                    + failures.size() + " sample(s):\n  " + String.join("\n  ", failures));
        }
    }

    private static <P extends AppModule._Param> void check(
            String codecName, P sample, ParamCodec.Decoded<P> decoded,
            String how, List<String> failures) {

        switch (decoded) {
            case ParamCodec.Decoded.Ok<P>(P got) -> {
                if (!got.equals(sample)) {
                    failures.add(sample + " " + how + " came back as " + got);
                }
            }
            case null -> failures.add(sample + " " + how + ": from() returned null");
            default -> failures.add(sample + " " + how + ": " + decoded.describe());
        }
    }
}
