package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A value a component is made with, immutable for its life (RFC 0071 port, owner 23 Oct): what is no part's choice - a
 * floating pane's least size, where a pane's floaters are drawn - named, typed, and with its default, so that its caller
 * gives only what differs; groups nest them. Given on the page in the component's fixed object, after its parts'
 * choices, checked against what is declared here, its defaults filled in, and frozen all the way down. Said in no
 * language: the page's checks are made from it.
 *
 * <p>Its name is a field's, as the page says it: a lower-case letter, then letters and digits - {@code least}. A value
 * that may be none says so; its default may then be none.</p>
 */
public sealed interface FixedValue extends ValueObject
        permits FixedValue.Whole, FixedValue.YesNo, FixedValue.Text, FixedValue.OneOf, FixedValue.Element, FixedValue.Group {

    /** Its name, unique among its group's. */
    String name();

    /** Whether it may be none. */
    boolean orNone();

    /** A whole number, none below 0: a measure, a count. */
    record Whole(String name, Integer byDefault, boolean orNone) implements FixedValue {
        public Whole {
            named(name, "Whole");
            if (byDefault == null && !orNone) throw new IllegalArgumentException("Whole " + name + ": its default is none, and it may not be none");
            if (byDefault != null && byDefault < 0) throw new IllegalArgumentException("Whole " + name + ": its default is a whole number, none below 0, not " + byDefault);
        }
    }

    /** Yes or no. */
    record YesNo(String name, Boolean byDefault, boolean orNone) implements FixedValue {
        public YesNo {
            named(name, "YesNo");
            if (byDefault == null && !orNone) throw new IllegalArgumentException("YesNo " + name + ": its default is none, and it may not be none");
        }
    }

    /** A text: any, the empty one among them. */
    record Text(String name, String byDefault, boolean orNone) implements FixedValue {
        public Text {
            named(name, "Text");
            if (byDefault == null && !orNone) throw new IllegalArgumentException("Text " + name + ": its default is none, and it may not be none");
        }
    }

    /** One of a set of names: lower-case words joined by hyphens, each once. */
    record OneOf(String name, List<String> names, String byDefault, boolean orNone) implements FixedValue {
        private static final Pattern WORDS = Pattern.compile("[a-z][a-z0-9]*(-[a-z0-9]+)*");
        public OneOf {
            named(name, "OneOf");
            names = List.copyOf(Objects.requireNonNull(names, "OneOf.names"));
            if (names.isEmpty()) throw new IllegalArgumentException("OneOf " + name + ": one of no names");
            if (!names.stream().allMatch(n -> WORDS.matcher(n).matches())) throw new IllegalArgumentException("OneOf " + name + ": its names are lower-case words joined by hyphens: " + names);
            if (new HashSet<>(names).size() != names.size()) throw new IllegalArgumentException("OneOf " + name + ": names each once: " + names);
            if (byDefault == null ? !orNone : !names.contains(byDefault)) throw new IllegalArgumentException("OneOf " + name + ": its default is one of its names" + (orNone ? ", or none" : "") + ", not " + byDefault);
        }
    }

    /** An element of the page, given by its caller - where what it makes is drawn: none by default, always. */
    record Element(String name) implements FixedValue {
        public Element { named(name, "Element"); }
        @Override public boolean orNone() { return true; }
    }

    /** A group of values, each named once: never none - a value it is not given is its default. */
    record Group(String name, List<FixedValue> values) implements FixedValue {
        public Group {
            named(name, "Group");
            values = List.copyOf(Objects.requireNonNull(values, "Group.values"));
            if (values.isEmpty()) throw new IllegalArgumentException("Group " + name + ": a group of no values");
            var seen = new HashSet<String>();
            for (FixedValue v : values) if (!seen.add(v.name())) throw new IllegalArgumentException("Group " + name + ": names its value " + v.name() + " twice");
        }
        @Override public boolean orNone() { return false; }
    }

    private static void named(String name, String kind) {
        Objects.requireNonNull(name, kind + ".name");
        if (!name.matches("[a-z][A-Za-z0-9]*")) throw new IllegalArgumentException(kind + ": '" + name + "' is no value's name - a lower-case letter, then letters and digits");
    }
}
