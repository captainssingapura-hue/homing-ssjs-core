package hue.captains.singapura.js.homing.studio.base.composed.text;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;
import java.util.Optional;

/**
 * A section / heading title — a single plain line, non-blank, hard-capped at
 * {@value #MAX_CHARS} chars.
 *
 * <p>A tighter sibling of {@link Line.Plain} (which caps at
 * {@value Line#MAX_CHARS}): a title is a heading, and long headings read badly,
 * so the cap is deliberately smaller. Like the other {@code text} value objects,
 * it moves the constraint into the type rather than leaving titles as raw,
 * unbounded {@code String}s.</p>
 */
public record Title(String text) implements ValueObject {

    /** Hard cap on a title's length, in chars — deliberately tighter than a {@link Line}. */
    public static final int MAX_CHARS = 66;

    public Title {
        Objects.requireNonNull(text, "Title.text");
        if (text.isBlank()) {
            throw new IllegalArgumentException("Title.text must not be blank — a title always says something");
        }
        if (text.length() > MAX_CHARS) {
            throw new IllegalArgumentException(
                    "Title.text exceeds " + MAX_CHARS + " chars (was " + text.length() + ")");
        }
    }

    /** Wrap a title string as an optional {@code Title} — {@code null}/blank becomes {@link Optional#empty()}. */
    public static Optional<Title> optional(String s) {
        return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(new Title(s));
    }

    /**
     * A human title derived from a {@link NodeName} — words split on {@code -},
     * {@code _} or {@code .}, each capitalized, joined by spaces
     * ({@code "dancing-animals"} yields {@code "Dancing Animals"}). The default
     * heading when a caller has a slug-like name but no separate title; always
     * within {@link #MAX_CHARS}, since a name is capped shorter still.
     *
     * <p>Lives here rather than on {@code NodeName} (RFC 0053). The name is an
     * ADDRESS and belongs to the tree substrate, which knows nothing of headings;
     * humanizing one is PRESENTATION and belongs with the text values. Keeping the
     * dependency pointing this way is what let {@code NodeName} move at all.</p>
     */
    public static Title humanizing(NodeName name) {
        Objects.requireNonNull(name, "name");
        String value = name.value();
        var sb = new StringBuilder(value.length());
        for (String word : value.split("[-_.]+")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) sb.append(word, 1, word.length());
        }
        return new Title(sb.length() == 0 ? value : sb.toString());
    }
}
