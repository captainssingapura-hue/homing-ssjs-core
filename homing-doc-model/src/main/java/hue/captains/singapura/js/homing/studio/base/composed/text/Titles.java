package hue.captains.singapura.js.homing.studio.base.composed.text;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Objects;
import java.util.Optional;

/** Titles read from what may not be one, or made from a name ({@link Title}). */
public record Titles() implements StatelessFunctionalObject {

    public static final Titles INSTANCE = new Titles();

    /** Wrap a title string as an optional {@code Title} — {@code null}/blank becomes {@link Optional#empty()}. */
    public Optional<Title> optional(String s) {
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
    public Title humanizing(NodeName name) {
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
