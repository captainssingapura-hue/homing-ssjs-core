package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * What a node means: its logical meaning - what it is for, in the terms of the user and the
 * business it serves, never how it looks. A brief section of markdown, the definitive guide to how
 * the node is to be used and designed, and what its finer states are to be grounded on.
 *
 * <p>Every node of both trees has one - the roots, every branch, every component, every role -
 * written in a markdown file beside the class that declares it, under {@code meanings/}: the
 * section headed by the node's name within that class. A node of {@code com.example.House} at
 * {@code House.Card} is the section {@code ## Card} of {@code meanings/com/example/House.md}; a node
 * that is the class itself is the section headed by its own name. A leaf's meaning narrows its
 * branch's, so a node is read with the meanings above it. A part has none of its own: its role's
 * says what it does, its base's what plays it.</p>
 *
 * @param markdown the section's body, its heading left out
 */
public record Meaning(String markdown) implements ValueObject {

    public Meaning {
        Objects.requireNonNull(markdown, "Meaning.markdown");
    }
}
