package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A room of a component's own (RFC 0067): an element inside it that is no part - it has no look of its own that its
 * owner's could not give it, only the owner's spacing, and nothing to do - so it shares its owner's class and is its
 * owner's to make and to place: a panel's body, a slider group's rack, a strip's tail. What sits in a room is the owner's
 * - its own parts, or a caller's content - never a part's. Its look is ruled in its owner's look, beside the casting's
 * rule, under the casting's class and the room's name.
 *
 * @param name its name: lower-case words joined by hyphens, unique among its component's rooms
 */
public record Room(String name) implements ValueObject {

    private static final Pattern NAME = Pattern.compile("[a-z][a-z0-9]*(-[a-z0-9]+)*");

    public Room {
        Objects.requireNonNull(name, "Room.name");
        if (!NAME.matcher(name).matches()) throw new IllegalArgumentException("Room: '" + name + "' is no room's name - lower-case words joined by hyphens");
    }
}
