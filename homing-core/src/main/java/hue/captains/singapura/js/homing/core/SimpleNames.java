package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/** The name a linkable goes by when it does not say: its class's simple name in kebab case. */
public record SimpleNames() implements StatelessFunctionalObject {

    public static final SimpleNames INSTANCE = new SimpleNames();

    /**
     * Default simple-name derivation. Splits the class's simple name on
     * uppercase boundaries, lowercases each piece, joins with {@code -}.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code PitchDeck.class}     → {@code "pitch-deck"}</li>
     *   <li>{@code WonderlandDemo.class} → {@code "wonderland-demo"}</li>
     *   <li>{@code HTTPHandler.class}   → {@code "http-handler"}</li>
     *   <li>{@code A.class}             → {@code "a"}</li>
     * </ul>
     *
     * <p>For an acronym followed by a normal word ({@code HTTPHandler}), the
     * acronym is treated as a single segment.</p>
     */
    public String of(Class<?> cls) {
        String simple = cls.getSimpleName();
        if (simple.isEmpty()) {
            throw new IllegalArgumentException("Cannot derive simple name from empty class name");
        }

        StringBuilder out = new StringBuilder(simple.length() + 4);
        int i = 0;
        int n = simple.length();
        while (i < n) {
            char c = simple.charAt(i);
            if (Character.isUpperCase(c)) {
                // Walk a run of uppercase chars (handles acronyms like HTTP).
                int start = i;
                while (i < n && Character.isUpperCase(simple.charAt(i))) {
                    i++;
                }
                int runEnd = i; // exclusive

                // If a non-upper follows, the LAST upper of the run starts the next word.
                // E.g., "HTTPHandler" → run [HTTP], then "Handler" — the H of Handler
                // belongs to the next word, not the acronym.
                int acronymEnd = (i < n && Character.isLowerCase(simple.charAt(i)) && (runEnd - start) > 1)
                        ? runEnd - 1
                        : runEnd;

                if (acronymEnd > start) {
                    if (out.length() > 0) out.append('-');
                    for (int k = start; k < acronymEnd; k++) {
                        out.append(Character.toLowerCase(simple.charAt(k)));
                    }
                }
                if (acronymEnd < runEnd) {
                    // The leftover upper char starts the next word.
                    if (out.length() > 0) out.append('-');
                    out.append(Character.toLowerCase(simple.charAt(acronymEnd)));
                }
            } else {
                // Continue the current word.
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }
}
