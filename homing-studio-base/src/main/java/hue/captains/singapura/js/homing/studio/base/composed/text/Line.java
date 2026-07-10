package hue.captains.singapura.js.homing.studio.base.composed.text;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;
import java.util.Optional;

/**
 * A single line of text — the smallest typed building block above a raw
 * {@code String}. Sealed sum with two variants, distinguished by whether
 * the source carries inline articulation:
 *
 * <ul>
 *   <li>{@link Plain}       — literal text, no articulation marks. The hard
 *       cap of {@value #MAX_CHARS} chars applies to the raw string directly.</li>
 *   <li>{@link Articulated} — text that carries inline articulation marks
 *       (the {@code Inline} grammar — bold/italic/code/ref markers). The cap
 *       of {@value #MAX_CHARS} applies to the <i>effective</i> length: the
 *       visible text, with the marks excluded.</li>
 * </ul>
 *
 * <p>The point is to replace raw {@code String} at the boundaries where a
 * value is known to be one line and length-bounded (captions, titles), so the
 * constraint lives in the type rather than in scattered checks.</p>
 *
 * <p><b>Articulation-mark stripping is not yet implemented</b>:
 * {@link Articulated#effectiveLength()} currently counts the raw length. Once
 * the mark grammar phases in, the exclusion lands in one place
 * ({@code Articulated}'s length helper) and the cap starts biting on visible
 * text only.</p>
 */
public sealed interface Line extends ValueObject
        permits Line.Plain, Line.Articulated {

    /** The hard cap on a single line's effective length, in chars. */
    int MAX_CHARS = 81;

    /** The raw source text of the line (articulation marks included, if any). */
    String raw();

    /** The length that counts against {@link #MAX_CHARS}. */
    int effectiveLength();

    /**
     * Wrap a caption/title string as an optional {@link Plain} line — a
     * {@code null} or blank string becomes {@link Optional#empty()}. The single
     * place the String-authoring boundary (DSL, convenience constructors)
     * crosses into the typed line.
     */
    static Optional<Plain> optionalPlain(String s) {
        return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(new Plain(s));
    }

    /** A plain single line — literal text, no articulation. Cap applies to the raw string. */
    record Plain(String raw) implements Line {
        public Plain {
            Objects.requireNonNull(raw, "Line.Plain.raw");
            if (raw.length() > MAX_CHARS) {
                throw new IllegalArgumentException(
                        "Line.Plain.raw exceeds " + MAX_CHARS + " chars (was " + raw.length() + ")");
            }
        }

        @Override public int effectiveLength() { return raw.length(); }
    }

    /**
     * An articulated single line — carries inline articulation marks that do
     * not count toward the visible length. The cap applies to
     * {@link #effectiveLength()}.
     */
    record Articulated(String raw) implements Line {
        public Articulated {
            Objects.requireNonNull(raw, "Line.Articulated.raw");
            int eff = effectiveLengthOf(raw);
            if (eff > MAX_CHARS) {
                throw new IllegalArgumentException(
                        "Line.Articulated effective length exceeds " + MAX_CHARS + " chars (was " + eff + ")");
            }
        }

        @Override public int effectiveLength() { return effectiveLengthOf(raw); }

        /**
         * The visible length, with articulation marks excluded.
         *
         * <p>TODO: strip articulation marks once the mark grammar phases in.
         * Until then this counts the raw length — the effective cap is a lower
         * bound on the true visible allowance, so nothing over-long slips through.</p>
         */
        private static int effectiveLengthOf(String raw) {
            return raw.length();
        }
    }
}
